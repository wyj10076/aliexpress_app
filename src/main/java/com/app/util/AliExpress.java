package com.app.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AliexpressAffiliateLinkGenerateRequest;
import com.taobao.api.response.AliexpressAffiliateLinkGenerateResponse;

public class AliExpress {

	public static String linkGenerate(LinkType type, String link) throws Exception {
		TaobaoClient client = new DefaultTaobaoClient(Config.ALI_URL, Config.ALI_APP_KEY, Config.ALI_SECRET);
		AliexpressAffiliateLinkGenerateRequest req = new AliexpressAffiliateLinkGenerateRequest();
		
		String promotionLink = null;
		
		long linkType = type == LinkType.PRODUCT ? 0L : 1L;
		req.setPromotionLinkType(linkType);
		req.setSourceValues(link);
		req.setTrackingId("naver");

		AliexpressAffiliateLinkGenerateResponse rsp = client.execute(req);

		JSONParser parser = new JSONParser();
		JSONObject resp_result = (JSONObject) ((JSONObject) ((JSONObject) parser.parse(rsp.getBody()))
				.get("aliexpress_affiliate_link_generate_response")).get("resp_result");
		
		String resp_code = String.valueOf(resp_result.get("resp_code"));
		if (resp_code.equals("200")) {
			promotionLink = (String) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) resp_result.get("result"))
					.get("promotion_links")).get("promotion_link")).get(0)).get("promotion_link");
		}
		
		return promotionLink;
	}
}
