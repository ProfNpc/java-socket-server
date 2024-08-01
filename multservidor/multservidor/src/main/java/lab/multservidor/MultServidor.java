package lab.multservidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MultServidor implements Runnable {
	
	private Map<String, TrataCliente> mapaClientes = new HashMap<>();
	private int port;
	private boolean ativo = true;
	private ServerSocket server;
	
	
	
    public MultServidor(int port) {
		this.mapaClientes = mapaClientes;
		this.port = port;
	}

	public static void main( String[] args ) {
		
		MultServidor multServidor = new MultServidor(Integer.parseInt(args[0]));
		
		multServidor.subir();
		

    }

	private void subir() {
		try {
			System.out.println("[MultServidor] Inicio");
			//Cria um objeto ServerSocket para ficar "ouvindo" a porta 7777
			//aguardando que outro programa se conecte nessa porta
			this.server = new ServerSocket(7777);

			//Recupera o endereço IP da maquina que esta rodando esse programa
			InetAddress enderecoIP = server.getInetAddress();
			//Recupera a porta, que nós sabemos que é a 7777
			int porta = server.getLocalPort();

			//Recupera, salva e apresenta a maquina, IP e a porta onde esta a aplicacao
			String serverIdentification = String.format("IP:=%s, Porta:%s\n", InetAddress.getLocalHost(), porta);
			System.out.printf("[MultServidor] %s", serverIdentification);
			
			iniciarTeclado();
			
			while(ativo) {
				//Fica aguardando uma conexão
				Socket socket = server.accept();

				TrataCliente cliente = new TrataCliente(this, socket);
				mapaClientes.put(cliente.getHostAddress(), cliente);
				new Thread(cliente).start();
				
				mostrarListaDosClientesParaTodosOsClientes();
			}
			

		}catch (Exception e) {
			System.out.println(e);
		} finally {
			

			
			System.out.println("[MultServidor] Fim");
		}		
	}

	private void encerrarClientes() {
		System.out.println("encerrarClientes()");
		for(TrataCliente c : mapaClientes.values()) {
			c.encerrar();
		}
		
		try {
			this.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[MultServidor] saindo");
	}

	private void mostrarListaDosClientesParaTodosOsClientes() {
		for(TrataCliente c : mapaClientes.values()) {
			String lista = getLista(c);
			c.enviar(lista);
		}
	}

	private String getLista(TrataCliente clienteDestino) {
		StringBuilder sb = new StringBuilder("Lista de Clientes");
		for(TrataCliente c : mapaClientes.values()) {
			if (!c.getHostAddress().equals(clienteDestino.getHostAddress())) {
				sb.append("IP:" + c.getHostAddress()).append("\n");
			}
		}
		String lista = sb.toString();
		return lista;
	}

	private void iniciarTeclado() {
		new Thread(this).start();
	}

	public TrataCliente getOutroCliente(String ipOutroCliente) {
		return this.mapaClientes.get(ipOutroCliente);
	}

	@Override
	public void run() {
		Scanner teclado = new Scanner(System.in);
		while(ativo && teclado.hasNext()) {
			String comando = teclado.nextLine();
			if (comando.trim().equals("sair")) {
				this.ativo = false;
			}
		}
		teclado.close();
		
		encerrarClientes();
		
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
