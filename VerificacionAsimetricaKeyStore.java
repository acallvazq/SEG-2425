import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.crypto.*;
import java.security.spec.*;
import java.security.*;
import java.net.*;
import java.util.ArrayList;

public class VerificacionAsimetricaKeyStore {

    public static void main(String[] args) throws Exception { 

		String 		provider         = "SunJCE";
		int    		longbloque;
		byte[]   	bloque           = new byte[1024];
		long   		filesize         = 0;
		
		// Variables para el KeyStore

		KeyStore    ks;
		char[]      ks_password  	= "criptonika".toCharArray();
		char[]      key_password 	= "criptonika".toCharArray();
		String		ks_file			="./keyStoreCliente/keyStoreClient1.jce";	    
		String		entry_alias		= "client1";
		
		ks = KeyStore.getInstance("JCEKS");
		ks.load(new FileInputStream(ks_file),  ks_password);
		
		// Para firmar primero pasamos el hash al mensaje (metodo "update")
		// y despues firmamos el hash (metodo sign).

		FileInputStream firma_file = new FileInputStream("./firma.txt");  

		ArrayList<Byte> firma_raw = new ArrayList<>();      
        int byteLeido;
        while ((byteLeido = firma_file.read()) > 0) {
            // Convertir el int leído a Byte y añadirlo a la lista
            firma_raw.add((byte)byteLeido);
        }

        byte[] firma = new byte[firma_raw.size()];

        for(int i = 0; i < firma_raw.size(); i++){
            firma[i] = firma_raw.get(i);
        }

		firma_file.close();

		/*******************************************************************
		 *       Verificacion
		 ******************************************************************/
		System.out.println("************************************* ");
		System.out.println("        VERIFICACION                  ");
		System.out.println("************************************* ");

		FileInputStream fmensajeV = new FileInputStream("./hot_wheels.jpg");        


		// Obtener la clave publica del keystore
		PublicKey publicKey = ks.getCertificate(entry_alias).getPublicKey();

		System.out.println("*** CLAVE PUBLICA ***");	
		System.out.println(publicKey);
		
		// Obtener el usuario del Certificado tomado del KeyStore.
		//   Hay que traducir el formato de certificado del formato del keyStore
		//	 al formato X.509. Para eso se usa un CertificateFactory.
		
		byte[] certificadoRaw  = ks.getCertificate(entry_alias).getEncoded();    
		ByteArrayInputStream inStream = null;
		inStream = new ByteArrayInputStream(certificadoRaw);
		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
	
		System.out.println ("CERTIFICADO: " +
					"\n -- Algoritmo Firma .... = " + cert.getSigAlgName() +
					"\n -- Usuario ............ = " + cert.getIssuerX500Principal() +
					"\n -- Parametros Algoritmo = " + cert.getSigAlgParams() +
					"\n -- Algoritmo de la PK.. = " + cert.getPublicKey().getAlgorithm() +
					"\n -- Formato  ........... = " + cert.getPublicKey().getFormat() +
					"\n -- Codificacion ....... = " + VerificacionAsimetrica.bytesToHex(cert.getPublicKey().getEncoded())		   						
				);       
		
		// Creamos un objeto para verificar, pasandole el algoritmo leido del certificado.
		Signature verifier = Signature.getInstance("SHA1withRSA");
		
		// Inicializamos el objeto para verificar
		
		verifier.initVerify(publicKey);
		
		while ((longbloque = fmensajeV.read(bloque)) > 0) {
			filesize = filesize + longbloque;    		     
			verifier.update(bloque,0,longbloque);
		}

		boolean resultado = false;
		
		System.out.println("Mi firma es:");
		
		for (byte firmaByte: firma) {
			System.out.print(firmaByte + " ");
		}
        System.out.print("\n");

		resultado = verifier.verify(firma);
				
		System.out.println();
		if (resultado)
			System.out.println("Verificacion correcta de la Firma");
		else
			System.out.println("Fallo de verificacion de firma");	    
		
		fmensajeV.close();

    }

	public static String bytesToHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
	}



}
