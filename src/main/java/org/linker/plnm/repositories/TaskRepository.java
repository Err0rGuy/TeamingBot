package org.linker.plnm.repositories;

import org.linker.plnm.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByName(String name);

    boolean existsByName(String name);

    @Modifying @Transactional
    @Query(value = "DELETE FROM team_tasks WHERE team_id = :teamId", nativeQuery = true)
    void deleteAllByTeamId(@Param("teamId") Long teamId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM member_tasks WHERE member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);

}
