package org.linker.plnm.bot.helpers.validation;

import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.services.MemberService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component @Slf4j
public class MemberValidators {

    private final MemberService memberService;

    public MemberValidators(MemberService memberService) {
        this.memberService = memberService;
    }

    public String validateMemberExistence(List<String> userNames) {
        List<String> responseTxt = new ArrayList<>();
        if (userNames.isEmpty())
            return BotMessage.NO_USERNAME_GIVEN.format();

        for (String userName : userNames)
            if (memberService.memberNotExists(userName))
                responseTxt.add(BotMessage.MEMBER_HAS_NOT_STARTED.format(userName));

        return String.join("\n\n", responseTxt);
    }
}
