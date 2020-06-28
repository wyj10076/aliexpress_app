package com.app.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.app.dto.UserDTO;

public class MailSender {

	private Session session;
	private String email;
	private String password;
	private Transport transport;
	
	public MailSender() {
	}

	public MailSender(String platform, String email, String password) throws Exception {
		Properties mailProps = new Properties();
		
		// Step1
		this.email = email;
		this.password = password;
		
		if (platform.equals("gmail")) {
			mailProps.put("mail.smtp.host", "smtp.gmail.com");
		} else if (platform.equals("naver")) {
			mailProps.put("mail.smtp.host", "smtp.naver.com");
		}
		mailProps.put("mail.smtp.port", "587");
		mailProps.put("mail.smtp.auth", "true");
		mailProps.put("mail.smtp.starttls.enable", "true");

		// Step2
		this.session = Session.getDefaultInstance(mailProps, null);
		
		// Step3
		this.transport = this.session.getTransport("smtp");

		// login test
		connect();
		if (transport.isConnected()) {
			disconnect();
		}
	}
	
	public void connect() throws Exception {
		// Enter your correct gmail UserID and Password
		this.transport.connect(this.email, this.password);
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
