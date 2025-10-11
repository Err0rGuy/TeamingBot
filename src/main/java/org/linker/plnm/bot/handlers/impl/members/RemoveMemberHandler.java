package org.linker.plnm.bot.handlers.impl.members;

import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.duplication.DuplicateTeamMemberException;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamMemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.MemberService;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;

import java.util.List;
import java.util.stream.Stream;

@Service
public class RemoveMemberHandler implements UpdateHandler {

    private final TeamService teamService;

    private final SessionCache<TeamDto> sessionCache;
    private final MemberService memberService;

    public RemoveMemberHandler(
            TeamService teamService,
            SessionCache<TeamDto> sessionCache,
            MemberService memberService) {
        this.teamService = teamService;
        this.sessionCache = sessionCache;
        this.memberService = memberService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.REMOVE_MEMBER;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();

        if (update.hasCallbackQuery()){
            String teamName = MessageParser.extractSecondPart(message.getText()).orElse("");
            return promptForUsernames(message, teamName);
        }

        var sessionOpt = sessionCache.fetch(message);
        if (sessionOpt.isEmpty()) return null;

        TeamDto team = sessionOpt.get().getTargets().getFirst();
        sessionCache.remove(message);

        return handleRemoveMembers(message, team);
    }

    /**
     * Asking for members usernames
     */
    private SendMessage promptForUsernames(Message message, String teamName) {
        if (!teamService.teamExists(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(
                    message,
                    BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName)
            );

        var team = teamService.findTeam(teamName, message.getChatId());
        var session = TeamActionSession.builder()
                .command(BotCommand.REMOVE_MEMBER)
                .teams(List.of(team))
                .build();

        sessionCache.add(message, session);

        return MessageBuilder.buildMessage(
                message,
                BotMessage.ASK_FOR_USERNAMES.format(),
                MessageParseMode.HTML
        );
    }

    /**
     * Removing members from specific team
     */
    private BotApiMethod<?> handleRemoveMembers(Message message, TeamDto teamDto) {
        var usernames = MessageParser.findUsernames(message.getText());

        if (usernames.length == 0) {
            return MessageBuilder.buildMessage(message, BotMessage.NO_USERNAME_GIVEN.format());
        }
        List<String> results =
                Stream.of(usernames)
                        .map(user -> tryRemoveMember(user, teamDto))
                        .toList();
        return MessageBuilder.buildMessage(
                message,
                String.join("\n\n", results)
        );
    }

    /**
     * Removing member from team
     */
    private String tryRemoveMember(String userName, TeamDto teamDto) {
        try {
            var memberDto = memberService.findMember(userName);
            teamService.removeTeamMember(
                    teamDto.chatGroup().chatId(), teamDto.name(), memberDto.id()
            );
            return BotMessage.MEMBER_REMOVED_FROM_TEAM.format(memberDto.displayName());

        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name());

        } catch (DuplicateTeamMemberException e) {
            return BotMessage.MEMBER_ALREADY_ADDED_TO_TEAM.format('@' + userName);

        } catch (TeamMemberNotFoundException | MemberNotFoundException e) {
            return BotMessage.MEMBER_HAS_NOT_BEEN_ADDED_TO_TEAM.format('@' + userName);
        }
    }
}
