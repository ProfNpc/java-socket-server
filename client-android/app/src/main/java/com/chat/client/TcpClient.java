package com.chat.client;

import java.io.*;
import java.net.*;

/**
 * Cliente TCP puro para o ChatServer.
 * Usa BufferedReader / PrintWriter sobre Socket —
 * o mesmo protocolo de linha que o servidor espera.
 */
public class TcpClient {

    public interface Listener {
        void onConnected();
        void onMessage(String message);
        void onDisconnected(String reason);
        void onError(String error);
    }

    private final String host;
    private final int port;
    private final Listener listener;

    private Socket socket;
    private PrintWriter out;
    private volatile boolean running = false;

    public TcpClient(String host, int port, Listener listener) {
        this.host     = host;
        this.port     = port;
        this.listener = listener;
    }

    // ── Conexão ───────────────────────────────────────────────────────────────

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000);
                socket.setSoTimeout(0);

                out = new PrintWriter(
                        new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream(), "UTF-8")),
                        true); // autoFlush

                running = true;
                listener.onConnected();
                startReader();

            } catch (ConnectException e) {
                listener.onError("Não foi possível conectar. O servidor está rodando?");
            } catch (Exception e) {
                listener.onError("Erro ao conectar: " + e.getMessage());
            }
        }, "tcp-connect").start();
    }

    public void disconnect() {
        running = false;
        // Envia "sair" para o servidor encerrar a sessão corretamente
        if (out != null) out.println("sair");
        close();
        listener.onDisconnected("Desconectado pelo usuário.");
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected()
                && !socket.isClosed() && running;
    }

    // ── Envio ─────────────────────────────────────────────────────────────────

    public void send(String message) {
        if (!isConnected() || message == null || message.isBlank()) return;
        // PrintWriter com autoFlush — não bloqueia a UI thread por tempo relevante,
        // mas rodamos em thread separada por boa prática
        new Thread(() -> out.println(message), "tcp-send").start();
    }

    // ── Leitura contínua ──────────────────────────────────────────────────────

    private void startReader() {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

                String line;
                while (running && (line = in.readLine()) != null) {
                    listener.onMessage(line);
                }

            } catch (IOException e) {
                if (running) {
                    listener.onDisconnected("Conexão encerrada pelo servidor.");
                }
            } finally {
                running = false;
                close();
            }
        }, "tcp-reader").start();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}
