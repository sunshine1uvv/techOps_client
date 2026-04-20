package org.example.tech_ops_gui.synchronization;

import java.util.List;


public class SyncMessage<T> {
    private String action;
    private List<T> payload;

    public SyncMessage() {
    }

    public SyncMessage(String action, List<T> payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<T> getPayload() {
        return payload;
    }

    public void setPayload(List<T> payload) {
        this.payload = payload;
    }
}
