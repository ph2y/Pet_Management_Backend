package com.sju18.petmanagement.global.email;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final EmailVerifyService emailVerifyServ;
    private final Logger logger = LogManager.getLogger();

    // 메일인증 안내메일의 제목 및 내용 생성
    private MimeMessage createVerificationMessage(String to) throws Exception{
        String currentAuthCode = emailVerifyServ.createAuthCode(to);
        logger.info("보내는 대상 : " + to);
        logger.info("인증 번호 : " + currentAuthCode);
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to); //보내는 대상
        message.setSubject("집사의 노트 서비스 인증 코드: " + currentAuthCode); //제목

        String verificationMessage="";
        verificationMessage += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 10px 0 10px;\"><img src=\"https://avatars.githubusercontent.com/u/90550468?s=200&v=4\" width=\"60px\" height=\"60px\" loading=\"lazy\"></div>";
        verificationMessage += "<div style=\"border-bottom: 0.5px solid gray; padding-right: 30px; padding-left: 30px; margin: 10px 30px 10px 30px;\"></div>";
        verificationMessage += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">이메일 주소 확인</h1>";
        verificationMessage += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인 코드를 인증코드란에 입력하십시오.</p>";
        verificationMessage += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 10px 0 10px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
        verificationMessage += currentAuthCode;
        verificationMessage += "</td></tr></tbody></table></div>";
        verificationMessage += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">본 확인 코드는 10분간만 유효합니다.</p>";
        verificationMessage += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">확인 코드는 집사의 노트 서비스의 회원가입, 비밀번호 찾기 등에 사용되므로, 만약, 본인이 요청한 것이 아니라면 본 이메일을 무시하십시오.</p><br/>";
        verificationMessage += "<div style=\"border-bottom: 0.5px solid gray; padding-right: 30px; padding-left: 30px; margin: 10px 30px 10px 30px;\"></div>";
        verificationMessage += "<p style=\"font-size: 13px; color: gray; padding-right: 30px; padding-left: 30px;\">본 이메일은 발신전용입니다.</p>";
        verificationMessage += "<p style=\"font-size: 13px; color: gray; padding-right: 30px; padding-left: 30px;\">©PH2Y. All Rights Reserved.</p><br/>";

        message.setText(verificationMessage, "utf-8", "html"); //내용
        message.setFrom(new InternetAddress("ph2ydev@gmail.com","집사의 노트")); //보내는 사람

        return message;
    }

    // 인증 메시지 발송
    public void sendVerificationMessage(String to) throws Exception {
        MimeMessage message = createVerificationMessage(to);
        try{
            //예외처리
            emailSender.send(message);
            emailVerifyServ.deleteExpiredAuthCode();
        } catch(MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
    }
    
    // 임시비밀번호 안내메일의 제목 및 내용 생성
    private MimeMessage createTempPasswordNotifyMessage(String to, String tempPassword) throws Exception{
        logger.info("보내는 대상 : " + to);
        logger.info("임시 비밀번호 : " + tempPassword);

        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to); //보내는 대상
        message.setSubject("집사의 노트 비밀번호 찾기: 임시 비밀번호 통지"); //제목

        String tempPasswordNotifyMessage="";
        tempPasswordNotifyMessage += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 10px 0 10px;\"><img src=\"https://avatars.githubusercontent.com/u/90550468?s=200&v=4\" width=\"60px\" height=\"60px\" loading=\"lazy\"></div>";
        tempPasswordNotifyMessage += "<div style=\"border-bottom: 0.5px solid gray; padding-right: 30px; padding-left: 30px; margin: 10px 30px 10px 30px;\"></div>";
        tempPasswordNotifyMessage += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">임시 비밀번호 통지</h1>";
        tempPasswordNotifyMessage += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래의 임시 비밀번호를 사용하여 로그인 후 반드시 비밀번호를 변경하시기 바랍니다.</p>";
        tempPasswordNotifyMessage += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 10px 0 10px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
        tempPasswordNotifyMessage += tempPassword;
        tempPasswordNotifyMessage += "</td></tr></tbody></table></div>";
        tempPasswordNotifyMessage += "<div style=\"border-bottom: 0.5px solid gray; padding-right: 30px; padding-left: 30px; margin: 10px 30px 10px 30px;\"></div>";
        tempPasswordNotifyMessage += "<p style=\"font-size: 13px; color: gray; padding-right: 30px; padding-left: 30px;\">본 이메일은 발신전용입니다.</p>";
        tempPasswordNotifyMessage += "<p style=\"font-size: 13px; color: gray; padding-right: 30px; padding-left: 30px;\">©PH2Y. All Rights Reserved.</p><br/>";

        message.setText(tempPasswordNotifyMessage, "utf-8", "html"); //내용
        message.setFrom(new InternetAddress("ph2ydev@gmail.com","집사의 노트")); //보내는 사람

        return message;
    }

    // 임시비밀번호 메시지 발송
    public void sendTempPasswordNotifyMessage(String to, String tempPassword) throws Exception {
        MimeMessage message = createTempPasswordNotifyMessage(to, tempPassword);
        try{
            //예외처리
            emailSender.send(message);
        } catch(MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
    }
}
