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

    boolean existsByNameAndChatGroup(String name, ChatGroup group);

    @Transactional
    void deleteTeamByNameAndChatGroup(String name, ChatGroup group);

    // ✅ Fetch teams WITH members in one query
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t.chatGroup = :chatGroup")
    List<Team> findTeamByChatGroup(@Param("chatGroup") ChatGroup chatGroup);

    Optional<Team> findTeamByNameAndChatGroup(String name, ChatGroup chatGroup);
}
