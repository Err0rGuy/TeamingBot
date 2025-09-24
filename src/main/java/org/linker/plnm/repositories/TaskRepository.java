package org.linker.plnm.repositories;

import org.linker.plnm.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByName(String name);

    boolean existsByName(String name);
}
