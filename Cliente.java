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

				byte[] claveSimetrica;

				Key claveSimetricaKey = ks.getKey("clave_simetrica", contrasinal.toCharArray());

				if (claveSimetricaKey != null) {
					claveSimetrica = claveSimetricaKey.getEncoded();
				} else {
					claveSimetrica = Cliente.crearClaveSimetrica();
					ks.setKeyEntry("clave_simetrica", claveSimetrica, new java.security.cert.Certificate[] { ks.getCertificate(entryAlias) });
				}

				documento = new FileInputStream(pathFile);
				byte[] documentoCifrado = MensajeRegistrarDocumento.cifrarDocumento(documento, claveSimetrica);
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



   /* private static void iniciarConexionTLS(String host, int port, int idOperacion){
		try{
			// SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();

			System.out.println ("Crear socket");
			// SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
			
			// Ver las suites SSL disponibles
			System.out.println ("CypherSuites");
			SSLContext context = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JCEKS");

			ks.load(new FileInputStream(Cliente.ficheroKeyStore), contrasinal.toCharArray());
			kmf.init(ks, contrasinal.toCharArray());
				
			context.init(kmf.getKeyManagers(), null, null);

			// Asignamos un socket al contexto.

			SSLSocketFactory factory = context.getSocketFactory();
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

			System.out.println ("Comienzo SSL Handshake");

			socket.startHandshake();
				
			System.out.println("Fin SSL Handshake");

			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

			if (idOperacion == 1){
				String pathFile = preguntaUsuario("Introduce el directorio del archivo que deseas enviar: ");

				String entryAlias = "client1";

				SSLSession session = socket.getSession();
				java.security.cert.Certificate[] remotecerts = session.getPeerCertificates();

				java.security.cert.Certificate localCert = ks.getCertificate(entryAlias);
				java.security.cert.Certificate remoteCert = remotecerts[0];

				PrivateKey privateKey = (PrivateKey) ks.getKey(entryAlias, contrasinal.toCharArray());

				if (privateKey == null) {
					System.out.println("No se ha encontrado la clave privada");
					System.exit(-1);
				}

				MensajeRegistrarDocumento mensaje = new MensajeRegistrarDocumento();

				mensaje.setNombreDocumento(pathFile.getBytes(Charset.forName("UTF-8")));

				System.out.println("Nombre documento: " + pathFile);

				byte[] claveSimetrica = Cliente.crearClaveSimetrica();

				System.out.println("Clave simetrica: " + new String(claveSimetrica));

				System.out.println("Documento a cifrar: " + new String(Cliente.getBytes(pathFile)));

				byte[] documentoCifrado = MensajeRegistrarDocumento.cifrarDocumento(new FileInputStream(pathFile), claveSimetrica);

				mensaje.setDocumentoCifrado(documentoCifrado);

				System.out.println("Documento cifrado: " + new String(documentoCifrado));

				byte[] claveSimetricaCifrada = MensajeRegistrarDocumento.cifrarClaveSimetrica(claveSimetrica, remoteCert.getPublicKey());

				System.out.println("Clave simetrica cifrada: " + new String(claveSimetricaCifrada));

				mensaje.setClaveSimetricaCifrada(claveSimetricaCifrada);

				mensaje.setCertificadoFirmaC(localCert.getEncoded());

				byte[] firmaDocumento = MensajeRegistrarDocumento.firmarDocumento(new FileInputStream(pathFile), privateKey);

				mensaje.setFirmaDocumento(firmaDocumento);

				System.out.println("Registrando documento...");

				out.writeUTF("GET " + pathFile  + " "  + "HTTP/1.0\r\n");
				try {
					int length = mensaje.convertToBytes(mensaje).length;
					out.writeUTF("Content-Length: " + length +
							"\r\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
				out.writeUTF("Content-Type: text/html\r\n\r\n");

				out.writeObject(mensaje);

				out.flush();

				System.out.println("GET " + pathFile + " " + "HTTP/1.0");
				
			}
			else{
				
			}


			/* Leer respuesta */
	/*		BufferedReader in = new BufferedReader(
								new InputStreamReader(
								socket.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);

			in.close();
			out.close();
			socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/


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

	public static byte[] crearClaveSimetrica() {

		if (Cliente.claveSimetrica != null) {
			return Cliente.claveSimetrica;
		}

		try {
			// Generarla
			String algoritmo = "AES";
			KeyGenerator kgen = KeyGenerator.getInstance(algoritmo);
			int longclave = 192;
			kgen.init(longclave);

			SecretKey skey = kgen.generateKey();

			Cliente.claveSimetrica = skey.getEncoded();

			// Almacenarla
			return Cliente.claveSimetrica;
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[0];
		}
    }
}



    /*
    ------------------------     PENDIENTE DE REVISION     ------------------------
    private SSLSocket iniciarConexionSSL(){
        SSLSocket socket;
        definirKeyStores();

        // Crear Key Manager
        try {
            SSLSocketFactory factory = null;
			SSLContext ctx;
			KeyManagerFactory kmf;
			KeyStore ks;

			try {
				ctx = SSLContext.getInstance("TLS");

				// Definir el/los KeyManager.
				kmf = KeyManagerFactory.getInstance("SunX509");
				ks = KeyStore.getInstance("JCEKS");
				ks.load(new FileInputStream(keyStorePathCliente), contrasinal);
				kmf.init(ks, contrasinal);
				
				ctx.init(kmf.getKeyManagers(), null, null);

				// Asignamos un socket al contexto.

				factory = ctx.getSocketFactory();*/

				/*********************************************************************
				 * Suites del contexto
				 *********************************************************************/
	/*			System.out.println("******** CypherSuites Disponibles **********");
				cipherSuites = factory.getSupportedCipherSuites();
				for (int i = 0; i < cipherSuites.length; i++)
					System.out.println(cipherSuites[i]);

				// Suites habilitadas por defecto

				System.out.println("******* CypherSuites Habilitadas por defecto **********");

				String[] cipherSuitesDef = factory.getDefaultCipherSuites();
				for (int i = 0; i < cipherSuitesDef.length; i++)
					System.out.println(cipherSuitesDef[i]);

			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}

			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

			String[] cipherSuitesHabilitadas = { "TLS_RSA_WITH_AES_128_CBC_SHA" };

			System.out.println(cipherSuitesHabilitadas[0]);

			socket.setEnabledCipherSuites(cipherSuitesHabilitadas);

			System.out.println("****** CypherSuites Habilitadas  **********");

			String[] cipherSuitesHabilSocket = socket.getEnabledCipherSuites();
			for (int i = 0; i < cipherSuitesHabilSocket.length; i++)
				System.out.println(cipherSuitesHabilSocket[i]);


			System.out.println("\n*************************************************************");
			System.out.println("  Comienzo SSL Handshake -- Cliente y Servidor Autenticados     ");
			System.out.println("*************************************************************");

			socket.startHandshake();

			System.out.println("\n*************************************************************");
			System.out.println("      Fin OK   SSL Handshake");
			System.out.println("*************************************************************");

			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
			
			out.println("GET /" + path + " HTTP/1.1");
			out.println();
			out.flush();

			if (out.checkError())
				System.out.println("SSLSocketClient: java.io.PrintWriter error");*/

			/* Leer respuesta */
		/*	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);

			in.close();
			out.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

    }



    private static void recuperarDocumento(){
        System.out.println("Recuperando el documento...");
    }
*/
