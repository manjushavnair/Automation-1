package com.automation.ui.base.common.api.clientimpl.apacheimpl;

import com.automation.ui.base.common.auth.User;
import com.automation.ui.base.common.constants.BASEConstants;
import com.automation.ui.base.common.core.Helios;
import com.automation.ui.base.common.logging.Log;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

import static com.automation.ui.base.common.contentpatterns.URLsContent.API_URL;

public class EditToken {

    private static String EDIT_TOKEN_ERROR_MESSAGE = "Problem with edit token API call";
    private String baseURL = API_URL.replace(BASEConstants.HTTPS_PREFIX, BASEConstants.HTTP_PREFIX);
    private User user;
    private String username;

    public EditToken(User user) {
        this.user = user;
    }

    public EditToken(String username) {
        this.username = username;
    }

    private static ResponseHandler<String> extractEditToken() {
        return res -> {
            HttpEntity entity = res.getEntity();
            Log.info("EDIT TOKEN HEADERS: ", res.toString());
            String source = EntityUtils.toString(entity);
            Log.info("EDIT TOKEN RESPONSE RAW: ", source);
            try {
                JSONObject pagesValue =
                        new JSONObject(source).getJSONObject("query").getJSONObject("pages");
                Iterator pageIterator = pagesValue.keys();
                if (pageIterator.hasNext()) {
                    String key = (String) pageIterator.next();
                    JSONObject pageInfo = pagesValue.getJSONObject(key);
                    return pageInfo.getString("edittoken");
                } else {
                    throw new WebDriverException("Could not find page in edit token response");
                }
            } catch (JSONException e) {
                Log.log("JSON EXCEPTION", ExceptionUtils.getStackTrace(e), false);
                throw new WebDriverException(e);
            }
        };
    }

    public String getEditToken() {
        try {
            String apiURL =
                    new URIBuilder(baseURL).setParameter("action", "query").setParameter("prop", "info")
                            .setParameter("format", "json").setParameter("intoken", "edit")
                            .setParameter("titles", "Main Page").build().toASCIIString();

            CloseableHttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries()
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
            HttpGet httpGet = new HttpGet(apiURL);
            // set header
            if (username != null) {
                httpGet.addHeader(BASEConstants.X_Site_AccessToken, Helios.getAccessToken(username));
            } else if (user != null) {
                httpGet.addHeader(BASEConstants.X_Site_AccessToken, Helios.getAccessToken(user.getUserName()));
            }

            Log.info("QUERY EDIT TOKEN: ", httpGet.toString());
            String editToken = httpClient.execute(httpGet, extractEditToken());

            Log.info("QUERY EDIT TOKEN: ", "Token fetched: " + editToken);

            return editToken;
        } catch (ClientProtocolException e) {
            Log.log("EXCEPTION", ExceptionUtils.getStackTrace(e), false);
            throw new WebDriverException(EDIT_TOKEN_ERROR_MESSAGE);
        } catch (IOException e) {
            Log.log("IO EXCEPTION", ExceptionUtils.getStackTrace(e), false);
            throw new WebDriverException(EDIT_TOKEN_ERROR_MESSAGE);
        } catch (URISyntaxException e) {
            Log.log("URI_SYNTAX EXCEPTION", ExceptionUtils.getStackTrace(e), false);
            throw new WebDriverException(EDIT_TOKEN_ERROR_MESSAGE);
        }
    }
}
