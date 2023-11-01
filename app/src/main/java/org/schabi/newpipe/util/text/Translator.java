package org.schabi.newpipe.util.text;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;


import org.json.JSONException;
import org.json.JSONObject;
import org.schabi.newpipe.util.Localization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public final class Translator {
    public static final String TAG = Translator.class.getSimpleName();

    private static final String PAPAGO_CLIENT_ID = "2jlowug69u";
    private static final String PAPAGO_CLIENT_SECRET = "NdudEcX07bVrRCrS49bKaNLTk0rGVKpT2NYyCu0S";
//    private static final String API_URL = "https://openapi.naver.com/v1/papago/n2mt";
    private static final String PAPAGO_API_URL =
        "https://naveropenapi.apigw.ntruss.com/nmt/v1/translation";

    private Translator() {
    }

    public static void translate(@NonNull final TextView textView, @NonNull final String tag) {
        final String targetLang = Localization.getAppLocale(textView.getContext()).toString();
        Log.d("translate Text", targetLang);
        translate(textView, tag, "auto", targetLang);

    }

    public static void translate(@NonNull final TextView textView, @NonNull final String tag,
                                 final String sourceLang, @NonNull final String targetLang) {
//        if (true) {
        if (sourceLang.equals(targetLang)) {
            return;
        }
        final OkHttpClient client = new OkHttpClient();

        final String text = textView.getText().toString();
        final String encodedText;
        encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

        final String boundary = UUID.randomUUID().toString();
        final RequestBody requestBody = new MultipartBody.Builder(boundary)
                .setType(MultipartBody.FORM)
                .addFormDataPart("source", sourceLang)
                .addFormDataPart("target", targetLang)
                .addFormDataPart("text", encodedText)
                .build();

        final Request request = new Request.Builder().url(PAPAGO_API_URL)
                .header("Content-Type", requestBody.contentType().toString())
                .header("X-NCP-APIGW-API-KEY-ID", PAPAGO_CLIENT_ID)
                .header("X-NCP-APIGW-API-KEY", PAPAGO_CLIENT_SECRET)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                final String responseBody = response.body().string();
                final JSONObject responseJson;
                try {
                    responseJson = new JSONObject(responseBody);
                    final String translatedText = responseJson.getJSONObject("message")
                            .getJSONObject("message").getString("translatedText");
//                    final JSONObject jsonObject = new JSONObject(jsonString);
//                    final JSONObject messageObject = jsonObject.getJSONObject("message");
//                    final JSONObject resultObject = messageObject.getJSONObject("result");
//                    return resultObject.getString("translatedText");

                    Log.d("translate", text + " => " + translatedText);
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(translatedText);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static final String NAVER_CLIENT_ID = "cdq70b0f3o";
    private static final String NAVER_CLIENT_SECRET = "QkrZmLgfaqqFlypmlnXuB8pnbPawEVc0UIhbEf8F";
    private static final String NAVER_API_URL =
            "https://naveropenapi.apigw.ntruss.com/image-to-image/v1/translate";

    // to control the number of translated images
    private static int translateImageCnt = 0;

    public static void translate(final ImageView imageView) {
        final String targetLang = Localization.getAppLocale(imageView.getContext()).toString();
        Log.d("translate Image", targetLang);
        translate(imageView, "auto", targetLang);
    }
    public static void translate(final ImageView imageView, final String sourceLang,
                                      final String targetLang) {
        if (translateImageCnt < 0) {
            return;
        }
        translateImageCnt++;
        if (sourceLang.equals(targetLang)) {
            return;
        }

        final OkHttpClient client = new OkHttpClient();

        // Extract Bitmap from ImageView
        final Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] byteArray = stream.toByteArray();

        final String boundary = UUID.randomUUID().toString();
        final RequestBody requestBody = new MultipartBody.Builder(boundary)
                .setType(MultipartBody.FORM)
                .addFormDataPart("source", sourceLang)
                .addFormDataPart("target", targetLang)
                .addFormDataPart("image", "a.png",
                        RequestBody.create(byteArray, MediaType.parse("application/octet-stream")))
                .build();

        final Request request = new Request.Builder().url(NAVER_API_URL)
                .header("Content-Type", requestBody.contentType().toString())
                .header("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID)
                .header("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                final String responseBody = response.body().string();
                final JSONObject responseJson;
                try {
                    responseJson = new JSONObject(responseBody);
                    Log.d("translateImage",
                            responseJson.getJSONObject("data").getString("sourceText")
                            + " => "
                            + responseJson.getJSONObject("data").getString("targetText"));
                    final String imageStr =
                            responseJson.getJSONObject("data")
                                    .getString("renderedImage");
                    final byte[] imgData = Base64.getDecoder().decode(imageStr);

                    final Bitmap translatedBitmap =
                            BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(translatedBitmap);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static String translateText(@NonNull final TextView textView,
                                       @NonNull final String tag) {
        final String text = textView.getText().toString();
        final String targetLang = Localization.getAppLocale(textView.getContext()).toString();
        Log.d("translate Text", targetLang);
        return translateText(text, tag, "auto", targetLang);
    }

    public static String translateText(@NonNull final String text, @NonNull final String tag,
                                 final String sourceLang, @NonNull final String targetLang) {
//        if (true) {
        if (sourceLang.equals(targetLang)) {
            return text;
        }
        final String encodedText;
        encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

        final Map<String, String> requestHeaders = new HashMap<>();
//        requestHeaders.put("X-Naver-Client-Id", CLIENT_ID);
//        requestHeaders.put("X-Naver-Client-Secret", CLIENT_SECRET);
        requestHeaders.put("X-NCP-APIGW-API-KEY-ID", PAPAGO_CLIENT_ID);
        requestHeaders.put("X-NCP-APIGW-API-KEY", PAPAGO_CLIENT_SECRET);

        final String responseInJson =
                post(PAPAGO_API_URL, requestHeaders, encodedText, sourceLang, targetLang);

        String translatedText;
        try {
            translatedText = getTranslatedTextFromJson(responseInJson);
//            System.out.println(translatedText);  // Outputs: core
        } catch (final JSONException e) {
            translatedText = text;
        }
//        System.out.println(translatedText);
        Log.d("Translator", sourceLang + text + " => " + targetLang + translatedText);
        return translatedText;
    }

    private static String post(final String apiUrl, final Map<String, String> requestHeaders,
                               final String text, final String srcLang, final String targetLang) {
        final HttpURLConnection con = connect(apiUrl);
        final String postParams = "source=" + srcLang + "&target=" + targetLang + "&text=" + text;
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

    public static String translate(@NonNull final String text, @NonNull final String tag) {
        if (false) {
            return text;
        }
//        System.out.println(text);
//        Log.e("Translator", text);
        final String encodedText;
        encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

        final Map<String, String> requestHeaders = new HashMap<>();
//        requestHeaders.put("X-Naver-Client-Id", CLIENT_ID);
//        requestHeaders.put("X-Naver-Client-Secret", CLIENT_SECRET);
        requestHeaders.put("X-NCP-APIGW-API-KEY-ID", PAPAGO_CLIENT_ID);
        requestHeaders.put("X-NCP-APIGW-API-KEY", PAPAGO_CLIENT_SECRET);

        final String responseInJson = post(PAPAGO_API_URL, requestHeaders, encodedText);

        String translatedText;
        try {
            translatedText = getTranslatedTextFromJson(responseInJson);
//            System.out.println(translatedText);  // Outputs: core
        } catch (final JSONException e) {
            translatedText = text;
        }
//        System.out.println(translatedText);
        Log.d("Translator", text + " => " + translatedText);
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

    private static String getTranslatedTextFromJson(final String jsonString) throws JSONException {
        final JSONObject jsonObject = new JSONObject(jsonString);
        final JSONObject messageObject = jsonObject.getJSONObject("message");
        final JSONObject resultObject = messageObject.getJSONObject("result");
        return resultObject.getString("translatedText");
    }

}
