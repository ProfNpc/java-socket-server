package lab.clientemultservidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteMultServidor {

	public static void main(String[] args) {

		try {
			System.out.println("[Cliente] Inicio");

			//Cria um objeto socket que se conecta na porta 7777 na maquina de endereço localhost
			Socket clientSocket = new Socket("localhost", 7777);

			//Cria um fluxo de saída (output stream) que permitira
			//enviar dados para a outra ponta do socket (servidor)
			PrintWriter saida = new PrintWriter(clientSocket.getOutputStream(), true);
			
			//Cria um fluxo de entrada (input stream) que permitira ler
			//o que for eviado pela outra ponta do socket (servidor)
			BufferedReader entrada = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			System.out.println("[Cliente] Vai enviar 'quem é?'");
			//Aqui a mensagem é enviada para a outra ponta do socket(servidor)
			saida.println("quem é?");
		
			//Aqui a resposta enviada pelo servidor pelo socket é lida
			String resposta = entrada.readLine();
			
			System.out.println("[Cliente] Resposta:" + resposta);
			
			//Fecha e libera os recursos utilizados
			saida.close();
			entrada.close();
			clientSocket.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("[Cliente] Fim");
		}
	}
}
