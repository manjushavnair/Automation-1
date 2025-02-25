package com.automation.ui.base.common.logging;

import com.automation.ui.base.common.core.UIWebDriver;
import com.automation.ui.base.common.core.annotations.RelatedIssue;
import com.automation.ui.base.common.core.configuration.Configuration;
import com.automation.ui.base.common.core.imageutilities.Shooter;
import com.automation.ui.base.common.core.url.UrlBuilder;
import com.automation.ui.base.common.driverprovider.DriverProvider;
import com.automation.ui.base.common.exception.BusinessException;
import com.automation.ui.base.common.utils.CommonUtils;
import com.automation.ui.base.common.utils.DateUtil;
import com.automation.ui.base.common.report.filehandler.*;
import lombok.Getter;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.assertj.core.util.Throwables;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class Log {

    private static final String POLISH_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss ZZ";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
    private static final String REPORT_PATH = "." + File.separator + "logs" + File.separator + "realreport" + File.separator;
    private static final String SCREEN_DIR_PATH = REPORT_PATH + "screenshots" + File.separator;
    private static final String SCREEN_PATH = SCREEN_DIR_PATH + "screenshot";
    //create log file in the  physical location ,suite indes hardcoded as 1 for now
    private static final String LOG_FILE_NAME = "log_" + DateUtil.getCurrentDateInReportFormat() + "-1.html";
    public static final String LOG_PATH = REPORT_PATH + LOG_FILE_NAME;
    private static final ArrayList<Boolean> LOGS_RESULTS = new ArrayList<>();
    public static String mobileSiteVersion = "";
    private static long imageCounter;

    private static Logger logger = Logger.getLogger(Log.class);

    @Getter
    private static boolean testStarted = false;

    public static void clearLogStack() {
        LOGS_RESULTS.clear();
    }

    // This is to print log for the beginning of the test case, as we usually run so many test cases as B test suite
    public static void startTestCase(String sTestCaseName) {

        logger.info("****************************************************************************************");
        logger.info("$$$$$$$$$$$$$$$$$$$$$                 " + sTestCaseName + "       $$$$$$$$$$$$$$$$$$$$$$$$$");
        logger.info("****************************************************************************************");

    }

    //This is to print log for the ending of the test case
    public static void endTestCase(String sTestCaseName) {
        logger.info("XXXXXXXXXXXXXXXXXXXXXXX             " + "-E---N---D-" + "             XXXXXXXXXXXXXXXXXXXXXX");
        logger.info("X");
        logger.info("X");


    }

  private static void createScreenShotHTMLFile(String htmlFile)
  {


	   File html=null;
	          try {
	             //html = File.createTempFile();
	              html =new File(htmlFile , ".html");

	          } catch (Exception e) {
	              e.printStackTrace();
              }
  }


    public static void log(String command, String description, boolean success, WebDriver driver) {
        LOGS_RESULTS.add(success);
        imageCounter += 1;

        try {
            new Shooter().savePageScreenshot(Log.SCREEN_PATH + Log.imageCounter, driver);
            createScreenShotHTMLFile(Log.SCREEN_PATH + Log.imageCounter);
            VelocityWrapper.fillErrorLogRow(Arrays.asList(LogLevel.ERROR), description, Log.imageCounter);
        } catch (Exception e) {
            VelocityWrapper
                    .fillErrorLogRowWoScreenshotAndSource(Arrays.asList(LogLevel.ERROR), description);
            Log.log("onException",
                    "driver has no ability to catch screenshot or html source - driver may died", false
            );
        }

        new Shooter().savePageScreenshot(SCREEN_PATH + imageCounter, driver);
        createScreenShotHTMLFile(Log.SCREEN_PATH + Log.imageCounter);

        LogData logType = success ? LogLevel.OK : LogLevel.ERROR;
        VelocityWrapper
                .fillLogRowWithScreenshot(Arrays.asList(logType), command, description, imageCounter);
        logJSError();
    }

    public static void log(String command, Throwable e, boolean success, WebDriver driver) {
        LOGS_RESULTS.add(success);
        imageCounter += 1;
        new Shooter().savePageScreenshot(SCREEN_PATH + imageCounter, driver);
        createScreenShotHTMLFile(Log.SCREEN_PATH + Log.imageCounter);
        String
                html =
                VelocityWrapper
                        .fillErrorLogRow(Arrays.asList(success ? LogLevel.OK : LogLevel.ERROR), command,
                                imageCounter
                        );
        CommonUtils.appendTextToFile(LOG_PATH, html);
        logJSError();
    }

    //LogData assertion result
    public static void log(String command, String description, boolean success) {
        log(command, description, success, false);
    }

    public static void log(String command, Throwable e, boolean success) {
        log(command, e.getMessage(), success, false);
    }

    public static void log(
            String command, String descriptionOnSuccess, String descriptionOnFail,
            boolean success
    ) {
        String description = descriptionOnFail;
        if (success) {
            description = descriptionOnSuccess;
        }
        log(command, description, success, false);
    }

    /**
     * LogData an action that is not user facing. LogData file reader can hide these actions to
     * increase test readability
     */
    public static void logOnLowLevel(String command, String description, boolean success) {
        log(command, description, success, true);
    }

    private static void log(
            String command, String description, boolean isSuccess,
            boolean ifLowLevel
    ) {
        LOGS_RESULTS.add(isSuccess);
        String escapedDescription = escapeHtml(description);

        List<LogData> logTypeList = new ArrayList<>();
        logTypeList.add(isSuccess ? LogLevel.OK : LogLevel.ERROR);

        if (ifLowLevel) {
            logTypeList.add(LogLevel.DEBUG);
        }
        VelocityWrapper.fillLogRow(logTypeList, command, description);
        logJSError();
    }

    public static void ok(String command, String description) {
        log(command, description, true);
    }

    public static void logError(String command, Throwable throwable) {
        log(command, escapeHtml(throwable.getMessage()), false, DriverProvider.getActiveDriver());
        stacktrace(throwable);
    }

    public static void warning(String command, Exception exception) {
        warning(command, exception.getMessage());
    }

    /**
     * This method will log warning to log file (line in yellow color)
     */
    public static void warning(String command, String description) {
        VelocityWrapper.fillLogRow(Arrays.asList(LogLevel.WARNING), command, description);
    }

    /**
     * This method will log info to log file (line in blue color)
     */
    public static void info(String description) {
        VelocityWrapper.fillLogRow(Arrays.asList(LogLevel.INFO), "INFO", description);
    }

    /**
     * This method will log info to log file (line in blue color)
     */
    public static void info(String command, String description) {
        VelocityWrapper.fillLogRow(Arrays.asList(LogLevel.INFO), command, description);
    }

    /**
     * This method will log info to log file (line in blue color)
     */
    public static void info(String description, Throwable e) {
        String finalDescription = description + " : " + e.getMessage();
        VelocityWrapper.fillLogRow(Arrays.asList(LogLevel.INFO), "INFO", finalDescription);
    }

    public static void image(String command, File image, boolean success) {
        byte[] bytes = new byte[0];
        try {
            bytes = new Base64().encode(FileUtils.readFileToByteArray(image));
        } catch (IOException e) {
            log("logImage", e.getMessage(), false);
        }
        image(command, new String(bytes, StandardCharsets.UTF_8), success);
    }

    public static void image(String command, String imageAsBase64, boolean success) {
        String imgHtml = VelocityWrapper.fillImage(imageAsBase64);
        VelocityWrapper
                .fillLogRow(Arrays.asList(success ? LogLevel.OK : LogLevel.ERROR), command, imgHtml);
    }

    public static void logJSError() {
        if ("true".equals(Configuration.getJSErrorsEnabled())) {
            JavascriptExecutor js = DriverProvider.getActiveDriver();
            List<String> error =
                    (ArrayList<String>) js.executeScript("return window.JSErrorCollector_errors.pump()");
            if (!error.isEmpty()) {
                VelocityWrapper.fillLogRow(Arrays.asList(LogLevel.ERROR), "click", error.toString());
            }
        }
    }

    public static List<Boolean> getVerificationStack() {
        return LOGS_RESULTS;
    }

    public static void startTest(Method testMethod) {
        String testName = testMethod.getName();
        String className = testMethod.getDeclaringClass().getCanonicalName();
        String command;
        String description;
        if (testMethod.isAnnotationPresent(RelatedIssue.class)) {
            String issueID = testMethod.getAnnotation(RelatedIssue.class).issueID();
            String jiraPath = Configuration.getJiraURL();
            String jiraUrl = jiraPath + issueID;
            String jiraLink = VelocityWrapper.fillLink(jiraUrl, issueID);
            command = "Known failure";
            description =
                    testName + " - " + jiraLink + " " + testMethod.getAnnotation(RelatedIssue.class)
                            .comment();
        } else {
            command = "";
            description = testName;
        }

        startTestCase(testName);
        String html = VelocityWrapper.fillFirstLogRow(className, testName, command, description);
        CommonUtils.appendTextToFile(LOG_PATH, html);
        testStarted = true;
    }

    public static void logAssertionStacktrace(AssertionError exception) {
        UIWebDriver driver = DriverProvider.getActiveDriver();

        Log.imageCounter += 1;
        if ("true".equals(Configuration.getLogEnabled())) {
            String exceptionMessage = ExceptionUtils.getStackTrace(exception);
            List<LogData> classList = new ArrayList<>();
            classList.add(LogLevel.ERROR);
            classList.add(LogType.STACKTRACE);
            String html = VelocityWrapper.fillErrorLogRow(classList, exceptionMessage, Log.imageCounter);
            // logger.info("htmlsource"+html);
            try {
                new Shooter().savePageScreenshot(Log.SCREEN_PATH + Log.imageCounter, driver);
                createScreenShotHTMLFile(Log.SCREEN_PATH + Log.imageCounter);
                CommonUtils.appendTextToFile(Log.LOG_PATH, html);
            } catch (Exception e) {
                html = VelocityWrapper.fillErrorLogRowWoScreenshotAndSource(classList, exceptionMessage);
                CommonUtils.appendTextToFile(Log.LOG_PATH, html);
                Log.log(
                        "onException",
                        "driver has no ability to catch screenshot or html source - driver may died<br/>",
                        false
                );
            }
            Log.logJSError();
        }
    }

    public static void stacktrace(Throwable throwable) {
        String exceptionMessage = Throwables.getStackTrace(throwable);
        List<LogData> classList = new ArrayList<>();
        classList.add(LogLevel.ERROR);
        classList.add(LogType.STACKTRACE);

        VelocityWrapper
                .fillLogRow(classList, "STACKTRACE", escapeHtml(exceptionMessage).replace("\n", "<br>"));
    }

    public static void stop() throws BusinessException {
        UIWebDriver driver = DriverProvider.getActiveDriver();

        // WebElement mercuryScriptVersion = driver.findElement(By.cssSelector("script[src*='mercury_ads_js']"));


        if (driver.getProxy() != null && Configuration.getForceHttps()) {

            Har har = driver.getProxy().getHar();
            for (HarEntry entry : har.getLog().getEntries()) {
                URL url;
                try {
                    url = new URL(entry.getRequest().getUrl());
                    if (url.getHost().contains("connected")) {
                        boolean isHttps = entry.getRequest().getUrl().startsWith("https");
                        Log.log("VISITED URL", "Url: " + entry.getRequest().getUrl(),
                                !Configuration.getForceHttps() || isHttps
                        );
                    }
                } catch (MalformedURLException e) {


                    Log.log("MALFORMED URL", "Url: " + entry.getRequest().getUrl(), false);
                    throw new BusinessException(e.getMessage());
                }
            }
        }


        if (Configuration.getMobileSiteVersion() != null) {
            Log.info("Mobile Site Version: " + Configuration.getMobileSiteVersion());
        }

        String html = VelocityWrapper.fillLastLogRow();

        CommonUtils.appendTextToFile(Log.LOG_PATH, html);
        Log.testStarted = false;
    }

    public static void startReport() {
        CommonUtils.createDirectory(Log.SCREEN_DIR_PATH);
        imageCounter = 0;

        String date = DateTimeFormat.forPattern(Log.DATE_FORMAT).print(DateTime.now(DateTimeZone.UTC));
        String
                polishDate =
                DateTimeFormat.forPattern(Log.POLISH_DATE_FORMAT).print(DateTime.now().withZone(
                        DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Warsaw"))));
        String browser = Configuration.getBrowser();
        String os = System.getProperty("os.name");
        String testingEnvironmentUrl = UrlBuilder.createUrlBuilder().getUrl();
        String testingEnvironment = Configuration.getEnv();
        String testedVersion = "TO DO: GET site VERSION HERE";

        String
                headerHtml =
                VelocityWrapper.fillHeader(date, polishDate, browser, os, testingEnvironmentUrl,
                        testingEnvironment, testedVersion, mobileSiteVersion
                );
        CommonUtils.appendTextToFile(Log.LOG_PATH, headerHtml);


        appendShowHideButtons();
        try {
            FileInputStream input = new FileInputStream("resources"+ File.separator + "script.txt");
            String content = IOUtils.toString(input,"UTF-8");
            CommonUtils.appendTextToFile(Log.LOG_PATH, content);
        } catch (IOException e) {
            System.out.println("no script.txt file available");
        }
    }

    private static void appendShowHideButtons() {
        String hideButton = VelocityWrapper.fillButton("hideLowLevel", "hide low level actions");
        String showButton = VelocityWrapper.fillButton("showLowLevel", "show low level actions");
        StringBuilder builder = new StringBuilder();
        builder.append(hideButton);
        builder.append(showButton);
        CommonUtils.appendTextToFile(Log.LOG_PATH, builder.toString());
    }
}
