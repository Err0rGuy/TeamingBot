package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.entities.Member;

@org.mapstruct.Mapper(componentModel = "spring")
public interface MemberBaseMapper extends BaseMapper<Member, MemberDto> {
}
