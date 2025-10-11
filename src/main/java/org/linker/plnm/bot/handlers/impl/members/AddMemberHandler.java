package org.linker.plnm.bot.handlers.impl.members;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.duplication.DuplicateTeamMemberException;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.MemberService;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Stream;

@Service
public class AddMemberHandler implements UpdateHandler {

    private final TeamService teamService;
    private final MemberService memberService;
    private final SessionCache<TeamDto> sessionCache;

    public AddMemberHandler(TeamService teamService,
                            MemberService memberService,
                            SessionCache<TeamDto> sessionCache) {
        this.teamService = teamService;
        this.memberService = memberService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.ADD_MEMBER;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();

        if (update.hasCallbackQuery()) {
            String teamName = MessageParser.extractSecondPart(message.getText()).orElse("");
            return promptForUsernames(message, teamName);
        }

        var sessionOpt = sessionCache.fetch(message);
        if (sessionOpt.isEmpty()) return null;

        TeamDto team = sessionOpt.get().getTargets().getFirst();
        sessionCache.remove(message);

        return handleAddMembers(message, team);
    }

    /**
     * Step 1: Ask user to provide usernames to add.
     */
    private SendMessage promptForUsernames(Message message, String teamName) {
        if (!teamService.teamExists(teamName, message.getChatId())) {
            return MessageBuilder.buildMessage(
                    message,
                    BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName)
            );
        }

        TeamDto team = teamService.findTeam(teamName, message.getChatId());
        var session = TeamActionSession.builder()
                .command(BotCommand.ADD_MEMBER)
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
     * Step 2: Parse usernames and try to add them to the team.
     */
    private BotApiMethod<?> handleAddMembers(Message message, TeamDto team) {
        String[] usernames = MessageParser.findUsernames(message.getText());
        if (usernames.length == 0) {
            return MessageBuilder.buildMessage(message, BotMessage.NO_USERNAME_GIVEN.format());
        }

        List<String> results =
                Stream.of(usernames)
                        .map(username -> tryAddMember(username, team.name(), message.getChatId()))
                        .toList();

        return MessageBuilder.buildMessage(
                message,
                String.join("\n\n", results)
        );
    }

    /**
     * Step 3: Try to add a single member to a team, returning result text.
     */
    private String tryAddMember(String username, String teamName, Long chatId) {
        try {
            var member = memberService.findMember(username);
            teamService.addTeamMember(chatId, teamName, member.id());
            return BotMessage.MEMBER_ADDED_TO_TEAM.format(member.displayName());

        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName);

        } catch (MemberNotFoundException e) {
            return BotMessage.MEMBER_HAS_NOT_STARTED.format('@' + username);

        } catch (DuplicateTeamMemberException e) {
            return BotMessage.MEMBER_ALREADY_ADDED_TO_TEAM.format('@' + username);
        }
    }
}
