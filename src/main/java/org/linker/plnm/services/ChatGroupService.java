package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.mappers.inherited.ChatGroupMapper;
import org.linker.plnm.exceptions.notfound.ChatGroupNotFoundException;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;

    private final ChatGroupMapper chatGroupMapper;

    public ChatGroupService(
            ChatGroupRepository chatGroupRepository,
            ChatGroupMapper chatGroupMapper) {
        this.chatGroupRepository = chatGroupRepository;
        this.chatGroupMapper = chatGroupMapper;
    }


    public ChatGroupDto findChatGroup(Long chatId) {
        var chatGroup = chatGroupRepository.findByChatId(chatId)
                .orElseThrow(ChatGroupNotFoundException::new);
        return chatGroupMapper.toDto(chatGroup);
    }


}
