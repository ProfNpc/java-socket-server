package lab.javachat;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        String mode = parseMode(args[0]);
        if (mode == null) {
            System.err.println("Erro: primeiro parâmetro inválido: " + args[0]);
            printUsage();
            System.exit(1);
        }

        Integer port = null;
        String host = null;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--port=") || arg.startsWith("-p=")) {
                String value = arg.substring(arg.indexOf('=') + 1);
                port = parsePort(value);
                if (port == null) {
                    System.err.println("Erro: valor de porta inválido: " + value);
                    printUsage();
                    System.exit(1);
                }

            } else if (arg.startsWith("--host=") || arg.startsWith("-h=")) {
                if (mode.equals(Constantes.Mode.SERVER)) {
                    System.err.println("Erro: parâmetro --host/-h não é permitido no modo server.");
                    printUsage();
                    System.exit(1);
                }
                host = arg.substring(arg.indexOf('=') + 1);
                if (host.isBlank()) {
                    System.err.println("Erro: valor de host não pode ser vazio.");
                    printUsage();
                    System.exit(1);
                }

            } 
//            else {
//                System.err.println("Erro: parâmetro desconhecido: " + arg);
//                printUsage();
//                System.exit(1);
//            }
        }
        
        if (port == null) {
        	port = Constantes.PORTA_PADRAO;
        	System.out.println("Porta definida com padrão[" + Constantes.PORTA_PADRAO + "]");
        }

        if (mode.equals(Constantes.Mode.CLIENT) && host == null) {
        	host = Constantes.HOST_PADRAO;
        	System.out.println("Host definido com padrão[" + Constantes.HOST_PADRAO + "]");
        } 

        // Exibe configuração final
        System.out.println("Modo: " + mode);
        if (port != null) System.out.println("Porta: " + port);
        if (host != null) System.out.println("Host: " + host);

        // Aqui você chama a lógica do server ou client
        if (mode.equals(Constantes.Mode.SERVER)) {
            startServer(port);
        } else {
            startClient(host, port);
        }
    }

    private static String parseMode(String arg) {
        return switch (arg) {
            case "--server", "-s" -> Constantes.Mode.SERVER;
            case "--client", "-c" -> Constantes.Mode.CLIENT;
            default -> null;
        };
    }

    private static Integer parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) return null;
            return port;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void startServer(Integer port) {
        System.out.println("Iniciando server" + (port != null ? " na porta " + port : " na porta padrão") + "...");
        new ChatServer(port).execute();
    }

    private static void startClient(String host, Integer port) {
        System.out.println("Conectando ao server " + host + (port != null ? ":" + port : "") + "...");
        new ChatClient(host, port).execute();
    }

    private static void printUsage() {
        System.out.println("""
                Uso:
                  java -jar java-chat.jar <modo> [opções]

                Modos:
                  --server, -s         Inicia no modo servidor
                  --client, -c         Inicia no modo cliente

                Opções:
                  --port=<número>, -p=<número>     
                  				Porta de conexão (opcional no server, 
                  				opcional no client, caso não seja informado 
                  				será definido com 12345)
                  --host=<url>,  -h=<url>          
                  				Endereço do servidor (no client, caso não 
                  				seja informado será definido com localhost, 
                  				inválido no server)

                Exemplos:
                  java -jar java-chat.jar --server
                  java -jar java-chat.jar --server --port=8080
                  java -jar java-chat.jar --client --host=localhost
                  java -jar java-chat.jar -c -h=192.168.0.1 -p=8080
                """);
    }
}
