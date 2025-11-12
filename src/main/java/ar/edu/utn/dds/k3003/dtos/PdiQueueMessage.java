package ar.edu.utn.dds.k3003.dtos;

import java.io.Serializable;

/**
 * DTO para enviar mensajes de PDI a procesar a través de RabbitMQ
 */
public class PdiQueueMessage implements Serializable {
    
    private String pdiId;
    private Long timestamp;
    
    // Constructor vacío necesario para Jackson
    public PdiQueueMessage() {
    }
    
    public PdiQueueMessage(String pdiId) {
        this.pdiId = pdiId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getPdiId() {
        return pdiId;
    }
    
    public void setPdiId(String pdiId) {
        this.pdiId = pdiId;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "PdiQueueMessage{pdiId='" + pdiId + "', timestamp=" + timestamp + "}";
    }
}