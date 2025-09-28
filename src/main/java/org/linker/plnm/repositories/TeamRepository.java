package org.linker.plnm.repositories;

import org.linker.plnm.domain.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying @Transactional
    @Query(value = "DELETE FROM team_members WHERE member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM team_tasks WHERE task_id = :taskId", nativeQuery = true)
    void deleteAllByTaskId(@Param("taskId") Long taskId);

    Optional<List<Team>> findAllByChatGroup_ChatId(Long chatGroupChatId);
}
