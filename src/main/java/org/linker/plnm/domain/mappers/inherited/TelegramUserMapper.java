package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.mappers.Mapper;
import org.telegram.telegrambots.meta.api.objects.User;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TelegramUserMapper extends Mapper<User, MemberDto> {}
