package ar.edu.utn.dds.k3003.commands;

import ar.edu.utn.dds.k3003.bot.BotMessenger;
import ar.edu.utn.dds.k3003.bot.ConversationState;
import ar.edu.utn.dds.k3003.bot.TelegramBot;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import java.util.Map;
import java.util.List;

public class CambiarEstadoCommand implements Command {
    
    private final ServicesClient servicesClient;
    
    public CambiarEstadoCommand(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
    }
    
    @Override
    public void execute(TelegramBot bot, long chatId, String message, Map<Long, ConversationState> states) {
        bot.sendMessage(chatId, "✏️ *Cambiar Estado de Solicitud*\n\nPaso 1: Ingresa el ID de la solicitud:");
        
        states.put(chatId, new ConversationState() {
            private String solicitudId;
            private int currentStep = 0;
            
            @Override
            public void handleInput(String input, BotMessenger bot, long chatId) {
                try {
                    switch (currentStep) {
                        case 0: // Solicitud ID
                            solicitudId = input.trim();
                            currentStep++;
                            
                            // Mostrar opciones de estado
                            List<List<String>> keyboard = List.of(
                                List.of("Aprobada", "Rechazada"),
                                List.of("Cancelar")
                            );
                            
                            bot.sendMessageWithKeyboard(chatId, 
                                "Paso 2: Selecciona el nuevo estado:", 
                                keyboard
                            );
                            break;
                            
                        case 1: // Estado
                            String estado = input.trim();
                            
                            if (estado.equals("Cancelar")) {
                                bot.sendMessage(chatId, "Operación cancelada");
                            } else {
                                bot.sendMessage(chatId, 
                                    "*Estado actualizado*\n\n" +
                                    "Solicitud ID: " + solicitudId + "\n" +
                                    "Nuevo estado: " + estado + "\n\n" +
                                    "Implementa PATCH /solicitudes/" + solicitudId
                                );
                            }
                            
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