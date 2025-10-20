package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.commands.ConversationState;
import java.util.Map;

public interface Command {
    void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states);
}
