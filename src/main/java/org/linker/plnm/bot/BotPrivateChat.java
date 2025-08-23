package org.linker.plnm.bot;


import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Service
public class BotPrivateChat {

    public SendMessage sendStartMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = """                
                💥 <b>Welcome to Teaming Bot!</b> 🚀
                
                This bot makes it easy to <b>organize and message teams</b> inside your group.
                
                ✨ <b>How it works:</b> \s
                
                1️⃣ Create teams for your group members. \s
                
                2️⃣ Mention a team with <code>~!teamname</code> in the group chat. \s
                
                3️⃣ All members of that team will get a <b>private message</b> 📩 with your message + a direct link 🔗 back to the group message.
                
                💡 <b>Example:</b> \s
                <pre>hello to ~!MyAmazingTeam team</pre> \s
                ➡️ All "data team" members receive your message instantly! \s
                
                ✅ Perfect for companies, projects, and communities that need <b>quick & private coordination</b>.
                """;

        message.setText(text);
        message.setParseMode("HTML");
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode("\uD83D\uDCA1Hint..."));
        button.setCallbackData("/hint");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);
        return message;
    }

    public SendMessage sendMoreDetails(Long chatId) {
        SendMessage message = new SendMessage();
        String hintText = """
                <b>Teaming Bot Commands</b>
                
                Here’s what you can do:
                
                📜 <b>/start</b> – Description. \s
                
                💡 <b>/hint</b> – Commands. \s
                
                🆕 <b>/create_team</b> – Create a new team. \s
                
                ❌ <b>/remove_team</b> – Remove an existing team. \s
                
                ✏️ <b>/edit_team</b> – Edit a team’s name, and members. \s
                
                📃 <b>/show_teams</b> – List all teams and groups. \s
                
                Good Luck 🙃
                """;
        message.setChatId(chatId.toString());
        message.setText(hintText);
        message.setParseMode("HTML");
        return message;
    }
}
