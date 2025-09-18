package org.linker.plnm.repositories;

import jakarta.transaction.Transactional;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByNameAndChatGroupChatId(String name, Long chatGroupChatId);

    @Query("""
    SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END
    FROM Team t
    JOIN t.members m
    JOIN t.chatGroup g
    WHERE t.name = :name AND g.chatId = :chatId
    """)
    boolean teamHasMember(@Param("name") String name, @Param("chatId") Long chatGroupChatId);

    @Transactional
    void deleteTeamByNameAndChatGroupChatId(String name, Long chatGroupChatId);

    List<Team> findTeamByChatGroupChatId(Long chatGroupChatId);

    Optional<Team> findTeamByNameAndChatGroupChatId(String name, Long chatGroupChatId);

}
