import java.util.Scanner;
//import java.net.ssl.*;


public class Cliente {
    //Atributos y constantes
    private static String raiz;
    private static String ficheroKeyStore;
    private static String ficheroTrustStore;
    private static String contrasinal = "criptonika";
    
    //Clases
    public static void main (String[] args){
		String host = null;
		int port = 9001;
        
        // Comprueba los argumentos
		if (args.length < 2 || args.length > 3) {
			System.out.println("USAGE: java Cliente " + "host port");
			System.exit(-1);
		}
		host = args[0];
		port = Integer.parseInt(args[1]);

		//Directorio de trabajo
		raiz = System.getProperty("user.dir");
		ficheroKeyStore = raiz + "/keyStoreCliente/keyStoreClient1.jce";
        ficheroTrustStore = raiz + "/keyStoreCliente/trustStoreClient.jce";

		definirKeyStores();

		menu();
      

    }

    //Metodos
	private static void menu(){
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
                    registrarDocumento();
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

    private static void registrarDocumento(){
		solicitarDatos();
        System.out.println("Registrando documento...");

    }

    public static void solicitarDatos(){
		String pathFile = preguntaUsuario("Introduce el directorio del archivo que deseas enviar: ");  // /home/alba/24-25/SEG/SEG-2425/textosPrueba/textoclaro.txt
		
		return;
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

    /*
    ------------------------     PENDIENTE DE REVISION     ------------------------
    
     */
/*
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

    private static void definirKeyStores() {
		// Almacen de credenciales
		System.setProperty("javax.net.ssl.keyStore", keyStorePathCliente);
		System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
		System.setProperty("javax.net.ssl.keyStorePassword", contrasinal);

		// Almacen de confianza	
		System.setProperty("javax.net.ssl.trustStore", trustStorePathCliente);
		System.setProperty("javax.net.ssl.trustStoreType", "JCEKS");
		System.setProperty("javax.net.ssl.trustStorePassword", contrasinal);

	}*/

}