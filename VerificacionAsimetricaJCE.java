import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import java.lang.*;
import java.util.Base64;

public class VerificacionAsimetricaJCE {
    public static void main(String[] args) throws Exception { 
        String provider         = "SunJCE"; 

        String algoritmo        =  "MD5withRSA"; 
        String algoritmo_base   =  "RSA";    
        int    longitud_clave   =  2048;         
        int    longbloque;

        byte[] firma = null;
        byte   bloque[]         = new byte[1024];
        long   filesize         = 0;



        // Leer la clave pública desde el archivo
            StringBuilder publicKeyString = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader("clavePublica.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    publicKeyString.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Convertir la cadena Base64 a PublicKey
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString.toString());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

        // Leer la clave privada desde el archivo
            StringBuilder privateKeyString = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader("clavePrivada.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    privateKeyString.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Convertir la cadena Base64 a PrivateKey
            byte[] keyBytes2 = Base64.getDecoder().decode(privateKeyString.toString());
            PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(keyBytes2);
            keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(spec2);

    		System.out.println("*** FIRMA    *** ");*/
            File archivo = new File("firma.txt");
		    try (FileInputStream fis = new FileInputStream(archivo)) {
                // Obtener el tamaño del archivo
                long longitud = archivo.length();
                firma = new byte[(int) longitud];

                // Leer el archivo en el arreglo de bytes
                int bytesLeidos = fis.read(firma);
            } catch (IOException e) {
                e.printStackTrace();
            }

		// Creamos un objeto para firmar/verificar
		
		Signature signer = Signature.getInstance(algoritmo);

		// Inicializamos el objeto para firmar

		signer.initSign(privateKey);
		
		// Para firmar primero pasamos el hash al mensaje (metodo "update")
		// y despues firmamos el hash (metodo sign).

        /*******************************************************************
	    *       Verificacion
	    ******************************************************************/
	    System.out.println("*** VERIFICACION *** ");

    	FileInputStream fmensajeV   = new FileInputStream("./pikachu.jpg");      

	    byte[] privateBytes = privateKey.getEncoded();
	    byte[] publicBytes  = publicKey.getEncoded();

    	//
	    System.out.println("Longitud Privada = " + privateBytes.length);
	    System.out.println("Longitud Publica = " + publicBytes.length);

	    // Creamos un objeto para verificar
	    Signature verifier=Signature.getInstance(algoritmo);	 
	
    	//**** Para verificar usamos la clave Publica *******
	    // (por defecto las claves publicas se almacenan en formato X.509)

	    EncodedKeySpec keySpec;
	    if (publicKey.getFormat().equals("X.509"))
		    keySpec = new X509EncodedKeySpec (publicBytes);
	    else
		    keySpec = new PKCS8EncodedKeySpec(publicBytes);
    

    keyFactory = KeyFactory.getInstance(algoritmo_base);
    PublicKey  publicKey2 = keyFactory.generatePublic(keySpec);


	// Inicializamos el objeto
	
    verifier.initVerify(publicKey2);
    
    while ((longbloque = fmensajeV.read(bloque)) > 0) {
        filesize = filesize + longbloque;    		     
    	verifier.update(bloque,0,longbloque);
    }  

	boolean resultado = false;

	resultado = verifier.verify(firma);
	
	if (resultado == true) 
	    System.out.print("Firma CORRECTA");
	else
	    System.out.print("Firma NO correcta");	    
	
	fmensajeV.close();
    }

}