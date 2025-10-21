package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.BotMessenger;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import java.util.Map;

public class VerHechoCommand implements Command {
    
    private final ServicesClient servicesClient;
    
    public VerHechoCommand(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
    }
    
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        bot.sendMessage(chatId, "*Ver Hecho*\n\nPor favor, ingresa el ID del hecho:");
        
        states.put(chatId, new ConversationState() {
            @Override
            public void handleInput(String input, BotMessenger bot, long chatId) {
                try {
                    String hechoId = input.trim();
                    
                    // Aquí deberías llamar a tu API
                    // Por ahora simulamos
                    bot.sendMessage(chatId, 
                        "*Detalles del Hecho #" + hechoId + "*\n\n" +
                        "Consultando información...\n\n" +
                        "Implementa la llamada a: GET /hecho/" + hechoId
                    );
                    
                    states.remove(chatId);
                    setWaitingForInput(false);
                    
                } catch (Exception e) {
                    bot.sendMessage(chatId, "Error: " + e.getMessage());
                    states.remove(chatId);
                }
            }
        });
        
        states.get(chatId).setWaitingForInput(true);
    }
}