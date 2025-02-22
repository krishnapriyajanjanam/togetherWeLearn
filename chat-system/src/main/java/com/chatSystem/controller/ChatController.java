package com.chatSystem.controller;

import com.chatSystem.model.ChatMessage;
import com.chatSystem.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    private static final String UPLOAD_DIR = "uploads/";


    // 1️⃣ **Send Text Message**
    @PostMapping("/send-text")
    public ResponseEntity<ChatMessage> sendTextMessage(@RequestParam("sender") String sender,
                                                       @RequestParam("content") String content) {
        ChatMessage message = new ChatMessage(sender, content, ChatMessage.MessageType.TEXT, null, ChatMessage.FileType.NONE, null);
        ChatMessage savedMessage = chatService.saveMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    // 2️⃣ **Send File (PDF, Audio)**
    @PostMapping("/send-file")
    public ResponseEntity<ChatMessage> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sender") String sender,
            @RequestParam("messageType") String messageTypeStr) {

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String newFileName = UUID.randomUUID() + fileExtension;

            // Determine file type
            ChatMessage.FileType fileType = ChatMessage.FileType.NONE;
            if (fileExtension.equalsIgnoreCase(".pdf")) {
                fileType = ChatMessage.FileType.PDF;
            } else if (fileExtension.equalsIgnoreCase(".mp3") || fileExtension.equalsIgnoreCase(".wav")) {
                fileType = ChatMessage.FileType.AUDIO;
            }

            // Save file
            Path path = Paths.get(UPLOAD_DIR + newFileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // Save message
            ChatMessage message = new ChatMessage(sender, "", ChatMessage.MessageType.valueOf(messageTypeStr.toUpperCase()), path.toString(), fileType, null);
            ChatMessage savedMessage = chatService.saveMessage(message);

            return ResponseEntity.ok(savedMessage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 3️⃣ **Retrieve All Messages**
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getAllMessages() {
        List<ChatMessage> messages = chatService.getAllMessages();
        return ResponseEntity.ok(messages);

    }


    // ✅ API to Start a Video Call (Different Links for Teachers & Students)
    @PostMapping("/start-video-call")
    public ResponseEntity<ChatMessage> startVideoCall(@RequestParam("sender") String sender,
                                                      @RequestParam("role") String role) {

        // ✅ Define meeting links
        String hostLink = "https://together-we-learn.whereby.com/all-handscb03d4a1-7736-45e8-9c4e-18958f85405f?roomKey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String participantLink = "https://together-we-learn.whereby.com/all-handscb03d4a1-7736-45e8-9c4e-18958f85405f";

        // ✅ Choose the correct link based on the role
        String meetLink;
        if ("teacher".equalsIgnoreCase(role)) {
            meetLink = hostLink;
        } else {
            meetLink = participantLink;  // Default: Students join as participants
        }

        // ✅ Create a chat message
        String messageContent = sender + " has started a video call. Click here: " + meetLink;

        ChatMessage message = new ChatMessage(
                sender,
                messageContent,
                ChatMessage.MessageType.TEXT,
                meetLink,
                ChatMessage.FileType.NONE,
                LocalDateTime.now()
        );

        ChatMessage savedMessage = chatService.saveMessage(message);

        // ✅ Return the correct link
        return ResponseEntity.ok(savedMessage);
    }

}
