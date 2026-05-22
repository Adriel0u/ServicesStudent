package com.uees.studentservices.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker simple en memoria; tema base: /topic/course/{id}/forum
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijos para mensajes enviados POR el cliente
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para mensajes a un usuario en particular
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint con SockJS para compatibilidad de navegadores antiguos
        registry.addEndpoint("/ws-forum")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // Endpoint nativo WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
