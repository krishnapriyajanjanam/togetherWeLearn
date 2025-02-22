package com.chatSystem.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender; // Could be a username or user ID

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    // URL/path for the uploaded file or voice message (if any)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private FileType fileType; // Can be AUDIO, PDF, or NONE

    public enum MessageType {
        TEXT, FILE, AUDIO
    }

    public enum FileType {
        NONE, PDF, AUDIO
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructors, getters, and setters
    public ChatMessage() { }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public ChatMessage(String sender, String content, MessageType messageType, String fileUrl, FileType fileType, LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }



}
