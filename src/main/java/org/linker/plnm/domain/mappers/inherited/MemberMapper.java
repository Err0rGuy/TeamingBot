package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.entities.Member;
import org.linker.plnm.domain.mappers.Mapper;

@org.mapstruct.Mapper(componentModel = "spring")
public interface MemberMapper extends Mapper<Member, MemberDto> {
}
