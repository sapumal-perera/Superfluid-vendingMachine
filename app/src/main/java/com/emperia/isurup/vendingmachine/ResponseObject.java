package com.emperia.isurup.vendingmachine;

import java.util.List;
import java.util.Map;

public class ResponseObject {

    List <Map> payloads;
    private String sessionId;
    private String messageId;
    private long timestamp;

    public List<Map> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<Map> payloads) {
        this.payloads = payloads;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



}
