package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.BotMessenger;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import java.util.Map;

public class SolicitudBorradoCommand implements Command {
    
    private final ServicesClient servicesClient;
    
    public SolicitudBorradoCommand(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
    }
    
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        bot.sendMessage(chatId, "*Solicitar Borrado*\n\nPor favor, ingresa el ID del hecho a borrar:");
        
        states.put(chatId, new ConversationState() {
            @Override
            public void handleInput(String input, BotMessenger bot, long chatId) {
                try {
                    String hechoId = input.trim();
                    
                    bot.sendMessage(chatId, 
                        "*Solicitud de borrado creada*\n\n" +
                        "Hecho ID: " + hechoId + "\n\n" +
                        "Implementa POST /solicitudes"
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