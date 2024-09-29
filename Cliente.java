import java.util.Scanner;
import java.net.ssl.*;


public class Cliente {
    //Atributos

    //Constantes
    private final String raiz = "./";
    private final String keyStore = "keyStore";
    private final String trustStore = "trustStore";
    private final String keyStorePathCliente = "./keyStoreCliente";
    private final String trustStorePathCliente = "./trustStoreCliente";
    private final String contrasinal = "abc123.";
    
    //Clases
    public static void main (String[] args){
        String respuesta;
        int salir = 1;
        Scanner scanner = new Scanner(System.in);

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
                    recuperarDocumento();   
                    break;
                case "X":
                    System.out.println("Saliendo del sistema...");
                    salir = 0;
                    break;
            }

        }while(salir == 1);
        

    }

    //Metodos

    /*
        Registro de documentos
     */
    private static void registrarDocumento(){
        //Iniciar conexion SSL
        iniciarConexionSSL();
        System.out.println("Registrando documento...");

    }
    /*
    ------------------------     PENDIENTE DE REVISION     ------------------------
    
     */

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

				factory = ctx.getSocketFactory();

				/*********************************************************************
				 * Suites del contexto
				 *********************************************************************/
				System.out.println("******** CypherSuites Disponibles **********");
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
				System.out.println("SSLSocketClient: java.io.PrintWriter error");

			/* Leer respuesta */
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

	}

}