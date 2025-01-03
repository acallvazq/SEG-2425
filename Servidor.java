import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.*;
import javax.crypto.*;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Servidor {
    //Atributos
    private static String raiz = "./keyStoreServidor/";
    private static int DefaultServerPort = 9001;
    private static final String contrasinal = "criptonika";
    private static KeyStore ks;

    private static final ArrayList<String> docPaths = new ArrayList<String>();

    //Clases
    public static void main(String[] args){
        int port = DefaultServerPort;
        String docroot = "";
        boolean authClient = false;
        
        if(args.length < 1 || args.length > 4){
            System.out.println("USAGE: java Servidor " + "port [TLS] [true]");
			System.exit(-1);
        }

        if(args.length >= 1){
            port = Integer.parseInt(args[0]);
        }
        
        String type = "PlainSocket";
        if(args.length >= 2){
            type = args[1];
        }

        if(args.length >= 3 && args[2].equals("true")){
            authClient = true;
        }

        definirKeyStores();        
        
        try{
            SSLServerSocket serverSocket = initServerSocket(port, authClient);
            
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
    private static SSLServerSocket initServerSocket(int port, boolean authClient) {
        SSLServerSocket serverSocket = null;

        try{
            mensajeInicio();

            // Inicializa el KeyStore y crea el KeyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            Servidor.ks = KeyStore.getInstance("JCEKS");
            FileInputStream keyStoreStream = new FileInputStream(raiz + "/keyStoreServer.jce");
            Servidor.ks.load(keyStoreStream, contrasinal.toCharArray());
            keyManagerFactory.init(Servidor.ks, contrasinal.toCharArray());
            
            // Inicializa el TrustStore y crea el TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore trustStore = KeyStore.getInstance("JCEKS");
            FileInputStream trustStoreStream = new FileInputStream(raiz + "/trustStoreServer.jce");
            trustStore.load(trustStoreStream, contrasinal.toCharArray());
            trustManagerFactory.init(trustStore);

            // Crear el SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // Crear y configurar el SSLServerSocket
            SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
            serverSocket.setNeedClientAuth(authClient);
            serverSocket.setEnabledCipherSuites(serverSocketFactory.getSupportedCipherSuites());
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.3"});

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

    /******************************************************
		          Clase Interna Hilo Cliente
    *******************************************************/
    private static class HiloCliente extends Thread {
        private final SSLSocket client;

        public HiloCliente(SSLSocket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter writer = new PrintWriter(client.getOutputStream(), true)
            ) {
                String clientIP = client.getInetAddress().getHostAddress();
                int clientPort = client.getPort();

                StringBuilder request = new StringBuilder();
                String line;

                // Leer la petición completa
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    request.append(line).append("\r\n");
                }

                System.out.println("Cliente [" + clientIP + ":" + clientPort + "]:\r\n" + request);

                String method = request.substring(0, 3);
                switch (method) {
                    case "GET":
                        System.out.println("Procesando solicitud GET");
                        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                        getDocumento(ois, writer);
                        ois.close();
                        break;

                    case "PUT":
                        System.out.println("Procesando solicitud PUT");
                        ObjectInputStream ois2 = new ObjectInputStream(client.getInputStream());
                        putDocumento(ois2, writer);
                        ois2.close();
                        break;

                    default:
                        System.out.println("Solicitud desconocida");
                        writer.println("HTTP/1.0 405 Method Not Allowed\r\n");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error al manejar el cliente: " + e.getMessage());
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el socket del cliente: " + e.getMessage());
                }
            }
        }

        private void getDocumento(ObjectInputStream ois, PrintWriter writer){
            try{
                MensajeRegistrarDocumento mensaje = (MensajeRegistrarDocumento) ois.readObject();

                String nombreDocumento = new String(mensaje.getNombreDocumento(), StandardCharsets.UTF_8);

                //El path está vacio
                if(nombreDocumento.equals("")){
                    writer.println("HTTP/1.0 403 Forbidden\r\n");
                }

                //no encuentra el archivo en la carpeta
                // if(archivo.size == 0){
                //     writer.println("HTTP/1.0 404 Not Found\r\n");
                // }

                System.out.println("Recibido: " + nombreDocumento);
            
                // Verificar firma
                byte[] firma = mensaje.getFirmaDocumento();

                // SSLSession session = socket.getSession();
				// java.security.cert.Certificate[] remotecerts = session.getPeerCertificates();
				// java.security.cert.Certificate remoteCert = remotecerts[0];

                // mensaje.verificarFirma(remoteCert.getPublicKey(), firma, mensaje.getDocumentoCifrado());

                // Descifrar clave simetrica
                byte[] claveSimetricaCifrada = mensaje.getClaveSimetricaCifrada();

                String entryAlias = "servertls";

                // Cargar clave privada
                PrivateKey privateKey = (PrivateKey) Servidor.ks.getKey(entryAlias, contrasinal.toCharArray());
                if (privateKey == null) {
                    System.out.println("No se ha encontrado la clave privada");
                    System.exit(-1);
                }

                FileOutputStream fileOutputClaveSimetricaCifrada = new FileOutputStream("./clave_simetrica_cifrada.temp");

                fileOutputClaveSimetricaCifrada.write(claveSimetricaCifrada);
                fileOutputClaveSimetricaCifrada.close();

                FileInputStream fileInputClaveSimetricaCifrada = new FileInputStream("./clave_simetrica_cifrada.temp");

                byte[] claveSimetrica = mensaje.descifrarClaveSimetrica(fileInputClaveSimetricaCifrada , privateKey);

                fileInputClaveSimetricaCifrada.close();
                new File("./clave_simetrica_cifrada.temp").delete();

                // Descifrar documento
                byte[] documentoCifrado = mensaje.getDocumentoCifrado();

                FileOutputStream fileOutputDocumentoCifrado = new FileOutputStream("./documento_cifrado.temp");

                fileOutputDocumentoCifrado.write(documentoCifrado);
                fileOutputDocumentoCifrado.close();

                FileInputStream fileInputDocumentoCifrado = new FileInputStream("./documento_cifrado.temp");

                byte[] documento = mensaje.descifrarDocumento(fileInputDocumentoCifrado, claveSimetrica);

                fileInputDocumentoCifrado.close();
                new File("./documento_cifrado.temp").delete();

                FileOutputStream fileOutputDocumento = new FileOutputStream("./documento_sin_cifrar.temp");

                fileOutputDocumento.write(documento);
                fileOutputDocumento.close();

                FileInputStream fileInputDocumento = new FileInputStream("./documento_sin_cifrar.temp");

                // Hay que cifrar el documento con la clave simetrica del servidor
                // La clave simetrica del servidor todavia no se ha generado

                SecretKey serverKey = (SecretKey) Servidor.ks.getKey("secreto", contrasinal.toCharArray());

                byte[] claveSimetricaServidor = serverKey.getEncoded();
                byte[] documentoCifradoServidor = mensaje.cifrarDocumento(fileInputDocumento,  claveSimetricaServidor);

                fileInputDocumento.close();
                // new File("./documento_sin_cifrar.temp").delete();

                String docPath = "./documentos/" + nombreDocumento.split("/")[nombreDocumento.split("/").length - 1];

                System.out.println("Guardando documento: " + docPath + " con ID:" + docPaths.size());

                docPaths.add(docPath);

                // Guardar documento cifrado de servidor en la carpeta de documentos del servidor
                FileOutputStream fos = new FileOutputStream(docPath);

                fos.write(documentoCifradoServidor);
                fos.close();

                //Leer documento y guardarlo en la carpeta de documentos del servidor (cp origen destino)
                writer.println("HTTP/1.0 200 OK\r\n");

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        private void putDocumento(ObjectInputStream ois, PrintWriter writer){
            try {
                MensajeRegistrarDocumento mensaje = (MensajeRegistrarDocumento) ois.readObject();
                writer.println("HTTP/1.0 200 OK\r\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

