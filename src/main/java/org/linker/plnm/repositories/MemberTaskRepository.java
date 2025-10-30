package org.linker.plnm.repositories;

import org.linker.plnm.domain.entities.MemberTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTaskRepository extends JpaRepository<MemberTask, Long> {
}
