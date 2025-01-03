import java.util.Scanner;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.AlgorithmParameters;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class Cliente {
    //Atributos y constantes
    private static String raiz;
    private static String ficheroKeyStore;
    private static String ficheroTrustStore;
    private static String contrasinal;
	private static String IpOCSPResponder;
	private static byte[] claveSimetrica = null;
    
    //Clases
    public static void main (String[] args){
		String host = "127.0.0.1";
		String raizAlmacenes;
		int port = 2080;
        
        // Comprueba los argumentos
		if (args.length != 4) {
			System.out.println("USAGE: java Cliente keyStoreFile truststoreFile contraseñaKeystore IpOCSPResponder");
			System.exit(-1);
		}
		contrasinal = args[2];
		IpOCSPResponder = args[3];

		//Directorio de trabajo
		raiz = System.getProperty("user.dir");
		raizAlmacenes = raiz + "/keyStoreCliente/";
		ficheroKeyStore = raizAlmacenes + args[0];
        ficheroTrustStore = raizAlmacenes + args[1];

		definirKeyStores();
		//iniciarConexionTLS(host, port);

		menu(host, port);   

    }

    //Metodos
	private static void menu(String host, int port){
		String respuesta;
        Scanner scanner = new Scanner(System.in);
		int salir = 1;

		do {
            System.out.println("\n" + 
                "╔════════════════════════════════════╗\n" +
                "║        GESTOR DE DOCUMENTOS        ║\n" +
                "╚════════════════════════════════════╝\n" +
                "  1) 📝 Registrar un documento       \n" +
                "  2) 📖 Recuperar un documento       \n" +
                "  X) 🚪 Salir                        \n");

            System.out.print("Seleccione una opcion: ");

            // Lee la respuesta del usuario y escoge opcion
            respuesta = scanner.nextLine().toUpperCase();

            switch(respuesta){
                case "1":
                    registrarDocumento(host, port);
                    break;
                case "2":
                  //  recuperarDocumento();   
                    break;
                case "X":
                    System.out.println("Saliendo del sistema...");
                    salir = 0;
                    break;
            }

        }while(salir == 1);

	}

	/******************************************************
	 *              Registrar un Documento
	 *****************************************************/

    private static void registrarDocumento(String host, int port){
        iniciarConexionTLS(host,port, 1);
    }

	private static String[] seleccionarSuite(String[] cipherSuites, boolean imprimirListado) {

			if (imprimirListado) {
				System.out.println("******** CypherSuites Disponibles **********");
				for (int i = 0; i < cipherSuites.length; i++)
					System.out.println(i+1 + ": " +cipherSuites[i]);
			}
				
			System.out.print("Selecciona una suite : ");
			Scanner scanner = new Scanner(System.in);

			int suite = scanner.nextInt();
			if (suite < 1 || suite > cipherSuites.length) {
				System.out.println("Suite de cifrado no válida, vuelve a intentarlo");
				return seleccionarSuite(cipherSuites, false);
			}
			String[] cipherSuitesHabilitadas = { cipherSuites[suite-1] };
			return cipherSuitesHabilitadas;
	}

	private static void iniciarConexionTLS(String host, int port, int idOperacion) {
		try {
			System.out.println("Crear socket");
			SSLContext context = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JCEKS");

			ks.load(new FileInputStream(Cliente.ficheroKeyStore), contrasinal.toCharArray());
			kmf.init(ks, contrasinal.toCharArray());
			context.init(kmf.getKeyManagers(), null, null);

			SSLSocketFactory factory = context.getSocketFactory();

			boolean imprimirListado = true;
			String[] cipherSuitesHabilitadas = seleccionarSuite(factory.getSupportedCipherSuites(), imprimirListado);

			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

			socket.setEnabledCipherSuites(cipherSuitesHabilitadas);
			socket.setEnabledProtocols(new String[]{"TLSv1.3"});

			System.out.println("Comienzo SSL Handshake");
			socket.startHandshake();
			System.out.println("Fin SSL Handshake");

			// Usa BufferedWriter para las cabeceras HTTP
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			FileInputStream documento = null;
			ObjectOutputStream outputStream = null;

			if (idOperacion == 1) {
				String pathFile = preguntaUsuario("Introduce el directorio del archivo que deseas enviar: ");
				String entryAlias = "client1";

				SSLSession session = socket.getSession();
				java.security.cert.Certificate[] remotecerts = session.getPeerCertificates();
				java.security.cert.Certificate remoteCert = remotecerts[0];

				PrivateKey privateKey = (PrivateKey) ks.getKey(entryAlias, contrasinal.toCharArray());
				if (privateKey == null) {
					System.out.println("No se ha encontrado la clave privada");
					System.exit(-1);
				}

				MensajeRegistrarDocumento mensaje = new MensajeRegistrarDocumento();
				mensaje.setNombreDocumento(pathFile.getBytes(StandardCharsets.UTF_8));

				Key claveSimetricaKey = ks.getKey("secreto", contrasinal.toCharArray());

				if (claveSimetricaKey != null) {
					claveSimetrica = claveSimetricaKey.getEncoded();
				}

				documento = new FileInputStream(pathFile);
				byte[] documentoCifrado = mensaje.cifrarDocumento(documento, claveSimetrica);
				byte[] claveSimetricaCifrada = MensajeRegistrarDocumento.cifrarClaveSimetrica(claveSimetrica, remoteCert.getPublicKey());

				mensaje.setDocumentoCifrado(documentoCifrado);
				mensaje.setClaveSimetricaCifrada(claveSimetricaCifrada);
				mensaje.setCertificadoFirmaC(ks.getCertificate(entryAlias).getEncoded());
				mensaje.setFirmaDocumento(MensajeRegistrarDocumento.firmarDocumento(documento, privateKey));

				

				// Construye y envía la cabecera HTTP
				writer.write("GET " + pathFile + " HTTP/1.0\r\n");
				writer.write("Content-Length: " + mensaje.convertToBytes(mensaje).length + "\r\n");
				writer.write("Content-Type: text/html\r\n");
				writer.write("\r\n");
				writer.flush();

				// Envía el objeto "mensaje" como contenido
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				outputStream.writeObject(mensaje);
				outputStream.flush();

			}

			// Lee la respuesta del servidor
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
			}

           in.close();
		   writer.close();
		   if(documento != null)
		   	documento.close();
		   if(outputStream != null)
		   	outputStream.close();
           socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	private static String preguntaUsuario(String mensaje){
        String respuesta;
		Scanner scanner = new Scanner(System.in);

		System.out.print(mensaje);
        respuesta = scanner.nextLine();

		return respuesta;
    }

	/******************************************************
	 * definirKeyStores()
	 *****************************************************/
    
	private static void definirKeyStores() {
        // Almacen de credenciales
        System.setProperty("javax.net.ssl.keyStore", ficheroKeyStore);
        System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
        System.setProperty("javax.net.ssl.keyStorePassword", contrasinal);

        // Almacen de confianza
        System.setProperty("javax.net.ssl.trustStore", ficheroTrustStore);
        System.setProperty("javax.net.ssl.trustStoreType", "JCEKS");
        System.setProperty("javax.net.ssl.trustStorePassword", contrasinal);
    }

	    /**********************************************************
    * getBytes -- Retorna un array de bytes con el contenido del fichero. 
    *    representado por el argumento <b>path</b>.
    *
    *  @return the bytes for the file
    *  @exception FileNotFoundException si el fichero 
    *      <b>path</b> no existe
    *********************************************************/
    public static byte[] getBytes(String path)  
    	                throws IOException, FileNotFoundException     {

	    String fichero = path;

	    File f = new File(fichero);
		int length = (int)(f.length());

		System.out.println("leyendo: " + fichero);
		
		if (length == 0) {
		    throw new IOException("La longitud del fichero es cero: " + path);
		} 
		else 
		{
		    FileInputStream fin = new FileInputStream(f);
		    DataInputStream in  = new DataInputStream(fin);
	
		    byte[] bytecodes = new byte[length];
	
		    in.readFully(bytecodes);
		    return bytecodes;
		}
    }

}
