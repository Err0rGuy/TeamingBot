package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.entities.Member;
import org.linker.plnm.domain.mappers.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@org.mapstruct.Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface MemberMapper extends Mapper<Member, MemberDto> {}
