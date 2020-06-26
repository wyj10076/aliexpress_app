package com.app.tool;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.simple.parser.ParseException;

import com.app.dto.UserDTO;
import com.taobao.api.ApiException;

public class MailSender {

	private Crawling crawling;
	private AliExpress aliExpress;
	private Session session;
	private String email;
	private String password;
	private Transport transport;
	
	public MailSender() {
	}

	public MailSender(String email, String password) throws AddressException, MessagingException {
		aliExpress = new AliExpress();
		Properties mailProps = new Properties();
		
		// Step1
		this.email = email;
		this.password = password;
		
		mailProps.put("mail.smtp.port", "587");
		mailProps.put("mail.smtp.auth", "true");
		mailProps.put("mail.smtp.starttls.enable", "true");

		// Step2
		session = Session.getDefaultInstance(mailProps, null);
		
		// Step3
		this.transport = this.session.getTransport("smtp");

	}
	
	public void connect() throws MessagingException {
		// Enter your correct gmail UserID and Password
		transport.connect("smtp.gmail.com", this.email, this.password);
		crawling = new Crawling();
	}
	
	public void disconnect() throws MessagingException {
		transport.close();
		crawling.quitBrowser();
	}

	public void sendMessage(UserDTO recipient) throws AddressException, MessagingException, ClassNotFoundException, ApiException, ParseException, UnsupportedEncodingException {
		
		MimeMessage message = getMessage(this.session, recipient);
		
		transport.sendMessage(message, message.getAllRecipients());
		
	}
	
	public MimeMessage getMessage(Session session, UserDTO recipient) throws AddressException, MessagingException, ClassNotFoundException, ApiException, ParseException, UnsupportedEncodingException {
		
		String email = recipient.getEmail();
		String firstKeyword =  recipient.getFirstKeyword();
		String secondKeyword = recipient.getSecondKeyword();
		
		MimeMessage message = new MimeMessage(session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		message.setSubject("오늘의 알리 익스프레스 맞춤 추천 < " + firstKeyword + (!secondKeyword.equals("") ? ", " + secondKeyword : "") + " >");

		MimeMultipart multipart = new MimeMultipart("related");
		
		Properties phrases = getPhrases();
		String header = (String) phrases.get("header");
		String footer = (String) phrases.get("footer");
		
		Map<String, Object> result1 = createHtml(firstKeyword);
		Map<String, Object> result2 = !secondKeyword.equals("") ? createHtml(secondKeyword) : null;
		
		String content = (String) result1.get("content");
		content += result2 != null ? ("<br/>" + (String)result2.get("content")) : "";
		
		BodyPart messageBodyPart = new MimeBodyPart();
		String htmlText = header + content + footer;
		messageBodyPart.setContent(htmlText, "text/html; charset=UTF-8");
		
		multipart.addBodyPart(messageBodyPart);
		
		BodyPart bodypart1 = (BodyPart) result1.get("messageBodyPart");
		if (bodypart1 != null) {
			multipart.addBodyPart(bodypart1);
		}
		
		BodyPart bodypart2 = result2 != null ? (BodyPart) result2.get("messageBodyPart") : null;
		if (bodypart2 != null) {
			multipart.addBodyPart(bodypart2);
		}
		message.setContent(multipart);
		
		return message;
	}
	
	public Properties getPhrases() {
		Properties phrases = new Properties();
		String header = "<span>안녕하세요.</span><br/><br/>";
		header += "<span style=\"color: #FF8000; font-weight: bold;\">알릭스</span><span>입니다.</span><br/><br/>";
		header += "상품을 클릭하시면 바로 구매하실 수 있습니다.<br/><br/>";
		
		String footer = "<br/><br/><span>기타 문의사항 등은 해당 메일로 보내시면 빠른 시일 내에 답변드리겠습니다^^</span><br/><br/>";
		footer += "<span>오늘도 좋은 하루 되세요!</span>";
		
		phrases.put("header", header);
		phrases.put("footer", footer);
		
		return phrases;
	}
	
	public Map<String, Object> createHtml(String keyword) throws ClassNotFoundException, MessagingException, ApiException, ParseException, UnsupportedEncodingException {
		Map<String, Object> map = new HashMap<String, Object>();
		
		String moreLink = aliExpress.linkGenerate(keyword);
		List<Properties> products = crawling.getContents(keyword);
		
		String content = "<h3 style=\"display: inline-block;\">'" + keyword + "' 키워드 로 검색한 결과</h3>";
				content += "<a href=\"" + moreLink + "\" style=\"margin-left: 5px;\">더 보기</a>";
		
		BodyPart messageBodyPart = null;
		
		String imagePath = Class.forName("com.app.aliexpress.MainApp").getResource("/images/no_result.png").getPath();
		imagePath = imagePath.substring(1);
		
		if (products.size() == 0) {
			content += "<img src=\"cid:no-result\">";
			messageBodyPart = new MimeBodyPart();
            DataSource fds = new FileDataSource(imagePath);
        
        	messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID","<no-result>");
				
					
		} else {
			content += "<div class=\"products_container\" style=\"max-width: fit-content;\">";
			
			for (Properties product : products) {
				
				String link = (String) product.get("link");
				link = aliExpress.linkGenerate(link);
				
				String image = (String) product.get("image");
				String title = (String) product.get("title");
				String price = (String) product.get("price");
				String shipping = (String) product.get("shipping");
				String rating = (String) product.get("rating");
				String saleVolume = (String) product.get("saleVolume");
				String store = (String) product.get("store");
				
				String lastest_volume = String.valueOf(product.get("lastest_volume"));
				
				content += "<div class=\"product_container\" style=\"display: inline-block;"
										+ "        vertical-align: top;" 
										+ "        width: 200px;"
										+ "        height: 300px;"
										+ "        padding: 0 10px 20px 10px;"
										+ "		   border: 1px solid #000;"
										+ "		   border-radius: 5px;\">";
				content += "<a style=\"text-decoration: none; color: #000;\" href=\"" + link + "\">";
				content += "<div class=\"product_box\" style=\"width: 100%;" 
										+ "        height: 100%;" 
										+ "        background: #fff;" 
										+ "        border-radius: 5px;\">";
				content += "<div class=\"img_box\" style=\"margin-top: 10px;" 
										+ "        width: 100%;" 
										+ "        height: 60%;" 
										+ "        text-align: center;\">";
				content += "<img src=\"" + image + "\""
										+ "style=\"max-width: 100%;" 
										+ "        max-height: 100%;\"/>";
				content += "</div>"; // img_box
				content += "<div class=\"title_box\" style=\"width: 100%;" 
										+ "        height: fit-content;" 
										+ "        white-space: nowrap;" 
										+ "        overflow: hidden;" 
										+ "        text-overflow: ellipsis;" 
										+ "        margin-top: 5px;"
										+ "        margin-left: 5px;"
										+ "        margin-right: 5px;\">";
//				content += "<div class=\"discount_icon\" style=\"float: left;"
//										+ "        background: #e72d01;" 
//										+ "        border-radius: 3px;" 
//										+ "        font-size: 9px;" 
//										+ "        text-align: center;" 
//										+ "        color: white;" 
//										+ "        width: 30px;"
//										+ visible + "\">세일</div>";
				content += "<span class=\"product_title\"" 
										+ "onmouseover=\"this.style.color= '#ff4747'\"" 
										+ "onmouseout=\"this.style.color= '#000'\""
										+ "style=\"width: 100%;" 
										//+ "        margin-left: 5px;" 
										+ "        font-size: 12px;" 
										+ "        line-height: 14px;\">"
										+ title 
										+ "</span>";
				content += "</div>"; // title_box
				content += "<div class=\"sale_price\" style=\"margin-left: 5px;" 
										+ "        font-weight: bold;" 
										+ "        letter-spacing: 0.03rem;\">" + price + "</div>";
//				content += "<div class=\"origin_price_box\" style=\"margin-left: 6px;\">";
//				content += "<span class=\"origin_price\" style=\"display: inline-block;" 
//										+ "        letter-spacing: 0.03rem;" 
//										+ "        font-size: 13px;" 
//										+ "        color: #999999;" 
//										+ "        text-decoration: line-through;"
//										+ visible + "\">$" + original_price +"</span>";
//				content += "<div class=\"discount_percent\" style=\"display: inline-block;" 
//										+ "        background: #ffe9e9;" 
//										+ "        border-radius: 2px;" 
//										+ "        font-weight: 600;" 
//										+ "        margin-left: 3px;" 
//										+ "        padding: 0 4px;" 
//										+ "        font-size: 12px;" 
//										+ "        color: #ff4747;"
//										+ visible + "\">-" + discount + "</div>";
//				content += "</div>"; // origin_price_box;
				content += "<div class=\"last_line\" style=\"margin: 10px 5px 0;\">";
				content += "<div class=\"evaluate_rate\">";
				content += "<span class=\"star_icon\" style=\"float: left;" 
										+ "        font-size: 15px;" 
										+ "        color: #ff4747;\">★</span>";
				content += "<span class=\"count\" style=\"float: left;" 
										+ "        margin-left: 3px;" 
										+ "        font-size: 13px;" 
										+ "        color: #999999;" 
										+ "        line-height: 23px;\">" + rating + "</span>";
				content += "</div>"; // evaluate_rate 
				content += "<div class=\"lastest_volume\" style=\"float: right;" 
										+ "        font-size: 13px;" 
										+ "        line-height: 25px;" 
										+ "        color: #999999;\">" + saleVolume + "</div>";
				content += "</div>"; // last_inline
				content += "</div>"; // product_box
				content += "</a>";
				content += "</div>"; // product_container
			}
			
			content += "</div>";
			
		}
		
		map.put("content", content);
		map.put("messageBodyPart", messageBodyPart);
		return map;
	}
	
	// getter, setter
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
