import java.io.*;
import java.net.*;
import java.security.KeyStore;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class Servidor {
    //Atributos
    private static String 	raiz = "./keyStoreServidor/";
    private final String contrasinal = "criptonika";

    //Clases
    public static void main(String[] args){
        
        if(args.length > 4 || args.length < 2){
            System.out.println("USAGE: java Cliente " + "host port");
			System.exit(-1);
        }

        iniciarServidor();
        



    }

    //Metodos
    static void iniciarServidor(){
        try{
            System.out.print("Iniciando el sistema");
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
	    System.setProperty("javax.net.ssl.keyStoreType",     "JCEKS");
	    System.setProperty("javax.net.ssl.keyStorePassword", contrasinal);
	
	    // Almacen de confianza	    
	    System.setProperty("javax.net.ssl.trustStore", raiz + "/trustStoreServer.jce");
	    System.setProperty("javax.net.ssl.trustStoreType",     "JCEKS");
	    System.setProperty("javax.net.ssl.trustStorePassword", contrasinal);
	}

}

