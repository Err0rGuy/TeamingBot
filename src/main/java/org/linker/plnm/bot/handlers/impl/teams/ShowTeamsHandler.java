package org.linker.plnm.bot.handlers.impl.teams;

import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.notfound.ChatGroupNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.ChatGroupService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class ShowTeamsHandler implements UpdateHandler {

    private final ChatGroupService chatGroupService;

    private final TemplateEngine templateEngine;

    public ShowTeamsHandler(
            ChatGroupService chatGroupService,
            TemplateEngine templateEngine) {
        this.chatGroupService = chatGroupService;
        this.templateEngine = templateEngine;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.SHOW_TEAMS;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        return  fetchAllGroupTeams(message);
    }

    /**
     * Listing all existing teams with their members
     */
    private BotApiMethod<?> fetchAllGroupTeams(Message message) {
        List<TeamDto> teams;
        try {
            teams = chatGroupService.getAllGroupTeams(message.getChatId());
        } catch (ChatGroupNotFoundException|TeamNotFoundException e) {
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_FOUND.format());
        }
        Context context = new Context();
        context.setVariable("teams", teams);
        String text = templateEngine.process("show_teams", context);
        return MessageBuilder.buildMessage(message, text, MessageParseMode.HTML);

    }
}
