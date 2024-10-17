import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import java.lang.*;
import java.util.Base64;


public class FirmaDigital {
    public static void main(String[] args) throws Exception { 
        FileInputStream fmensaje = new FileInputStream("./pikachu.jpg");      
    
        String provider = "SunJCE"; 
        String algoritmo = "MD5withRSA"; 
        String algoritmo_base = "RSA";    
        int longitud_clave = 2048;         
        int longbloque;

        byte bloque[] = new byte[1024];
        long filesize = 0;

        // Crea generador de claves   
	   KeyPairGenerator keyPairGen; 
	   keyPairGen = KeyPairGenerator.getInstance(algoritmo_base); 	
	     
	   // Crea generador de claves 
       keyPairGen.initialize(longitud_clave);
    
	   // Generamos un par de claves (publica y privada)
       KeyPair     keypair    = keyPairGen.genKeyPair();
       PrivateKey  privateKey = keypair.getPrivate();
       PublicKey   publicKey  = keypair.getPublic();
       
       //Guardar claves
       // Convertir la clave pública a formato Base64
       String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

       // Guardar la clave pública en un archivo
       try (FileWriter fileWriter = new FileWriter("clavePublica.txt")) {
           fileWriter.write(publicKeyString);
       } catch (IOException e) {
           e.printStackTrace();
       }

       // Convertir la clave privada a formato Base64
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        // Guardar la clave privada en un archivo
        try (FileWriter fileWriter = new FileWriter("clavePrivada.txt")) {
            fileWriter.write(privateKeyString);
        } catch (IOException e) {
            e.printStackTrace();
        }

       /* Visualizar pareja claves */

       System.out.println("*** CLAVES PRIVADA ***");	System.out.println(privateKey);
       System.out.println("*** CLAVES PUBLICA ***");	System.out.println(publicKey);
     
       System.out.println("*** FIRMA    *** ");
 
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
	
	   System.out.println("*** Fin Firma. La firma es: ");
	   for (int i=0; i<firma.length; i++)
           System.out.print(firma[i] + " ");
	       System.out.println();

	       fmensaje.close();
        

        try (FileOutputStream firma_file = new FileOutputStream("./firma.txt")) {
            firma_file.write(firma);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*FileOutputStream firma_file        = new FileOutputStream("./firma.txt");

        firma_file.write(firma);

        firma_file.close();*/
    }

}