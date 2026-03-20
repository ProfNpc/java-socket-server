package lab.javachat;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
    	ChatClient chatClient = new ChatClient();
    	chatClient.execute();
    }

	public void execute() {
		System.out.println("Conectando ao servidor " + HOST + ":" + PORT + "...");

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader serverIn = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            Scanner userInput = new Scanner(System.in)
        ) {
            System.out.println("Conectado! Aguardando mensagem de boas-vindas...\n");

            // Thread dedicada para receber mensagens do servidor de forma assíncrona
            Thread receiverThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        System.out.println("[Servidor] " + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("[!] Conexão com o servidor encerrada.");
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Loop principal: lê entrada do usuário e envia ao servidor
            System.out.println("Digite suas mensagens (ou 'sair' para encerrar):\n");
            while (userInput.hasNextLine()) {
                String line = userInput.nextLine();
                serverOut.println(line);

                if (line.equalsIgnoreCase("sair")) {
                    System.out.println("[i] Encerrando conexão...");
                    break;
                }
            }

        } catch (ConnectException e) {
            System.err.println("[ERRO] Não foi possível conectar. O servidor está rodando?");
        } catch (IOException e) {
            System.err.println("[ERRO] " + e.getMessage());
        }

        System.out.println("Cliente encerrado.");
	}
}
