package com.zklcsoftware.aimodel.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;


@Component
@Slf4j
public class WebSocketDisconnectHandler<S> implements ApplicationListener<SessionDisconnectEvent> {
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessAttrs = sha.getSessionAttributes();
        log.info("连接断开:{}, {}", event, sessAttrs);
    }
}
