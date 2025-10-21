package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.BotMessenger;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import java.util.Map;

public class ListarHechosCommand implements Command {
    
    private final ServicesClient servicesClient;
    
    public ListarHechosCommand(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
    }
    
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        bot.sendMessage(chatId, "*Listar Hechos*\n\nPor favor, ingresa el nombre de la colección:");
        
        // Crear estado de conversación
        states.put(chatId, new ConversationState() {
            @Override
            public void handleInput(String input, BotMessenger bot, long chatId) {
                try {
                    // Aquí llamarías a tu API del agregador
                    // Por ahora, simulamos la respuesta
                    String coleccion = input.trim();
                    
                    bot.sendMessage(chatId, 
                        "Hechos de la colección: *" + coleccion + "*\n\n" +
                        "Consultando al agregador...\n\n" +
                        "Implementa la llamada a: GET /coleccion/" + coleccion + "/hechos"
                    );
                    
                    // Remover estado
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