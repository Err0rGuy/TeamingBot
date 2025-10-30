package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.inherited.ChatGroupMapper;
import org.linker.plnm.domain.mappers.inherited.TeamMapper;
import org.linker.plnm.exceptions.notfound.ChatGroupNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;

    private final TeamMapper teamMapper;

    public ChatGroupService(
            ChatGroupRepository chatGroupRepository,
            TeamMapper teamMapper
    ) {
        this.chatGroupRepository = chatGroupRepository;
        this.teamMapper = teamMapper;
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getAllGroupTeams(Long chatId) {
        var teams = chatGroupRepository.getAllTeamsById(chatId)
                .orElseThrow(ChatGroupNotFoundException::new);

        if (teams.isEmpty()) {
            throw new TeamNotFoundException();
        }
        return teamMapper.toDtoList(teams);
    }
}
