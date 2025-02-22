package com.chatSystem.repository;

import com.chatSystem.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Additional custom queries can be defined here if needed.
    List<ChatMessage> findAllByOrderByTimestampAsc();

}