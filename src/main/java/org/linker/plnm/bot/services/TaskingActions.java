package org.linker.plnm.bot.services;

import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Service;

@Service
public class TaskingActions {

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;

    private final CacheUtilities<String, String> cacheUtilities;

    public TaskingActions(
            MemberRepository memberRepository,
            TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository,
            CacheUtilities<String, String> cacheUtilities
    ) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.cacheUtilities = cacheUtilities;
    }
}
