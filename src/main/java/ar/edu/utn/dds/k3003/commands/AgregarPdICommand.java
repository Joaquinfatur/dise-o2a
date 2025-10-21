package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.BotMessenger;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import java.util.Map;

public class AgregarPdICommand implements Command {
    
    private final ServicesClient servicesClient;
    
    public AgregarPdICommand(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
    }
    
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        bot.sendMessage(chatId, "*Agregar PDI*\n\nPaso 1: Ingresa el ID del hecho:");
        
        states.put(chatId, new ConversationState() {
            private String hechoId;
            private int currentStep = 0;
            
            @Override
            public void handleInput(String input, BotMessenger bot, long chatId) {
                try {
                    switch (currentStep) {
                        case 0: // Hecho ID
                            hechoId = input.trim();
                            currentStep++;
                            bot.sendMessage(chatId, "Paso 2: Ingresa el contenido del PDI (puede incluir URL de imagen):");
                            break;
                            
                        case 1: // Contenido
                            String contenido = input.trim();
                            
                            bot.sendMessage(chatId, 
                                "*PDI agregado*\n\n" +
                                "Hecho ID: " + hechoId + "\n" +
                                "Contenido: " + contenido + "\n\n" +
                                "Implementa POST /pdis"
                            );
                            
                            states.remove(chatId);
                            setWaitingForInput(false);
                            break;
                    }
                } catch (Exception e) {
                    bot.sendMessage(chatId, "Error: " + e.getMessage());
                    states.remove(chatId);
                }
            }
        });
        
        states.get(chatId).setWaitingForInput(true);
    }
}