package com.cmps.ims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendShippingNotification(String toEmail, String companyName, String productName,
            Integer quantity, Integer billingAmount, String sendDate) {

        SpringTemplateEngine engine = new SpringTemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setCharacterEncoding("UTF-8");
        engine.setTemplateResolver(templateResolver);

        Map<String, Object> datas = new HashMap<>();
        datas.put("companyName", companyName);
        datas.put("productName", productName);
        datas.put("quantity", quantity);
        datas.put("billingAmount", billingAmount);
        datas.put("sendDate", sendDate);

        Context context = new Context();
        context.setVariables(datas);

        String htmlBody = engine.process("/templates/mail/shipping.html", context);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setSubject("発送完了のお知らせ");
            helper.setText(htmlBody, true);
            javaMailSender.send(mimeMessage);
            log.info("発送完了メール送信成功: to={}", toEmail);
        } catch (Exception e) {
            log.error("発送完了メール送信失敗: to={}", toEmail, e);
            // メール送信失敗は発送処理自体を失敗させない（在庫・statusは既に確定済みのため）
        }
    }
}