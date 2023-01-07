package com.example.springbackend.service;

import com.example.springbackend.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import com.example.springbackend.dto.creation.UserCreationDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private Environment env;


    /*
     * Anotacija za oznacavanje asinhronog zadatka
     * Vise informacija na: https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#scheduling
     */
    @Async
    public void sendRegistrationEmail(Member member, String token) {
        HashMap<String, String> variables = new HashMap<>();
        variables.put("name", member.getUsername());
        variables.put("link", "http://localhost:8080/api/auth/confirm-registration/" + token);
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setText(buildEmailText(variables), true);
            helper.setTo("mrsisatim20@outlook.com");
            helper.setSubject("Registration Confirmation");
            helper.setFrom("mrsisatim20@outlook.com");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | IOException e) {
            System.out.println(e);
        }
    }

    private String buildEmailText(Map<String, String> variables) throws IOException {
        String message = "```html\n" +
                "<div style=\"background-image: linear-gradient(#ffffff,#ffffff); width: 100%\">\n" +
                "  <div style=\"background-image: linear-gradient(#e5eeee, #e5eeee); padding-bottom: 1px; color:#0b0c0c;\n" +
                "              font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto,\n" +
                "              Helvetica Neue, Arial, Noto Sans, sans-serif, Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji;\">\n" +
                "    <div style=\"margin: 20px;\">\n" +
                "\n" +
                "      <img src=\"{{ image_data }}\" width=\"80%\" alt=\"\"\n" +
                "           style=\"display: block; padding-top: 20px; margin-left: auto; margin-right: auto; max-width: 500px;\" />\n" +
                "\n" +
                "      <div style=\"text-align: center; padding-top: 20px; width: 100%\">\n" +
                "        <h1 style=\"margin-bottom: 5px;\">Hi {{ name }},</h1>\n" +
                "        <p style=\"padding: 0 20%; color: black;\">Thank you for completing your registration with us.</p>\n" +
                "        <p style=\"padding: 0 20%; margin-bottom: 0;\">Please confirm your account with the link below.</p>\n" +
                "\n" +
                "        <a href=\"{{ link }}\">\n" +
                "          <button style=\"display: inline; cursor: pointer; margin: 20px auto 20px; background-color: black;\n" +
                "                           color: white; width: 170px; padding: 10px; font-size: medium; border-radius: 8px;\">\n" +
                "            Proceed to website\n" +
                "          </button>\n" +
                "        </a>\n" +
                "        <p  style=\"padding: 0 20%; margin: 0;\">Suber team</p>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  </div>\n" +
                "</div>\n" +
                "```";

        String target;
        String value;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            target = "\\{\\{ " + entry.getKey() + " \\}\\}";
            value = entry.getValue();

            message = message.replaceAll(target, value);
        }
        return message;
    }

}
