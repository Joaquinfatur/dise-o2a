package ar.edu.utn.dds.k3003.bot;

import java.util.List;

public interface BotMessenger {
    void sendMessage(long chatId, String text);
    void sendMessageWithKeyboard(long chatId, String text, List<List<String>> keyboard);
    void sendPhoto(long chatId, String photoUrl, String caption);
}