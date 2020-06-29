package com.app.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.app.dto.UserDTO;

public class MailSender {

	private Session session;
	// 로그인 계정
	private String email; 
	private String password;
	// 발송 이메일
	private String address;
	private Transport transport;
	
	public MailSender() {
	}

	public MailSender(String platform, String email, String password) throws Exception {
		Properties mailProps = new Properties();
		
		// Step1
		this.email = email;
		this.password = password;
		this.address = platform.equals("naver") ? email + "@naver.com" : email;
		
		mailProps.put("mail.smtp.host", "smtp." + platform + ".com");
		mailProps.put("mail.smtp.port", "587");
		mailProps.put("mail.smtp.auth", "true");
		mailProps.put("mail.smtp.starttls.enable", "true");

		// Step2
		this.session = Session.getDefaultInstance(mailProps);
		
		// Step3
		this.transport = this.session.getTransport("smtp");

		// login test
		connect();
		if (transport.isConnected()) {
			disconnect();
		}
	}
	
	public void connect() {
		// Enter your correct gmail UserID and Password
		try {
			this.transport.connect(this.email, this.password);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect() throws Exception {
		this.transport.close();
	}

	public void sendMessage(UserDTO recipient, Map<String, List<Properties>> processedData) throws Exception {
		
		MimeMessage message = getMessage(recipient, processedData);
		
		transport.sendMessage(message, message.getAllRecipients());
		
	}
	
	public MimeMessage getMessage(UserDTO recipient, Map<String, List<Properties>> processedData) throws Exception {
		
		String email = recipient.getEmail();
		String firstKeyword =  recipient.getFirstKeyword();
		String secondKeyword = recipient.getSecondKeyword();
		
		MimeMessage message = new MimeMessage(this.session);
		message.setFrom(new InternetAddress(this.address));
		
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		message.setSubject("오늘의 알리 익스프레스 맞춤 추천 < " + firstKeyword + (!secondKeyword.equals("") ? ", " + secondKeyword : "") + " >");

		MimeMultipart multipart = new MimeMultipart("related");
		
		Properties phrases = Editor.getPhrases();
		String header = (String) phrases.get("header");
		String footer = (String) phrases.get("footer");
		
		Map<String, Object> result1 = Editor.createHtml(EditorType.SEND, firstKeyword, processedData);
		Map<String, Object> result2 = !secondKeyword.equals("") ? Editor.createHtml(EditorType.SEND, secondKeyword, processedData) : null;
		
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
	
	// getter, setter
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
