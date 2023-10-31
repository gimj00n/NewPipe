package org.schabi.newpipe.util.text;

import android.util.Log;

import androidx.annotation.NonNull;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


public final class Translator {
    public static final String TAG = Translator.class.getSimpleName();

    private static final String CLIENT_ID = "2jlowug69u";
    private static final String CLIENT_SECRET = "NdudEcX07bVrRCrS49bKaNLTk0rGVKpT2NYyCu0S";
//    private static final String API_URL = "https://openapi.naver.com/v1/papago/n2mt";
    private static final String API_URL =
        "https://naveropenapi.apigw.ntruss.com/nmt/v1/translation";

    private Translator() {
    }

    private static String getTranslatedTextFromJson(final String jsonString) throws JSONException {
        final JSONObject jsonObject = new JSONObject(jsonString);
        final JSONObject messageObject = jsonObject.getJSONObject("message");
        final JSONObject resultObject = messageObject.getJSONObject("result");
        return resultObject.getString("translatedText");
    }

    /**
     * Translate a text.
     *
     * @param text               the String to translate
     * @param context            the context in which this function is called
     * @return                   the translated text
     */
    public static String translate(@NonNull final String text,
                                       @NonNull final String context) {
        System.out.println(text);
        Log.e("Translator", text);
        final String encodedText;
        encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
//        try {
//            encodedText = URLEncoder.encode(text, "UTF-8");
//        } catch (final UnsupportedEncodingException e) {
//            throw new RuntimeException("encoding failure", e);
//        }

        final Map<String, String> requestHeaders = new HashMap<>();
//        requestHeaders.put("X-Naver-Client-Id", CLIENT_ID);
//        requestHeaders.put("X-Naver-Client-Secret", CLIENT_SECRET);
        requestHeaders.put("X-NCP-APIGW-API-KEY-ID", CLIENT_ID);
        requestHeaders.put("X-NCP-APIGW-API-KEY", CLIENT_SECRET);

        final String responseInJson = post(API_URL, requestHeaders, encodedText);

        String translatedText;
        try {
            translatedText = getTranslatedTextFromJson(responseInJson);
//            System.out.println(translatedText);  // Outputs: core
        } catch (final JSONException e) {
            translatedText = text;
        }
//        System.out.println(translatedText);
        return translatedText;
    }

    private static String post(final String apiUrl,
                               final Map<String, String> requestHeaders,
                               final String text) {
        final HttpURLConnection con = connect(apiUrl);
        final String postParams = "source=en&target=ko&text=" + text;
        try {
            con.setRequestMethod("POST");
            for (final Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postParams.getBytes());
                wr.flush();
            }

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                return readBody(con.getInputStream());
            } else {  // 에러 응답
                return readBody(con.getErrorStream());
            }
        } catch (final IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(final String apiUrl) {
        try {
            final URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (final MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (final IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(final InputStream body) {
        final InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            final StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (final IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

}
