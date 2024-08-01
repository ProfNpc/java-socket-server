package lab.multservidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TrataCliente implements Runnable{
	
	private MultServidor multservidor;
	
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private boolean ativo = true;
	
	public TrataCliente(MultServidor multservidor, Socket socket) {
		this.multservidor = multservidor;
		this.socket = socket;
		

		try {
			//Cria um fluxo de saída (output stream) que permitira
			//enviar dados para a outra ponta do socket (cliente)
			this.out = new PrintWriter(socket.getOutputStream(), true);
			
			//Cria um fluxo de entrada (input stream) que permitira ler
			//o que for eviado pela outra ponta do socket (cliente)
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void run() {
		
		try {
			
			while(ativo) {
				
				try {
					
					if (in.ready()) {
						String pacote = in.readLine();
						System.out.println("[MultServidor] Pacote = '" + pacote + "'");
						String[] partesPacote = pacote.split(";");
						
						String ipOutroCliente = "";
						String msg = "";
						
						if (partesPacote.length > 0) {
							msg = partesPacote[0];
						} else if (partesPacote.length > 1) {
							ipOutroCliente = partesPacote[0];
							msg = partesPacote[1];
						}
						
						TrataCliente outroCliente = this.multservidor.getOutroCliente(ipOutroCliente);
						if (outroCliente != null) {
							outroCliente.enviar(msg);
						}
					}
					
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("[MultServidor] Encerrando cliente = " + getHostAddress() + "");

		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				//Fecha e libera os recursos utilizados
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void enviar(String msg) {
		//Responde para o cliente
		out.println(msg);
	}

	public String getHostAddress() {
		String hostAddress = socket.getInetAddress().getHostAddress();
		return hostAddress;
	}

	public void encerrar() {
		this.ativo = false;
	}
	
}
