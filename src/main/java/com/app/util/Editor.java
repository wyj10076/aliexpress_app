package com.app.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;

public class Editor {

	public static Properties getPhrases() {
		Properties phrases = new Properties();
		String header = "<span>안녕하세요.</span><br/><br/>";
		header += "<span style=\"color: #FF8000; font-weight: bold;\">알릭스</span><span>입니다.</span><br/><br/>";
		header += "상품을 클릭하시면 바로 구매하실 수 있습니다.<br/><br/>";
		header += "<h4>알리익스프레스 프로모션 코드</h4>";
		header += "<a href=\"http://s.click.aliexpress.com/e/_dZ9tTNW?bz=725*90\" target=\"_parent\">";
		header += "<img width=\"725\" height=\"90\""
								+ "src=\"https://ae01.alicdn.com/kf/HTB1bf4ahYZnBKNjSZFG762t3FXa1/EN_725_90.png\"/>";
		header += "</a><br/><br/>";
		
		String footer = "<br/><br/><span>기타 문의사항 등은 해당 메일로 보내시면 빠른 시일 내에 답변드리겠습니다^^</span><br/><br/>";
		footer += "<span>오늘도 좋은 하루 되세요!</span>";
		
		phrases.put("header", header);
		phrases.put("footer", footer);
		
		return phrases;
	}
	
	public static Map<String, Object> createHtml(EditorType type, String keyword, Map<String, List<Properties>> processedData) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<Properties> products = processedData.get(keyword);
		
		String content = "";
		content += "<h3 style=\"display: inline-block;\">'" + keyword + "' 키워드로 검색한 결과</h3>\n";
		
		// 카테고리 시작글
		if (type == EditorType.SEND || type == EditorType.HTML) {
			String moreLink = AliExpress.linkGenerate(LinkType.SEARCH, keyword);
			content += "<a href=\"" + moreLink + "\" style=\"margin-left: 5px;\">더 보기</a>\n";
		}
		
		content += "<br/>\n";
		
		BodyPart messageBodyPart = null;
		
		if (products.size() == 0) {
			
			if (type == EditorType.SEND) {
				content += "<img src=\"cid:no-result\"/>\n";
				messageBodyPart = new MimeBodyPart();
				
				// 이미지 첨부
	            DataSource fds = new FileDataSource(Config.NO_RESULT_IMAGE_PATH);
	        
	        	messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID","<no-result>");
				
			} else if (type == EditorType.PREVIEW) {
				content += "<img src=\"" + new File(Config.NO_RESULT_IMAGE_PATH).getCanonicalPath() + "\"/>\n";
				
			} else if (type == EditorType.HTML) {
				content += "<div>검색 결과가 없습니다.</div>\n";
			}
				
		} else {
			content += "<div class=\"products_container\" style=\"max-width: fit-content; line-height: 1.3;\">\n";
			
			for (Properties product : products) {
				
				String title = (String) product.get("title");
				String link = (String) product.get("link");
				String image = (String) product.get("image");
				String price = (String) product.get("price");
				String shipping = (String) product.get("shipping");
				String rating = (String) product.get("rating");
				String saleVolume = (String) product.get("saleVolume");
				String store = (String) product.get("store");
				
				content += "\t<div class=\"product_container\" style=\"display: inline-block;"
										+ "        vertical-align: top;" 
										+ "        width: 200px;"
										+ "        height: 300px;"
										+ "		   border: 3px solid #d1d1d5;"
										+ "		   border-radius: 10px;"
										+ "		   margin-bottom: 5px;"
										+ "        margin-right: 5px;\">\n";
				content += "\t\t<a style=\"text-decoration: none; color: #000;\" href=\"" + link + "\">\n";
				content += "\t\t\t<div class=\"product_box\" style=\"width: 100%;" 
										+ "        height: 100%;" 
										+ "        background: #fff;" 
										+ "        border-radius: 10px;\">\n";
				content += "\t\t\t\t<div class=\"img_box\" style=\"border-radius: 7px 7px 0 0;" 
										+ "        width: 100%;" 
										+ "        height: 65%;" 
										+ "        text-align: center;\">\n";
				content += "\t\t\t\t\t<img src=\"" + image + "\""
										+ "style=\"width: 100%;" 
										+ "        height: 100%;"
										+ "		   border-radius: 7px 7px 0 0\"/>\n";
				content += "\t\t\t\t</div>\n"; // img_box
				content += "\t\t\t\t<div class=\"title_box\""
										+ "onmouseover=\"this.style.color= '#ff4747'\"" 
										+ "onmouseout=\"this.style.color= '#000'\""
										+ "style=\"width: 95%;" 
										+ "        height: fit-content;" 
										+ "        white-space: nowrap;" 
										+ "        overflow: hidden;" 
										+ "        text-overflow: ellipsis;" 
										+ "        margin-top: 5px;"
										+ "        margin-left: 5px;\">\n";
				content += "\t\t\t\t\t<span class=\"product_title\"" 
										+ "onmouseover=\"this.style.color= '#ff4747'\"" 
										+ "onmouseout=\"this.style.color= '#000'\""
										+ "style=\"width: 100%;" 
										+ "        font-size: 12px;" 
										+ "        line-height: 14px;\">"
										+ title 
										+ "</span>\n";
				content += "\t\t\t\t</div>\n"; // title_box
				content += "\t\t\t\t<div class=\"sale_price\" style=\"margin-left: 5px;" 
										+ "        font-weight: bold;" 
										+ "        letter-spacing: 0.03rem;\">" + price + "</div>\n";
				content += "\t\t\t\t<div class=\"shipping\" style=\"font-size: 12px;"
										+ "        color: #666666;"
										+ "        margin-left: 5px;\">" + shipping + "</div>\n";
				content += "\t\t\t\t<div class=\"last_line\" style=\"margin: 0 5px 0;\">\n";
				content += "\t\t\t\t\t<div class=\"evaluate_rate\">\n";
				content += "\t\t\t\t\t\t<span class=\"star_icon\" style=\"float: left;" 
										+ "        font-size: 15px;" 
										+ "        color: #ff4747;\">★</span>\n";
				content += "\t\t\t\t\t\t<span class=\"count\" style=\"float: left;" 
										+ "        margin-left: 3px;" 
										+ "        font-size: 13px;" 
										+ "        color: #999999;" 
										+ "        line-height: 23px;\">" + rating + "</span>\n";
				content += "\t\t\t\t\t</div>"; // evaluate_rate 
				content += "\t\t\t\t\t<div class=\"sale_volume\" style=\"float: right;" 
										+ "        font-size: 13px;" 
										+ "        line-height: 25px;" 
										+ "        color: #999999;\">" + saleVolume + "</div>\n";
				content += "\t\t\t\t</div>"; // last_inline
				content += "\t\t\t\t<div class=\"store_box\""
										+ "onmouseover=\"this.style.color= '#ff4747'\"" 
										+ "onmouseout=\"this.style.color= '#999999'\""
										+ "style=\"width: 95%;"
										+ "		   height: fit-content;"
										+ "		   white-space: nowrap;"
										+ "        overflow: hidden;"
										+ "        text-overflow: ellipsis;"
										+ "        margin-left: 5px;"
										+ "        font-size: 12px;"
										+ "        color: #999999;\">\n";
				content += "\t\t\t\t\t<span class=\"product_title\""
										+ "onmouseover=\"this.style.color= '#ff4747'\"" 
										+ "onmouseout=\"this.style.color= '#999999'\""
										+ "style=\"width: 100%"
										+ "		   font-size: 12px;"
										+ "        color: #999999;"
										+ "        text-decoration: underline;\">" + store + "</span>\n";
				content += "\t\t\t\t</div>\n"; // store_box
				content += "\t\t\t</div>\n"; // product_box
				content += "\t\t</a>\n";
				content += "\t</div>\n"; // product_container
			}
			
			content += "</div>";
			
		}
		
		map.put("content", content);
		map.put("messageBodyPart", messageBodyPart);
		
		return map;
	}
	
}
