package com.chat.client;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements TcpClient.Listener {

    private EditText etHost, etPort, etMessage;
    private Button   btnConnect, btnSend;
    private TextView tvStatus;
    private View     statusDot;
    private RecyclerView recycler;

    private ChatAdapter adapter;
    private TcpClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        etHost     = findViewById(R.id.et_host);
        etPort     = findViewById(R.id.et_port);
        etMessage  = findViewById(R.id.et_message);
        btnConnect = findViewById(R.id.btn_connect);
        btnSend    = findViewById(R.id.btn_send);
        tvStatus   = findViewById(R.id.tv_status);
        statusDot  = findViewById(R.id.status_dot);
        recycler   = findViewById(R.id.recycler);

        // RecyclerView
        adapter = new ChatAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        // Eventos
        btnConnect.setOnClickListener(v -> onConnectClick());
        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        setConnectedState(false);
        addSystemMessage("Informe o servidor e toque em Conectar.");
    }

    // ── Conexão ───────────────────────────────────────────────────────────────

    private void onConnectClick() {
        if (tcpClient != null && tcpClient.isConnected()) {
            tcpClient.disconnect();
            return;
        }

        String host = etHost.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();

        if (host.isEmpty() || portStr.isEmpty()) {
            Toast.makeText(this, "Preencha host e porta.", Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Porta inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.clear();
        addSystemMessage("Conectando a " + host + ":" + port + "...");
        btnConnect.setEnabled(false);

        tcpClient = new TcpClient(host, port, this);
        tcpClient.connect();
    }

    private void sendMessage() {
        if (tcpClient == null || !tcpClient.isConnected()) return;
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        tcpClient.send(text);
        addMessage(ChatMessage.own(text));
        etMessage.setText("");
    }

    // ── TcpClient.Listener ───────────────────────────────────────────────────

    @Override public void onConnected() {
        runOnUiThread(() -> {
            setConnectedState(true);
            btnConnect.setEnabled(true);
        });
    }

    @Override public void onMessage(String message) {
        runOnUiThread(() -> {
            if (message.startsWith("[Servidor]")) {
                addMessage(ChatMessage.system(message));
            } else {
                addMessage(ChatMessage.fromRaw(message));
            }
        });
    }

    @Override public void onDisconnected(String reason) {
        tcpClient.send("sair");
        runOnUiThread(() -> {
            setConnectedState(false);
            btnConnect.setEnabled(true);
            addSystemMessage(reason);
        });
    }

    @Override public void onError(String error) {
        runOnUiThread(() -> {
            setConnectedState(false);
            btnConnect.setEnabled(true);
            addSystemMessage("Erro: " + error);
        });
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void addMessage(ChatMessage msg) {
        adapter.add(msg);
        recycler.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void addSystemMessage(String text) {
        addMessage(ChatMessage.system(text));
    }

    private void setConnectedState(boolean connected) {
        statusDot.setBackgroundResource(
            connected ? R.drawable.dot_connected : R.drawable.dot_disconnected);
        tvStatus.setText(connected ? "Conectado" : "Desconectado");
        btnConnect.setText(connected ? "Desconectar" : "Conectar");
        etMessage.setEnabled(connected);
        btnSend.setEnabled(connected);
        etHost.setEnabled(!connected);
        etPort.setEnabled(!connected);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (tcpClient != null) tcpClient.disconnect();
    }
}
