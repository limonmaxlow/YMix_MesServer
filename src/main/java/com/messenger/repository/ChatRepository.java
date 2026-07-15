package com.messenger.repository;

import com.messenger.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // Найти приватный чат между двумя конкретными пользователями
    @Query("""
            select c from Chat c
            where c.type = com.messenger.domain.ChatType.PRIVATE
              and c.id in (select cp.chat.id from ChatParticipant cp where cp.user.id = :userAId)
              and c.id in (select cp.chat.id from ChatParticipant cp where cp.user.id = :userBId)
            """)
    Optional<Chat> findPrivateChatBetween(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    // Список чатов пользователя, отсортированный по времени последнего сообщения
    @Query("""
            select c from Chat c
            where c.id in (select cp.chat.id from ChatParticipant cp where cp.user.id = :userId)
            order by coalesce(c.lastMessageAt, c.createdAt) desc
            """)
    List<Chat> findAllForUser(@Param("userId") Long userId);
}
