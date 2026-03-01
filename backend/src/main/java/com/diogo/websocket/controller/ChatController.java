package com.diogo.websocket.controller;

import com.diogo.websocket.controller.dto.ChatMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/join")
    public void joinRoom(ChatMessage chatMessage, org.springframework.messaging.simp.stomp.StompHeaderAccessor headerAccessor) {
        sessionRoomMap.put(headerAccessor.getSessionId(), chatMessage.getRoomId());

        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                new ChatMessage("Sistema", chatMessage.getSender() + " entrou na sala!")
        );
    }

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                chatMessage
        );
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String roomId = sessionRoomMap.get(sessionId);
        if (roomId != null) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId,
                    new ChatMessage("Sistema", "Usuário saiu!")
            );
            sessionRoomMap.remove(sessionId);
        }
    }
}