package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBuilder {


    @NotNull
    public static InlineKeyboardButton buildButton(String text, String callBackData){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callBackData);
        return button;
    }

    public static List<List<InlineKeyboardButton>> buildRows(InlineKeyboardButton... buttons){
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for(InlineKeyboardButton button : buttons)
            rows.add(List.of(button));
        return rows;
    }

    public static InlineKeyboardMarkup buildVerticalMenu(InlineKeyboardButton... buttons) {
        var rows = KeyboardBuilder.buildRows(buttons);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
