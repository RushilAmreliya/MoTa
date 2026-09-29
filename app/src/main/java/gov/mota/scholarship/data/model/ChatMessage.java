package gov.mota.scholarship.data.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public static final int TYPE_BOT = 0;
    public static final int TYPE_USER = 1;

    private int type;
    private String text;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(int type, String text) {
        this.type = type;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    public int getType() { return type; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
}
