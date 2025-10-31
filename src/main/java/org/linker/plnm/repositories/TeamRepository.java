package org.linker.plnm.repositories;

import org.linker.plnm.domain.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByNameAndChatGroupChatId(String name, Long chatGroupChatId);

    @Query("""
    SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END
    FROM Team t
    JOIN t.members m
    JOIN t.chatGroup g
    WHERE t.name = :name AND g.chatId = :chatId
    """)
    boolean teamHasMember(@Param("name") String name, @Param("chatGroupId") Long chatGroupChatId);

    @Transactional
    void deleteTeamByNameAndChatGroupChatId(String name, Long chatGroupChatId);

    Optional<Team> findTeamByNameAndChatGroupChatId(String name, Long chatGroupChatId);

    @Query(value = "SELECT t FROM Team t WHERE t.name in :teamNames AND t.chatGroup.chatId = :chatId")
    List<Team> getAllTeamsByNameAndChatId(@Param("teamNames") List<String> teamNames, @Param("chatId") Long chatId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM teams_members WHERE member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM teams_tasks WHERE task_id = :taskId", nativeQuery = true)
    void deleteAllByTaskId(@Param("taskId") Long taskId);


    List<Team> findAllByChatGroupChatId(Long chatGroupChatId);

    boolean existsAllByChatGroupChatId(Long chatGroupChatId);
}
