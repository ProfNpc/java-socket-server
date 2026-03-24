package lab.javachat;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ChatServer {

    private static final int TAMANHO_THREAD_POOL = 50;

    // Lista thread-safe de todos os handlers ativos
    private static final CopyOnWriteArrayList<ClientHandler> clients =
            new CopyOnWriteArrayList<>();

    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    private int porta;
    
    public ChatServer(int porta) {
		this.porta = porta;
	}

	public ChatServer() {
		this(Constantes.PORTA_PADRAO);
	}

	public static void main(String[] args) {
    	ChatServer chatServer = new ChatServer();
    	chatServer.execute();
    }

	public void execute() {
		ExecutorService threadPool = Executors.newFixedThreadPool(TAMANHO_THREAD_POOL);

        System.out.println("Servidor de chat iniciado na porta " + this.porta);

        try (ServerSocket serverSocket = new ServerSocket(this.porta)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.incrementAndGet();

                ClientHandler handler = new ClientHandler(clientSocket, clientId);
                clients.add(handler);

                System.out.println("[+] Cliente #" + clientId + " conectado: "
                        + handler.getAddress());

                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
	}

    // Envia uma mensagem para todos os clientes, exceto o remetente
    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Remove um cliente da lista quando ele desconecta
    static void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("[-] " + handler.getAddress() + " desconectado. "
                + "Clientes ativos: " + clients.size());
    }

    // -------------------------------------------------------------------------

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private final int clientId;
        private final String address;
        private PrintWriter out;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
            // Captura IP:porta no momento da conexão
            this.address = socket.getInetAddress().getHostAddress()
                    + ":" + socket.getPort();
        }

        public String getAddress() {
            return address;
        }

        // Envia uma mensagem para ESTE cliente (chamado pelo broadcast)
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = writer;

                // Avisa o próprio cliente seu ID e endereço
                out.println("[Servidor] Você é o cliente #" + clientId
                        + " (" + address + ")");
                out.println("[Servidor] Digite 'sair' para desconectar.\n");

                // Anuncia a chegada para os outros clientes
                broadcast("[Servidor] Cliente #" + clientId
                        + " (" + address + ") entrou no chat.", this);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.isBlank()) continue;

                    if (message.equalsIgnoreCase("sair")) {
                        out.println("[Servidor] Até logo!");
                        break;
                    }

                    // Formata e propaga a mensagem com identificação do remetente
                    String formatted = "[#" + clientId + " | " + address + "] " + message;
                    System.out.println(formatted);
                    broadcast(formatted, this);
                }

            } catch (IOException e) {
                System.err.println("[ERRO] Cliente " + address + ": " + e.getMessage());
            } finally {
                // Anuncia a saída antes de remover da lista
                removeClient(this);
                broadcast("[Servidor] Cliente #" + clientId
                        + " (" + address + ") saiu do chat.", this);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }
}