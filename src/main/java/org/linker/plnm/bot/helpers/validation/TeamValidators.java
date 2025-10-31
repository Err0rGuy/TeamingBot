package org.linker.plnm.bot.helpers.validation;

import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Component @Slf4j
public class TeamValidators {

    private final TeamService teamService;

    public TeamValidators(TeamService teamService) {
        this.teamService = teamService;
    }

    public String validateTeamsExistence(List<String> teamNames, Long chatId) {
        List<String> responseTxt = new ArrayList<>();

        if (teamNames.isEmpty())
            return BotMessage.NO_TEAM_NAME_GIVEN.format();

        for (String teamName : teamNames)
            if (teamService.teamNotExists(teamName, chatId))
                responseTxt.add(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));

        return String.join("\n\n", responseTxt);
    }

}
