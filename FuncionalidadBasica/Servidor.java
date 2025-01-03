import java.io.*;
import java.net.*;
import java.security.KeyStore;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class Servidor {
    //Atributos
    private static String raiz = "../keyStoreServidor/";
    private static int DefaultServerPort = 9001;
    private static final String contrasinal = "criptonika";

    //Clases
    public static void main(String[] args){
        int port = DefaultServerPort;
        String docroot = "";
        
        if(args.length > 4){
            System.out.println("USAGE: java Servidor " + "port [TLS] [true]");
			System.exit(-1);
        }

        if(args.length >= 1){
            port = Integer.parseInt(args[0]);
        }

        if(args.length >= 2){
            port = Integer.parseInt(args[1]);
        }

        String type = "PlainSocket";
        if(args.length >= 3){
            type = args[2];
        }

        definirKeyStores();        
        
        try{
            SSLServerSocket serverSocket = initServerSocket(port);

            while (true) {
                // Atender cada cliente en un hilo separado
                SSLSocket client = (SSLSocket) serverSocket.accept();
                new HiloCliente(client).start();
            }

        }catch(Exception e){
            System.out.println(e);
        }

    }


    //Metodos
    private static SSLServerSocket initServerSocket(int port) {
        SSLServerSocket serverSocket = null;

        try{
            mensajeInicio();

            // Inicializa el KeyStore y crea el KeyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            FileInputStream keyStoreStream = new FileInputStream(raiz + "/keyStoreServer.jce");
            keyStore.load(keyStoreStream, contrasinal.toCharArray());
            keyManagerFactory.init(keyStore, contrasinal.toCharArray());
            
            // Inicializa el TrustStore y crea el TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore trustStore = KeyStore.getInstance("JCEKS");
            FileInputStream trustStoreStream = new FileInputStream(raiz + "/trustStoreServer.jce");
            trustStore.load(trustStoreStream, contrasinal.toCharArray());
            trustManagerFactory.init(trustStore);

            // Crear el SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Crear y configurar el SSLServerSocket
            SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});

        }catch(Exception e){
            System.out.println(e);
        }                

        return serverSocket;
    }

    private static void mensajeInicio(){
        try{
            System.out.print("Iniciando el servidor");
            Thread.sleep(1*1000);
            System.out.print(".");
            Thread.sleep(1*1000);
            System.out.print(".");
            Thread.sleep(1*1000);
            System.out.print(".");
            Thread.sleep(1*1000);
            System.out.print("\n");
        }catch(Exception e){
            System.out.println(e);
        }
    }


    /******************************************************
		            definirKeyStores()
    *******************************************************/
	private static void definirKeyStores()
	{
	    // Almacen de claves	
	    System.setProperty("javax.net.ssl.keyStore", raiz + "/keyStoreServer.jce");
	    System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
	    System.setProperty("javax.net.ssl.keyStorePassword", contrasinal);
	
	    // Almacen de confianza	    
	    System.setProperty("javax.net.ssl.trustStore", raiz + "/trustStoreServer.jce");
	    System.setProperty("javax.net.ssl.trustStoreType", "JCEKS");
	    System.setProperty("javax.net.ssl.trustStorePassword", contrasinal);
	}

    /**
     * Clase interna
     */
    private static class HiloCliente extends Thread {
        private final SSLSocket clientSocket;

        public HiloCliente(SSLSocket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                writer.println("Conexión segura establecida. Escribe algo:");

                // Leer y responder al cliente
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Cliente [" + clientSocket.getInetAddress() + "]: " + line);
                    writer.println("Servidor: " + line); // Respuesta eco
                }
            } catch (IOException e) {
                System.err.println("Error al manejar el cliente: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el socket del cliente: " + e.getMessage());
                }
            }
        }
    }
}

