package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.MemberDto;
import org.telegram.telegrambots.meta.api.objects.User;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TelegramUserBaseMapper extends BaseMapper<User, MemberDto> {}
