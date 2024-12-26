import java.util.Scanner;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;


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
        ficheroTrustStore = raiz + "/keyStoreCliente/trustStoreClient1.jce";

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

<<<<<<< HEAD
    private static void registrarDocumento(String host, int port){
		String pathFile = preguntaUsuario("Introduce el directorio del archivo que deseas enviar: ");  // /home/alba/24-25/SEG/SEG-2425/textosPrueba/textoclaro.txt
        
		//Creación de socket
        iniciarConexionTLS(host, port, pathFile);
		
=======
    private static void registrarDocumento(){
        iniciarConexionTLS("localhost",8090, 1);

>>>>>>> d14de11 (Co-authored-by: PiterDev <PiterWeb@users.noreply.github.com> Commit Brutal clasificando para la practica)
		System.out.println("Registrando documento...");
    }

	private static String preguntaUsuario(String mensaje){
        String respuesta;
		Scanner scanner = new Scanner(System.in);

<<<<<<< HEAD
		System.out.print(mensaje);
        respuesta = scanner.nextLine();

		return respuesta;
    }

	/*******************************************************
	 *              inicializar conexion TLS
	 *******************************************************/
    private static void iniciarConexionTLS(String host, int port, String pathFile){
=======
    private static void iniciarConexionTLS(String host, int port, int idOperacion){
>>>>>>> d14de11 (Co-authored-by: PiterDev <PiterWeb@users.noreply.github.com> Commit Brutal clasificando para la practica)
		try{
			// SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();

			System.out.println ("Crear socket");
			// SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
			
			// Ver las suites SSL disponibles
			System.out.println ("CypherSuites");
			SSLContext context = SSLContext.getDefault();
			SSLSocketFactory sf = context.getSocketFactory();
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JCEKS");

			ks.load(new FileInputStream(keyStorePathCliente), contrasinal);
			kmf.init(ks, contrasinal);
				
			ctx.init(kmf.getKeyManagers(), null, null);

			// Asignamos un socket al contexto.

			SSLSocketFactory factory = ctx.getSocketFactory();
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
				
			String[] cipherSuites = sf.getSupportedCipherSuites();

			for (int i=0; i<cipherSuites.length; i++); //System.out.println (cipherSuites[i]);

			System.out.println ("Comienzo SSL Handshake");

			socket.startHandshake();
				
			System.out.println ("Fin SSL Handshake");

			if (idOperacion == 1){
				String pathFile = preguntaUsuario("Introduce el directorio del archivo que deseas enviar: ");

				SSLSession session = socket.getSession();
        		java.security.cert.Certificate[] localcerts = session.getLocalCertificates();

				java.security.cert.Certificate localCert = localcerts[0];

			}
			else{
				
			}

			PrintWriter out = new PrintWriter(
							new BufferedWriter(
							new OutputStreamWriter(
							socket.getOutputStream())));

			out.println("GET " + "/" + pathFile  + " "  + " HTTP/1.0");
			out.println();
			out.flush();

			System.out.println("GET " + "/" + pathFile  + " " + "HTTP/1.0");
			/*
			* Make sure there were no surprises
			*/
			if (out.checkError())
				System.out.println("SSLSocketClient:  java.io.PrintWriter error");

			// Leer respuesta 
			BufferedReader in = new BufferedReader(
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

    }

	/******************************************************
	 * definirKeyStores()
	 *****************************************************/
    
	private static void definirKeyStores() {
        // Almacen de credenciales
      //  System.setProperty("javax.net.ssl.keyStore", ficheroKeyStore);
      //  System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
      //  System.setProperty("javax.net.ssl.keyStorePassword", contrasinal);

        // Almacen de confianza
		System.setProperty("javax.net.ssl.trustStore", ficheroTrustStore);
		System.setProperty("javax.net.ssl.trustStoreType",     "JCEKS");
		System.setProperty("javax.net.ssl.trustStorePassword", contrasinal);

    }
 
}


