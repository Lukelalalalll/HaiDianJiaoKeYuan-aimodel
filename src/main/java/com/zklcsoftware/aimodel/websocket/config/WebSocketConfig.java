package com.zklcsoftware.aimodel.websocket.config;



import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.service.TAiModelService;
import com.zklcsoftware.aimodel.websocket.WebSocketAppInterceptor;
import com.zklcsoftware.aimodel.websocket.WebSocketHandshakeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfig  extends AbstractSessionWebSocketMessageBrokerConfigurer {
    @Autowired
    private WebSocketAppInterceptor webSocketAppInterceptor;
    @Autowired
    private WebSocketHandshakeHandler webSocketHandshakeHandler;
    @Autowired
    TAiModelService tAiModelService;
    @Override
    protected void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*").withSockJS();
        registry.addEndpoint("/ws_app")
                .addInterceptors(webSocketAppInterceptor)
                .setHandshakeHandler(webSocketHandshakeHandler)
                .setAllowedOrigins("*");
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        //多节点部署时，启用消息代理机制 实现共享
        /*registry
                .enableStompBrokerRelay("/chatmessage", "/promptmessage","/systemmessage", "/user")
                .setVirtualHost("") //对应自己rabbitmq里的虚拟host
                .setRelayHost("")//对应自己rabbitmq地址
                .setClientLogin("root")//分配给客户端
                .setClientPasscode("root")//分配给客户端
                .setSystemLogin("root")//分配给服务端
                .setSystemPasscode("root")//分配给服务端
                .setSystemHeartbeatSendInterval(5000)
                .setSystemHeartbeatReceiveInterval(4000);*/

        List<TAiModel> aiModelList=tAiModelService.findAll();
        String[] arrs=new String[aiModelList.size()+1];
        for (int i = 0; i < aiModelList.size(); i++) {
            arrs[i]="/chatmessage"+aiModelList.get(i).getId();
        }
        arrs[aiModelList.size()]= "/user";
        registry.enableSimpleBroker(arrs);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");

    }

}