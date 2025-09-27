package org.linker.plnm.repositories;

import org.linker.plnm.domain.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByUsername(String value);

    boolean existsByUsername(String username);

    @Modifying @Transactional
    @Query(value = "DELETE FROM team_members WHERE team_id = :teamId", nativeQuery = true)
    void deleteAllByTeamId(@Param("teamId") Long teamId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM member_tasks WHERE task_id = :taskId", nativeQuery = true)
    void deleteAllByTaskId(@Param("taskId") Long taskId);

}
