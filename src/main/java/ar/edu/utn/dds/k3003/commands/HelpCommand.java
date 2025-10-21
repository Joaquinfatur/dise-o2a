package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import java.util.Map;

public class HelpCommand implements Command {
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        String helpMessage = """
            *Comandos Disponibles:*
            
            /listarhechos - Listar hechos de una colección
            /verhecho - Ver detalles de un hecho específico
            /agregarhecho - Agregar un nuevo hecho a una fuente
            /agregarpdi - Agregar un PDI a un hecho
            /solicitarborrado - Crear solicitud de borrado
            /cambiarestado - Cambiar estado de solicitud
            /help - Mostrar esta ayuda
            """;
        
        bot.sendMessage(chatId, helpMessage);
    }
}