package com.chatbotSystem.model;


import jakarta.validation.constraints.NotBlank;

public class ChatbotRequest {

    private String user;

    @NotBlank(message = "Query cannot be empty")
    private String query;

    public ChatbotRequest() {}

    public ChatbotRequest(String user, String query) {
        this.user = user;
        this.query = query;
    }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
