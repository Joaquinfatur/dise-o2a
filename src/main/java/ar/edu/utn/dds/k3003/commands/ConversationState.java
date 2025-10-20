// ========== ConversationState.java ==========
package ar.edu.utn.dds.k3003.commands;

public abstract class ConversationState {
    private boolean waitingForInput = false;
    
    public boolean isWaitingForInput() {
        return waitingForInput;
    }
    
    public void setWaitingForInput(boolean waiting) {
        this.waitingForInput = waiting;
    }
    
    public abstract void handleInput(String input, TelegramBot bot, long chatId);
}