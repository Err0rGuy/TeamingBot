package org.linker.plnm.bot.handlers.impl.teaming.members;

import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.DuplicateTeamMemberException;
import org.linker.plnm.exceptions.teaming.MemberNotFoundException;
import org.linker.plnm.exceptions.teaming.TeamNotFoundException;
import org.linker.plnm.services.MemberService;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;

import java.util.Arrays;
import java.util.List;


@Service
public class AddMemberUpdate implements UpdateHandler {

    private final TeamService teamService;

    private final MemberService memberService;

    private final SessionCache sessionCache;

    public AddMemberUpdate(
            TeamService teamService,
            MemberService memberService,
            SessionCache sessionCache) {
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
        String teamName;
        if (update.hasCallbackQuery()){
            teamName = String.valueOf(MessageParser.extractSecondPart(message.getText()));
            return askForUsernames(message, teamName);
        }
        var fetchedSession = sessionCache.fetch(message);
        if (fetchedSession.isEmpty())
            return null;
        teamName = fetchedSession.get().getTargets().getFirst();
        sessionCache.remove(message);
        return addMembers(message, teamName);
    }

    private SendMessage askForUsernames(Message message, String teamName) {
        if (!teamService.existsTeam(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        var session = TeamActionSession.builder()
                .command(BotCommand.ADD_MEMBER)
                .teamNames(List.of(teamName))
                .build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_USERNAMES.format(), "HTML");
    }

    private BotApiMethod<?> addMembers(Message message, String teamName) {
        var userNames = MessageParser.findUsernames(message.getText());
        if (userNames.length == 0)
            return MessageBuilder.buildMessage(message, BotMessage.NO_USERNAME_GIVEN.format());
        List<String> responses = Arrays.stream(userNames)
                .map(user -> processAddMember(user, teamName, message.getChatId()))
                .toList();
        return MessageBuilder.buildMessage(message, String.join("\n\n", responses));
    }


    private String processAddMember(String userName, String teamName, Long chatId) {
        try {
            var memberDto = memberService.findMemberByUserName(userName);
            teamService.addMemberToTeam(chatId, teamName, memberDto.id());
            return BotMessage.MEMBER_ADDED_TO_TEAM.format(memberDto.displayName());
        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName);
        } catch (MemberNotFoundException e) {
            return BotMessage.MEMBER_HAS_NOT_STARTED.format('@' + userName);
        } catch (DuplicateTeamMemberException e) {
            return BotMessage.MEMBER_ALREADY_ADDED_TO_TEAM.format('@' + userName);
        }
    }
}
