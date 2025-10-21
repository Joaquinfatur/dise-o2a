package ar.edu.utn.dds.k3003.bot;

public abstract class ConversationState {
    private boolean waitingForInput = false;
    
    public boolean isWaitingForInput() {
        return waitingForInput;
    }
    
    public void setWaitingForInput(boolean waiting) {
        this.waitingForInput = waiting;
    }
    
    // Ahora usa la interfaz en lugar de la clase concreta
    public abstract void handleInput(String input, BotMessenger bot, long chatId);
}