package lab.servidor;

//import java.io.*;
//import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServidorRustico {

	public static void main(String[] args) {
		try {
			System.out.println("[ServidorRustico] Inicio");
			
			Scanner teclado = new Scanner(System.in);
			System.out.println("Digite a porta:");
			int porta = Integer.parseInt(teclado.nextLine());
			teclado.close();
			
			//Cria um objeto ServerSocket para ficar "ouvindo" a porta escolhida
			//aguardando que outro programa se conecte nessa porta
			ServerSocket server = new ServerSocket(porta);

			//Recupera o endereço IP da maquina que esta rodando esse programa
			InetAddress enderecoIP = server.getInetAddress();

			//Recupera, salva e apresenta a maquina, IP e a porta onde esta a aplicacao
			String serverIdentification = String.format("IP:=%s, Porta:%s\n", InetAddress.getLocalHost(), porta);
			System.out.printf("[ServidorRustico] %s", serverIdentification);
			
			
			//Fica aguardando uma conexão
			Socket socket = server.accept();

			//Cria um fluxo de saída (output stream) que permitira
			//enviar dados para a outra ponta do socket (cliente)
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			//Cria um fluxo de entrada (input stream) que permitira ler
			//o que for eviado pela outra ponta do socket (cliente)
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//Lê o que o cliente enviou
			String str = in.readLine();
			
			//Verifica se é o texto esperado
			if (str.equals("quem é?")) {
				System.out.println("[ServidorRustico] Recebeu '" + str + "'");

				//Responde para o cliente
				out.println(serverIdentification);
			}

			//Fecha e libera os recursos utilizados
			in.close();
			out.close();
			server.close();
		}catch (Exception e) {
			System.out.println(e);
		} finally {
			System.out.println("[ServidorRustico] Fim");
		}
	}
}
