package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.bot.TelegramBot;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/telegram")
public class TelegramWebhookController {
    
    private final TelegramBot bot;
    
    public TelegramWebhookController(TelegramBot bot) {
        this.bot = bot;
    }
    
    @PostMapping("/webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);
    }
    
    @GetMapping("/webhook")
    public String getWebhookInfo() {
        return "Telegram Webhook está activo";
    }
}