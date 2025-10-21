package ar.edu.utn.dds.k3003.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ar.edu.utn.dds.k3003.commands.*;
import ar.edu.utn.dds.k3003.clients.ServicesClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final Map<String, Command> commands = new HashMap<>();
    private final Map<Long, ConversationState> conversationStates = new HashMap<>();
    
    private final ServicesClient servicesClient;

    public TelegramBot(ServicesClient servicesClient) {
        this.servicesClient = servicesClient;
        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("/start", new StartCommand());
        commands.put("/listarhechos", new ListarHechosCommand(servicesClient));
        commands.put("/verhecho", new VerHechoCommand(servicesClient));
        commands.put("/agregarhecho", new AgregarHechoCommand(servicesClient));
        commands.put("/agregarpdi", new AgregarPdICommand(servicesClient));
        commands.put("/solicitarborrado", new SolicitudBorradoCommand(servicesClient));
        commands.put("/cambiarestado", new CambiarEstadoCommand(servicesClient));
        commands.put("/help", new HelpCommand());
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Verificar si el usuario está en medio de una conversación
            ConversationState state = conversationStates.get(chatId);
            
            if (state != null && state.isWaitingForInput()) {
                // Procesar la respuesta del usuario
                handleConversationInput(chatId, messageText, state);
            } else {
                // Es un comando nuevo
                String command = messageText.split(" ")[0].toLowerCase();
                
                if (commands.containsKey(command)) {
                    try {
                        commands.get(command).execute(this, chatId, messageText, conversationStates);
                    } catch (Exception e) {
                        sendMessage(chatId, " Error: " + e.getMessage());
                    }
                } else {
                    sendMessage(chatId, " Comando no reconocido. Usa /help para ver los comandos disponibles.");
                }
            }
        }
    }

    private void handleConversationInput(long chatId, String input, ConversationState state) {
        try {
            state.handleInput(input, (BotMessenger) this, chatId);
        } catch (Exception e) {
            sendMessage(chatId, "Error procesando tu respuesta: " + e.getMessage());
            conversationStates.remove(chatId);
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageWithKeyboard(long chatId, String text, List<List<String>> keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        
        for (List<String> row : keyboard) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (String button : row) {
                keyboardRow.add(new KeyboardButton(button));
            }
            keyboardRows.add(keyboardRow);
        }
        
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(long chatId, String photoUrl, String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(new InputFile(photoUrl));
        if (caption != null) {
            photo.setCaption(caption);
        }
        
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "No se pudo cargar la imagen: " + photoUrl);
        }
    }

    public Map<Long, ConversationState> getConversationStates() {
        return conversationStates;
    }
}