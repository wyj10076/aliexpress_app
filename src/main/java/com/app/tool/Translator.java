package com.app.tool;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Translator {

	private static final String PAPAGO_URL = "https://openapi.naver.com/v1/papago/n2mt";
	private static final String CLIENT_ID = "QeAPL3qhIL5KBe9NULU1";
	private static final String CLIENT_SECRET = "wzFuzc0q2a";
	private static final String SOURCE_LANG = "ko";
	private static final String TARGET_LANG = "en";

	public static String translate(String text) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(PAPAGO_URL);
			httpPost.setHeader("X-Naver-Client-Id", CLIENT_ID);
			httpPost.setHeader("X-Naver-Client-Secret", CLIENT_SECRET);

			// Add parameters for POST
			List<NameValuePair> urlParams = new ArrayList<NameValuePair>();
			urlParams.add(new BasicNameValuePair("source", SOURCE_LANG));
			urlParams.add(new BasicNameValuePair("target", TARGET_LANG));
			urlParams.add(new BasicNameValuePair("text", text));

			// 한국어 일본어 중국어 깨짐 방지!
			httpPost.setEntity(new UrlEncodedFormEntity(urlParams, "UTF-8"));

			HttpResponse response = client.execute(httpPost);
			ResponseHandler<String> handler = new BasicResponseHandler();
			String body = handler.handleResponse(response);

			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(body);
			JSONObject message = (JSONObject) obj.get("message");

			JSONObject res = (JSONObject) message.get("result");

			String translatedText = (String) res.get("translatedText");

			return translatedText;
			
		} catch (Exception e) {
			
		}
		
		return null;
	}
}
