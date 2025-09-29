package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.entities.Member;

@org.mapstruct.Mapper(componentModel = "spring")
public interface MemberMapper extends Mapper<Member, MemberDto> {
}
