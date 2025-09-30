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
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import java.util.List;


@Service
public class AddMemberCommand implements CommandHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public AddMemberCommand(TeamService teamService, SessionCache sessionCache) {
        this.teamService = teamService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.ADD_MEMBER;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (update.hasCallbackQuery()){
            String teamName = message.getText().split(" ", 2)[1].trim();
            return askForUsernames(message, teamName);
        }
        sessionCache.remove(message);
        return addMembers(message);
    }

    private SendMessage askForUsernames(Message message, String teamName) {
        if (!teamService.existsTeam(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        var session = TeamActionSession.builder()
                .command(BotCommand.ADD_MEMBER)
                .teamNames(List.of(teamName))
                .build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_USERNAMES.format());
    }

    private BotApiMethod<?> addMembers(Message message) {
        StringBuilder responseText = new StringBuilder();
        TeamDto teamDto = DtoBuilder.buildTeamDto(message);
        List<MemberDto> membersDtoList = DtoBuilder.buildMemberDtoList(message);
        membersDtoList.forEach(memberDto -> {
            try {
                teamService.addMemberToTeam(teamDto, memberDto);
                responseText.append(BotMessage.MEMBER_ADDED_TO_TEAM.format(memberDto.userName())).append("\n\n");
            } catch (TeamNotFoundException e) {
                responseText.append(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name())).append("\n\n");
            } catch (MemberNotFoundException e) {
                responseText.append(BotMessage.MEMBER_HAS_NOT_STARTED.format(memberDto.displayName())).append("\n\n");
            } catch (DuplicateTeamMemberException e) {
                responseText.append(BotMessage.MEMBER_ALREADY_ADDED_TO_TEAM.format(memberDto.displayName())).append("\n\n");
            }
        });
        return MessageBuilder.buildMessage(message, responseText.toString());
    }
}
