package com.sju18.petmanagement.global.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationPushController {

    @RequestMapping("/send/token")
    public void sendByToken() throws FirebaseMessagingException {
        // This registration token comes from the client FCM SDKs.
        String registrationToken = "fBHfui-ZSBShvebtYPY7lu:APA91bE8W0ACTqwTevUZ5ZGM2_xbxYzy9WYZVz_9iIIGLK92mDviFJlIhpuFsXneqwJf686qFnJKEBKeGvEOlH97SSDe3PW5awI3SGsWYfoFtdjTdRTPLWXVEqlEv4XKbqEVbePjEjIU";

        // Set Notification
        Notification notification = Notification.builder()
                .setTitle("집사의 노트 알림")
                .setBody("집사의 노트 알림이 왔어요!")
                .build();

        // See documentation on defining a message payload.
                Message message = Message.builder()
                        .setNotification(notification)
                        .setToken(registrationToken)
                        .build();

        // Send a message to the device corresponding to the provided
        // registration token.
                String response = FirebaseMessaging.getInstance().send(message);

        // Response is a message ID string.
                System.out.println("Successfully sent message: " + response);
    }
}
