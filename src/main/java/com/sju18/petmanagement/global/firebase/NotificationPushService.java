package com.sju18.petmanagement.global.firebase;

import com.google.firebase.messaging.*;
import com.sju18.petmanagement.domain.account.dao.Account;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationPushService {
    public void sendToSingleDevice(String title, String body, Account pushSubjectAccount) throws FirebaseMessagingException {
        // FCM 토큰이 존재하고 알림이 ON 인 경우에만 알림 보내기
        if(pushSubjectAccount.getFcmRegistrationToken() != null && pushSubjectAccount.getNotification()) {
            // 알림 객체 설정
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 메세지 객체 설정
            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(pushSubjectAccount.getFcmRegistrationToken())
                    .build();

            // 메세지 FCM 서버에 보내기
            String response = FirebaseMessaging.getInstance().send(message);

            // Response 받기
            System.out.println("Successfully sent message: " + response);
        }
    }

    public void sendToMultipleDevice(String title, String body, List<Account> pushSubjectAccounts) throws FirebaseMessagingException {
        // FCM 토큰이 존재하고 알림이 ON 인 유저들의 FCM 토큰만 가져오기
        List<String> fcmRegistrationTokens = pushSubjectAccounts.stream()
                .filter(account -> account.getFcmRegistrationToken() != null)
                .filter(Account::getNotification)
                .map(Account::getFcmRegistrationToken)
                .collect(Collectors.toList());

        // 알림 객체 설정
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // 멀티캐스트 메세지 객체 설정
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(fcmRegistrationTokens)
                .build();

        // 메세지 FCM 서버에 보내기
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

        // 성공적으로 보낸 메시지 출력
        System.out.println(response.getSuccessCount() + " messages were sent successfully");

        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();

            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    failedTokens.add(fcmRegistrationTokens.get(i));
                }
            }
            // 전송 실패한 토큰들 출력
            System.out.println("List of tokens that caused failures: " + failedTokens);
        }
    }
}
