package org.linker.plnm.repositories;

import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByUsername(String value);
}
