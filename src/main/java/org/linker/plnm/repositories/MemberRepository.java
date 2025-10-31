package org.linker.plnm.repositories;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.domain.entities.Member;
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
public interface MemberRepository extends JpaRepository<Member,Long> {

    @Query(value = "SELECT m FROM Member m WHERE m.userName in :userNames")
    List<Member> findAllByUserNames(@Param("userNames") List<String> userNames);

    Optional<Member> findByUserName(String userName);

    boolean existsByUserName(String userName);

    @Modifying @Transactional
    @Query(value = "DELETE FROM team_members WHERE team_id = :teamId", nativeQuery = true)
    void deleteAllByTeamId(@Param("teamId") Long teamId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM member_tasks WHERE task_id = :taskId", nativeQuery = true)
    void deleteAllByTaskId(@Param("taskId") Long taskId);

    boolean existsById(@NotNull Long id);

    @Query(value = "SELECT m.teams FROM Member m WHERE m.id = :memberId")
    Optional<List<Team>> getAllMemberTeams(@Param("memberId") Long memberId);
}
