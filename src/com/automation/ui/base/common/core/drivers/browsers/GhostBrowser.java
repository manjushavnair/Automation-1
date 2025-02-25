package com.automation.ui.base.common.core.drivers.browsers;

import com.automation.ui.base.common.core.UIWebDriver;
import com.automation.ui.base.common.core.drivers.BrowserAbstract;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.phantomjs.*;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

public class GhostBrowser extends BrowserAbstract {


    @Override
    public void setOptions() {
        String phantomJSBinaryName;
        String osName = System.getProperty("os.name").toUpperCase();

        if (osName.equalsIgnoreCase("WINDOWS")) {
            phantomJSBinaryName = "phantomjs.exe";

            File phantomJSBinary =
                    new File("." + File.separator + "src" + File.separator + "test" + File.separator
                            + "resources" + File.separator + "PhantomJS" + File.separator + phantomJSBinaryName);

            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    phantomJSBinary.getAbsolutePath());
        }
    }

    @Override
    public UIWebDriver create() {

	// String [] phantomJsArgs = {"--web-security=no", "--ignore-ssl-errors=yes"};
    //  caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomJsArgs);

		caps.setCapability("takesScreenshot", true);
		caps.setJavascriptEnabled(true);
		return new UIWebDriver(new PhantomJSDriver(caps), false);
    }

    @Override
    public void addExtension(String extensionName) {
        // No extensions are applied to PhantomJS browser
    }

	    /**
	     * Set Proxy instance for B Browser instance
	     *  @return void
	     */
    protected void setProxy() {
		 Proxy proxyServer = Proxy.extractFrom(caps);

		 caps.setCapability(CapabilityType.PROXY, proxyServer);

	}
}
