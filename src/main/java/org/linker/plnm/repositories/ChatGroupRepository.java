package org.linker.plnm.repositories;

import org.linker.plnm.entities.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatGroupRepository extends JpaRepository<ChatGroup,Long> {
    Optional<ChatGroup> findByChatId(Long chatId);

    @Query("SELECT g.name FROM ChatGroup g WHERE g.chatId = :chatId")
    String findNameByChatId(Long chatId);
}
