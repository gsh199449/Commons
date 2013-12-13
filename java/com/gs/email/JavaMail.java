package com.gs.email;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class JavaMail {
	// ���÷�����
	private static String KEY_SMTP = "mail.smtp.host";
	private static String VALUE_SMTP = "smtp.qq.com";
	// ��������֤
	private static String KEY_PROPS = "mail.smtp.auth";
	private boolean VALUE_PROPS = true;
	// �������û�������
	private String SEND_USER = "574361375@qq.com";
	private String SEND_UNAME = "574361375@qq.com";
	private String SEND_PWD = "";
	// �����Ự
	private MimeMessage message;
	private Session s;

	public JavaMail() {
	}

	public boolean doSendHtmlEmail(String headName, String sendHtml,
			String receiveUser) {

		Properties props = System.getProperties();
		props.setProperty(KEY_SMTP, VALUE_SMTP);
		props.setProperty("mail.smtp.auth", "true");
		s = Session.getInstance(props);
		message = new MimeMessage(s);

		boolean b = true;
		try {
			// ������
			InternetAddress from = new InternetAddress(SEND_USER);
			message.setFrom(from);
			// �ռ���
			InternetAddress to = new InternetAddress(receiveUser);
			message.setRecipient(Message.RecipientType.TO, to);
			// �ʼ�����
			message.setSubject(headName);
			String content = sendHtml.toString();
			// �ʼ�����,Ҳ����ʹ���ı�"text/plain"
			message.setContent(content, "text/html;charset=GBK");
			message.saveChanges();
			Transport transport = s.getTransport("smtp");
			// smtp��֤���������������ʼ��������û�������
			transport.connect(VALUE_SMTP, SEND_UNAME, SEND_PWD);
			// ����
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			
		} catch (AddressException e) {
			e.printStackTrace();
			b = false;
		} catch (MessagingException e) {
			e.printStackTrace();
			b = false;
		}
		return b;
	}

}
