package com.sju18.petmanagement.global.firebase;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationPushService {
    public void sendToSingleDevice(String title, String body, String fcmRegistrationToken) throws FirebaseMessagingException {
        if(fcmRegistrationToken != null) {
            // 알림 객체 설정
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 메세지 객체 설정
            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmRegistrationToken)
                    .build();

            // 메세지 FCM 서버에 보내기
            String response = FirebaseMessaging.getInstance().send(message);

            // Response 받기
            System.out.println("Successfully sent message: " + response);
        }
        else {
            System.out.println("User fcmRegistrationToken is null");
        }
    }

    public void sendToMultipleDevice(String title, String body, List<String> fcmRegistrationTokens) throws FirebaseMessagingException {
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
