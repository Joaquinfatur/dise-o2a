package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import java.util.Map;

public class StartCommand implements Command {
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        String welcomeMessage = """
            👋 ¡Bienvenido al Bot de Gestión de Hechos y PDIs!
            
            Este bot te permite:
            📋 Listar hechos
            👁️ Ver detalles de un hecho
            ➕ Agregar hechos y PDIs
            🗑️ Gestionar solicitudes de borrado
            
            Usa /help para ver todos los comandos disponibles.
            """;
        
        bot.sendMessage(chatId, welcomeMessage);
    }
}