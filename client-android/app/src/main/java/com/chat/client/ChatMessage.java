package com.chat.client;

/**
 * Representa uma mensagem na lista do chat.
 */
public class ChatMessage {

    public enum Type { OWN, OTHER, SYSTEM }

    public final Type type;
    public final String sender;   // null para SYSTEM e OWN
    public final String text;
    public final long timestamp;

    public ChatMessage(Type type, String sender, String text) {
        this.type      = type;
        this.sender    = sender;
        this.text      = text;
        this.timestamp = System.currentTimeMillis();
    }

    // Fábrica para mensagens do próprio usuário
    public static ChatMessage own(String text) {
        return new ChatMessage(Type.OWN, null, text);
    }

    // Fábrica para mensagens de outros clientes
    // Formato esperado: "[#N | IP:porta] texto"
    public static ChatMessage fromRaw(String raw) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("^\\[#(\\d+) \\| ([^\\]]+)\\] (.+)$")
            .matcher(raw);

        if (m.matches()) {
            String sender = "Cliente #" + m.group(1) + "  ·  " + m.group(2);
            return new ChatMessage(Type.OTHER, sender, m.group(3));
        }
        return system(raw);
    }

    // Fábrica para avisos do servidor
    public static ChatMessage system(String text) {
        return new ChatMessage(Type.SYSTEM, null,
            text.replaceFirst("^\\[Servidor\\]\\s*", ""));
    }
}
