package com.zklcsoftware.aimodel.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;

@Component
@Slf4j
public class WebSocketUnsubscribeHandler<S> implements ApplicationListener<SessionUnsubscribeEvent> {
    @Override
    public void onApplicationEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();
        if (destination == null) {
            return;
        }
        String[] parts = destination.split("/");
        if (parts.length != 3) {
            return;
        }
        String destType = parts[1];
        String roomId = parts[2];

        Map<String, Object> sessAttrs = sha.getSessionAttributes();
        sessAttrs.remove("roomId_" + roomId);
        sessAttrs.remove("subid_" + sha.getSubscriptionId());
        sha.setSessionAttributes(sessAttrs);
        log.info("取消消息订阅{}, {}, {}", roomId, destType, event);
    }

}