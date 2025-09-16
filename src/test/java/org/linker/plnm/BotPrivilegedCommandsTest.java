package org.linker.plnm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linker.plnm.bot.Bot;
import org.linker.plnm.bot.UpdateHandler;
import org.linker.plnm.configuration.BotSettings;
import org.linker.plnm.utilities.IOUtilities;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class BotPrivilegedCommandsTest {

    private final Bot bot;

    private final Environment environment;

    private User adminUser;

    private User normalUser;

    private Message message;

    private Chat chat;

    private Update update;

    @Autowired
    public BotPrivilegedCommandsTest(BotSettings botSettings, UpdateHandler updateHandler, Environment environment) {
        this.environment = environment;
        this.bot = spy(new Bot(botSettings, updateHandler));
    }

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(Long.valueOf(Objects.requireNonNull(environment.getProperty("telegram.privileged-user.id"))));
        adminUser.setFirstName(Objects.requireNonNull(environment.getProperty("telegram.privileged-user.firstname")));
        adminUser.setLastName(Objects.requireNonNull(environment.getProperty("telegram.privileged-user.lastname")));
        adminUser.setUserName(Objects.requireNonNull(environment.getProperty("telegram.privileged-user.username")));
        adminUser.setIsBot(false);

        normalUser = new User();
        normalUser.setId(Long.valueOf(Objects.requireNonNull(environment.getProperty("telegram.unprivileged-user.id"))));
        normalUser.setFirstName(Objects.requireNonNull(environment.getProperty("telegram.unprivileged-user.firstname")));
        normalUser.setLastName(Objects.requireNonNull(environment.getProperty("telegram.unprivileged-user.lastname")));
        normalUser.setUserName(Objects.requireNonNull(environment.getProperty("telegram.unprivileged-user.username")));
        normalUser.setIsBot(false);

        chat = new Chat();
        chat.setId(Long.valueOf(Objects.requireNonNull(environment.getProperty("telegram.test-chat-id"))));

        message = new Message();
        message.setChat(chat);

        update = new Update();
        update.setMessage(message);
    }


    @Test
    void testStartCommandReturnsCorrectResponse() throws Exception {
        update.getMessage().setText("/start");
        update.getMessage().setFrom(adminUser);
        bot.onUpdateReceived(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture());
        SendMessage response = captor.getValue();
        assertThat(response.getText()).isEqualTo(
            IOUtilities.readFile("static/botStart.html")
        );
    }

    @Test
    void testHintCommandReturnsCorrectResponse() throws Exception {
        update.getMessage().setText("/hint");
        update.getMessage().setFrom(adminUser);
        bot.onUpdateReceived(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture());
        SendMessage response = captor.getValue();
        assertThat(response.getText()).isEqualTo(
                IOUtilities.readFile("static/botStart.html")
        );
    }


    @Test
    void testTeamCanBeCreated() {
        update.getMessage().setText("/create_team");
        update.getMessage().setFrom(adminUser);
    }

}