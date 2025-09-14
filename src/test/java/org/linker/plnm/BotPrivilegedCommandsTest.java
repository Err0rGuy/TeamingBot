package org.linker.plnm;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linker.plnm.bot.Bot;
import org.linker.plnm.bot.UpdateHandler;
import org.linker.plnm.configuration.BotSettings;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BotPrivilegedCommandsTest {

    private BotSettings botSettings;
    private DefaultBotOptions defaultBotOptions;
    private UpdateHandler updateHandler;
    private Bot bot;

    @BeforeEach
    void setUp() {
        botSettings = mock(BotSettings.class);
        defaultBotOptions = mock(DefaultBotOptions.class);
        updateHandler = mock(UpdateHandler.class);
        when(botSettings.getProxy()).thenReturn(null);
        bot = new Bot(botSettings, updateHandler);
    }

    @Test
    public void testStartCommand(){
        var update = mock(UpdateHandler.class);
    }
}
