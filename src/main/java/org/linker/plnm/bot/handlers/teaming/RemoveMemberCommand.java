package org.linker.plnm.bot.handlers.teaming;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.DuplicateTeamMemberException;
import org.linker.plnm.exceptions.teaming.MemberNotFoundException;
import org.linker.plnm.exceptions.teaming.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.CommandHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.dtos.DtoBuilder;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.sessions.TeamActionSession;
import java.util.List;

@Service
public class RemoveMemberCommand implements CommandHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public RemoveMemberCommand(
            TeamService teamService,
            SessionCache sessionCache
    ) {
        this.teamService = teamService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.REMOVE_MEMBER;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (update.hasCallbackQuery()){
            String teamName = message.getText().split(" ", 2)[1].trim();
            if (!teamService.existsTeam(teamName, message.getChatId()))
                return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return askForUsernames(message, teamName);
        }
        sessionCache.remove(message);
        return removeMembers(message);
    }

    private BotApiMethod<?> removeMembers(Message message) {
        StringBuilder responseText = new StringBuilder();
        TeamDto teamDto = DtoBuilder.buildTeamDto(message);
        List<MemberDto> members = DtoBuilder.buildMemberDtoList(message);
        for (MemberDto memberDto : members) {
            try {
                teamService.removeMemberFromTeam(teamDto, memberDto);
                responseText.append(BotMessage.MEMBER_REMOVED_FROM_TEAM.format(memberDto.username())).append("\n\n");
            } catch (TeamNotFoundException | MemberNotFoundException | DuplicateTeamMemberException e) {
                responseText.append(e.getMessage()).append("\n\n");
            }
        }
        return MessageBuilder.buildMessage(message, responseText.toString());
    }

    private SendMessage askForUsernames(Message message, String teamName) {
        var session = TeamActionSession.builder()
                .command(BotCommand.REMOVE_MEMBER)
                .teamNames(List.of(teamName))
                .build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_USERNAMES.format());
    }
}
