package lab.javachat;

public class Main {
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Nenhum parâmetro informado");
			mostrarListaDeOpcoes();
		} else if (args.length > 1) {
			System.out.println("Informe apenas 1 dos seguintes parâmetros:");
			mostrarListaDeOpcoes();
		} else {
			String opcao = args[0];
			if (opcao == null) {
				System.out.println("Parâmetro vazio");
				System.out.println("Informe apenas 1 dos seguintes parâmetros:");
				mostrarListaDeOpcoes();
			} else {
				opcao = opcao.trim();
				if ("--server".equalsIgnoreCase(opcao)
					|| "-s".equalsIgnoreCase(opcao)) {
					
					new ChatServer().execute();
					
				} else if ("--client".equalsIgnoreCase(opcao)
					|| "-c".equalsIgnoreCase(opcao)) {
					
					new ChatClient().execute();
					
				} else {
					System.out.println("Parâmetro " + opcao + " inválido");
					System.out.println("Informe apenas 1 dos seguintes parâmetros:");
					mostrarListaDeOpcoes();
				}
			}
		}
	}

	private static void mostrarListaDeOpcoes() {
		System.out.println("--server, -s para executar em modo servidor; ou");
		System.out.println("--client, -c para executar em modo cliente.");
	}

}
