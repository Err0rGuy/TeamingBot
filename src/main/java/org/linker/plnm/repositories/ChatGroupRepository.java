package org.linker.plnm.repositories;

import org.linker.plnm.domain.entities.ChatGroup;
import org.linker.plnm.domain.entities.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup,Long> {
    Optional<ChatGroup> findByChatId(Long chatId);

    @Query("SELECT g.name FROM ChatGroup g WHERE g.chatId = :chatId")
    String findNameByChatId(Long chatId);

    @Query(value = "SELECT ch.teams FROM ChatGroup ch WHERE ch.chatId = :chatId")
    Optional<List<Team>> getAllTeamsById(@Param("chatId") Long chatId);
}
