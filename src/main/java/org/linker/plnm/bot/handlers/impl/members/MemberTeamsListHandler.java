package org.linker.plnm.bot.handlers.impl.members;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.inherited.TelegramUserMapper;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;

import java.util.List;

@Service
public class MemberTeamsListHandler implements UpdateHandler {

    private final TeamService teamService;

    private final TelegramUserMapper telegramUserMapper;

    private final TemplateEngine templateEngine;

    public MemberTeamsListHandler(
            TeamService teamService,
            TelegramUserMapper telegramUserMapper,
            TemplateEngine templateEngine) {
        this.teamService = teamService;
        this.telegramUserMapper = telegramUserMapper;
        this.templateEngine = templateEngine;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.MY_TEAMS;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        var memberDto = telegramUserMapper.toDto(message.getFrom());
        return getMemberTeams(memberDto, message);
    }

    /**
     * Listing member teams
     */
    private BotApiMethod<?> getMemberTeams(MemberDto memberDto, Message message) {
        List<TeamDto> teams;
        try {
            teams = teamService.getMemberTeams(memberDto, message.getChatId());
        } catch (MemberNotFoundException e) {
            return MessageBuilder.buildMessage(message, BotMessage.YOU_DID_NOT_STARTED.format());
        } catch (TeamNotFoundException e) {
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_FOUND.format());
        }
        Context context = new  Context();
        context.setVariable("teams", teams);
        String text = templateEngine.process("my_teams", context);
        return MessageBuilder.buildMessage(message, text, "HTML");
    }
}
