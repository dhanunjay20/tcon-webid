package com.tcon.webid.service;

public interface MailService {
    void sendSimpleMail(String to, String subject, String body);
    void sendHtmlMail(String to, String subject, String htmlBody);
}

