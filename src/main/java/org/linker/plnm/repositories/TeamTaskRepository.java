package org.linker.plnm.repositories;

import org.linker.plnm.domain.entities.TeamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface TeamTaskRepository extends JpaRepository<TeamTask, Long> {

    @Query("""
    SELECT COUNT(t) > 0
    FROM TeamTask t
    JOIN t.teams tm
    WHERE t.name = :taskName AND tm.id = :teamId
    """)
    boolean existsByNameAndTeamId(@Param("taskName") String taskName, @Param("teamId") Long teamId);

    @Query("""
    SELECT t FROM TeamTask t
    JOIN t.teams tm
    WHERE t.name = :taskName AND tm.id = :teamId\s
    """)
    Optional<TeamTask> getTaskByNameAndTeamId(@Param("taskName") String taskName, @Param("teamId") Long teamId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM team_tasks t WHERE t.team_id = :teamId", nativeQuery = true)
    void deleteTeamFromAllTasks(@Param("teamId") Long teamId);
}
