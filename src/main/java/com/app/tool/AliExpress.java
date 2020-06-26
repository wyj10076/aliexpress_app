package com.app.tool;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AliexpressAffiliateLinkGenerateRequest;
import com.taobao.api.request.AliexpressAffiliateProductQueryRequest;
import com.taobao.api.response.AliexpressAffiliateLinkGenerateResponse;
import com.taobao.api.response.AliexpressAffiliateProductQueryResponse;

public class AliExpress {

	public static final String URL;
	public static final String APP_KEY;
	public static final String SECRET;

	static {
		URL = "http://api.taobao.com/router/rest";
		APP_KEY = "30259406";
		SECRET = "606f6aa3e35e217ea1ce97c8d9c0cb81";
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getProducts(String keyword) throws ApiException, ParseException {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, SECRET);
		AliexpressAffiliateProductQueryRequest req = new AliexpressAffiliateProductQueryRequest();
		req.setKeywords(keyword);
		req.setPageNo(1L);
		req.setPageSize(5L);
		req.setPlatformProductType("ALL");
		req.setSort("LAST_VOLUME_DESC");
		req.setTargetCurrency("USD");
		req.setTargetLanguage("KO");
		req.setTrackingId("naver");

		AliexpressAffiliateProductQueryResponse rsp = null;

		rsp = client.execute(req);

		JSONParser parser = new JSONParser();
		JSONObject resp_result = (JSONObject) ((JSONObject) ((JSONObject) parser.parse(rsp.getBody()))
				.get("aliexpress_affiliate_product_query_response")).get("resp_result");

		String resp_code = String.valueOf(resp_result.get("resp_code"));

		List<Map<String, Object>> products = null;
		if (resp_code.equals("200")) {
			products = (List<Map<String, Object>>) ((JSONObject) ((JSONObject) ((JSONObject) resp_result).get("result"))
					.get("products")).get("product");

			return products;
		}

		return null;
	}

	public String linkGenerate(String link) throws ApiException, ParseException, UnsupportedEncodingException {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, SECRET);
		AliexpressAffiliateLinkGenerateRequest req = new AliexpressAffiliateLinkGenerateRequest();
		req.setPromotionLinkType(0L);
		req.setSourceValues(link);
		//req.setSourceValues("https://ko.aliexpress.com/af/" + keyword + ".html?trafficChannel=af&d=y&ltype=affiliate&SortType=total_tranpro_desc&groupsort=1&CatId=0&page=1");
		req.setTrackingId("naver");

		AliexpressAffiliateLinkGenerateResponse rsp = client.execute(req);

		JSONParser parser = new JSONParser();
		JSONObject resp_result = (JSONObject) ((JSONObject) ((JSONObject) parser.parse(rsp.getBody()))
				.get("aliexpress_affiliate_link_generate_response")).get("resp_result");

		String resp_code = String.valueOf(resp_result.get("resp_code"));
		if (resp_code.equals("200")) {
			String promotionLink = (String) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) resp_result.get("result"))
					.get("promotion_links")).get("promotion_link")).get(0)).get("promotion_link");
			
			return promotionLink;
		}

		return null;
	}
}
