import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.*;

public class FirmaAsimetricaKeyStore {

    public static void main(String[] args) throws Exception { 

		FileInputStream fmensaje   = new    FileInputStream("./hot_wheels.jpg");      

		String 		provider         = "SunJCE"; 
		String 		algoritmo        = "SHA1withRSA"; 
		int    		longbloque;
		byte   		bloque[]         = new byte[1024];
		long   		filesize         = 0;
		
		// Variables para el KeyStore

		KeyStore    ks;
		char[]      ks_password  	= "criptonika".toCharArray();
		char[]      key_password 	= "criptonika".toCharArray();
		String		ks_file			= "./keyStoreCliente/keyStoreClient1.jce";	    
		String		entry_alias		= "client1";
			
		System.out.println("******************************************* ");
		System.out.println("*               FIRMA                     * ");
		System.out.println("******************************************* ");

		// Obtener la clave privada del keystore
				
		ks = KeyStore.getInstance("JCEKS");

		ks.load(new FileInputStream(ks_file),  ks_password);

		KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)
											ks.getEntry(entry_alias, 
											new KeyStore.PasswordProtection(key_password));
	
		PrivateKey privateKey = pkEntry.getPrivateKey();

		// Visualizar clave privada

		System.out.println("*** CLAVE PRIVADA ***");	
		System.out.println("Algoritmo de Firma (sin el Hash): " + privateKey.getAlgorithm());
		System.out.println(privateKey.getEncoded());

		// Creamos un objeto para firmar/verificar
		
		Signature signer = Signature.getInstance(algoritmo);

		// Inicializamos el objeto para firmar
		signer.initSign(privateKey);
		
		// Para firmar primero pasamos el hash al mensaje (metodo "update")
		// y despues firmamos el hash (metodo sign).

		byte[] firma = null;
		
		while ((longbloque = fmensaje.read(bloque)) > 0) {
			filesize = filesize + longbloque;    		     
			signer.update(bloque,0,longbloque);
		}  

		firma = signer.sign();
		
		double  v = firma.length;
		
		System.out.println("*** FIRMA: ****");
		for (int i=0; i<firma.length; i++)
		
			System.out.print(firma[i] + " ");
		System.out.println();
		System.out.println();

		fmensaje.close();
		
	///	System.out.println("Mi firma es:" + firma.length());

		try (FileOutputStream firma_file = new FileOutputStream("./firma.txt")) {
				firma_file.write(firma);
				firma_file.close();
		} catch (IOException e) {
				e.printStackTrace();
		}


    }
}