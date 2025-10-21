package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.BotMessenger;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import java.util.Map;

public class AgregarHechoCommand implements Command {
    
    private final ServicesClient servicesClient;
    private String coleccion;
    private int step = 0;
    
    public AgregarHechoCommand(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
    }
    
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        bot.sendMessage(chatId, "*Agregar Hecho*\n\nPaso 1: Ingresa el nombre de la colección:");
        
        states.put(chatId, new ConversationState() {
            private String coleccion;
            private String titulo;
            private int currentStep = 0;
            
            @Override
            public void handleInput(String input, BotMessenger bot, long chatId) {
                try {
                    switch (currentStep) {
                        case 0: // Colección
                            coleccion = input.trim();
                            currentStep++;
                            bot.sendMessage(chatId, "Paso 2: Ingresa el título del hecho:");
                            break;
                            
                        case 1: // Título
                            titulo = input.trim();
                            currentStep++;
                            bot.sendMessage(chatId, "Paso 3: Ingresa la descripción del hecho:");
                            break;
                            
                        case 2: // Descripción
                            String descripcion = input.trim();
                            
                            bot.sendMessage(chatId, 
                                "*Hecho creado*\n\n" +
                                "Colección: " + coleccion + "\n" +
                                "Título: " + titulo + "\n" +
                                "Descripción: " + descripcion + "\n\n" +
                                "Implementa POST /hecho"
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