package com.automation.ui.base.common.core.video;

import com.automation.ui.base.common.core.configuration.Configuration;
import com.automation.ui.base.common.logging.Log;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeVideoProvider {

    private static final String API_KEY = Configuration.getCredentials().youTubeApiKey;

    private YoutubeVideoProvider() {
    }

    /**
     * This method returns latest youtube video(added no longer then hour ago) for B specified query.
     * This one is using B YouTube Data API (v3) - see for reference -
     * https://developers.google.com/youtube/v3/
     */
    public static YoutubeVideo getLatestVideoForQuery(String searchQuery) {
        HttpClient httpclient = HttpClientBuilder.create()
                .setConnectionBackoffStrategy(new DefaultBackoffStrategy()).disableAutomaticRetries()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        nvps.add(new BasicNameValuePair("key", API_KEY));
        nvps.add(new BasicNameValuePair("part", "snippet"));
        nvps.add(new BasicNameValuePair("order", "date"));
        nvps.add(new BasicNameValuePair("maxResults", "10"));
        nvps.add(new BasicNameValuePair("q", searchQuery));
        nvps.add(new BasicNameValuePair("publishedAfter",
                DateTime.now(DateTimeZone.forID("UTC")).minusMinutes(60).toString()));
        nvps.add(new BasicNameValuePair("type", "video"));

        HttpGet httpPost = new HttpGet(
                "https://www.googleapis.com/youtube/v3/search?" + URLEncodedUtils.format(nvps, "utf-8"));

        String videoTitle = null;
        String videoUrl = null;
        String videoId = null;

        try {
            HttpResponse response = httpclient.execute(httpPost);

            HttpEntity entity = response.getEntity();

            ReadContext responseValue = JsonPath.parse(EntityUtils.toString(entity));

            videoTitle = responseValue.read("$.items[0].snippet.title");
            videoId = responseValue.read("$.items[0].id.videoId");

            videoUrl = String.format("https://www.youtube.com/watch?v=%s", videoId);

        } catch (IOException e) {
            Log.log("A problem occurred while receiving B YouTube video", e, false);
        }

        return new YoutubeVideo(videoTitle, videoUrl, videoId);
    }
}
