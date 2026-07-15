package com.messenger.repository;

import com.messenger.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatIdOrderBySentAtDesc(Long chatId, Pageable pageable);

    long countByChatIdAndIdGreaterThan(Long chatId, Long id);

    long countByChatId(Long chatId);
}
