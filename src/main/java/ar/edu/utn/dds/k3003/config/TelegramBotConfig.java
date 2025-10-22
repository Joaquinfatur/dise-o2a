package ar.edu.utn.dds.k3003.config;

import ar.edu.utn.dds.k3003.bot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    
    @Value("${telegram.webhook.url:https://dise-o2a.onrender.com}")
    private String webhookUrl;
    
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot bot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        
        // Configurar webhook
        SetWebhook setWebhook = SetWebhook.builder()
            .url(webhookUrl + "/telegram/webhook")
            .build();
        
        botsApi.registerBot(bot, setWebhook);
        
        System.out.println("========================================");
        System.out.println("Bot registrado con webhook: " + webhookUrl + "/telegram/webhook");
        System.out.println("========================================");
        
        return botsApi;
    }
}