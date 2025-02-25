package com.automation.ui.base.common.api.clientimpl.httpbaseimpl;

import com.automation.ui.base.common.api.util.ContentType;
import com.automation.ui.base.common.api.util.MethodType;
import com.automation.ui.base.common.api.adapter.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.given;

public class PutAdapter extends  AbstractAdapter implements  RestAdapter {
    private String name;

    protected PutAdapter(GetBuilder<?, ?> builder) {
        super(builder);
        this.name = builder.name;

    }

    @Override
    public JsonPath execute() {

        return null;
    }
    public static GetBuilder<?, ?> builder() {
        return new DefaultGetBuilder();
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T execute(Class<T> responseClass) {
        Gson jsonParser = new Gson();
        final String endpoint = getEndPoint() + getMethod();
        String body = jsonParser.toJson(getObject());
        HttpURLConnection request = null;
        try {
            request = putRequest(endpoint, body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String response = null;
        try {
            response = makeRawRequest(request, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Request \n" + prettyPrint(body) + "\n");
        System.out.println("Response \n" + prettyPrint(response) + "\n");
        return jsonParser.fromJson(response, responseClass);
    }

    private HttpURLConnection putRequest(String command, String body) throws IOException {
        HttpURLConnection con = null;
        try {
            URL obj = new URL(command);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(MethodType.PUT.getMethodType());
            con.setRequestProperty("Content-Type", ContentType.JSON.getContentType());
            con.setDoOutput(true);
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(body);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return con;
    }


    public static abstract class GetBuilder<S extends PutAdapter, B extends GetBuilder<S, B>> extends AbstractBuilder<S, B> {
        private String name;

        @SuppressWarnings("unchecked")
        public B name(String name) {
            this.name = name;
            return (B) this;
        }

    }

    private static class DefaultGetBuilder extends GetBuilder<PutAdapter, DefaultGetBuilder> {
        @Override
        public PutAdapter build() {
            return new PutAdapter(this);
        }
    }
}