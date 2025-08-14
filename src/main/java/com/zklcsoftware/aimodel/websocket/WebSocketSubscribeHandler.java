package com.zklcsoftware.aimodel.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;

@Component
@Slf4j
public class WebSocketSubscribeHandler<S> implements ApplicationListener<SessionSubscribeEvent> {
    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();
        if (destination == null) {
            return;
        }
        String[] parts = destination.split("/");
        // /channel/roomId or /liveusers/roomId
        /*if (parts.length != 3) {
            return;
        }*/
        // channel or liveusers
        String destType = parts[1];
        String roomId = parts[2];

        Map<String, Object> sessAttrs = sha.getSessionAttributes();
        sessAttrs.put("roomId_" + roomId, roomId);
        sessAttrs.put("subid_" + sha.getSubscriptionId(), destType); // 把当前订阅的id对应到订阅类型，保存在session中
        sha.setSessionAttributes(sessAttrs); // 保存session
        log.info("订阅消息通道:{}, {}, {}","subid_" + sha.getSubscriptionId(), destType, event);
    }

}
