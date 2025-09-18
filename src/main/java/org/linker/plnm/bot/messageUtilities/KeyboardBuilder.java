package org.linker.plnm.bot.messageUtilities;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class KeyboardBuilder {


    @NotNull
    public static InlineKeyboardButton buildButton(String text, String callBackData){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callBackData);
        return button;
    }
}
