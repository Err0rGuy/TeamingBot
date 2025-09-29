package org.linker.plnm.bot.handlers.teaming;

import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.TelegramUserMapper;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.CommandHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

public class ShowTeamsCommand implements CommandHandler {

    private final TeamService teamService;

    private final TelegramUserMapper telegramUserMapper;

    private final TemplateEngine templateEngine;

    public ShowTeamsCommand(
            TeamService teamService,
            TelegramUserMapper telegramUserMapper,
            TemplateEngine templateEngine
    ) {
        this.teamService = teamService;
        this.telegramUserMapper = telegramUserMapper;
        this.templateEngine = templateEngine;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.SHOW_TEAMS;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        List<TeamDto> teams;
        try {
            teams = teamService.getAllGroupTeams(message.getChatId());
        } catch (TeamNotFoundException e) {
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_FOUND.format());
        }
        Context context = new  Context();
        context.setVariable("teams", teams);
        String text = templateEngine.process("show_teams", context);
        return MessageBuilder.buildMessage(message, text);
    }
}
