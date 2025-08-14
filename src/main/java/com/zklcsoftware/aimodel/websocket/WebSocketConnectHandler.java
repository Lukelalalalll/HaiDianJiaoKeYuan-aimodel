package com.zklcsoftware.aimodel.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.Map;

@Component
@Slf4j
public class WebSocketConnectHandler<S> implements ApplicationListener<SessionConnectEvent> {

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessAttrs = sha.getSessionAttributes();
        log.info("新建立连接:{}, {}", event, sessAttrs);
    }
}
