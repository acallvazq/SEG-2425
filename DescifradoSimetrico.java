import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.AlgorithmParameters;
import java.util.ArrayList;

public class DescifradoSimetrico {
    public static void main(String[] args) throws Exception {
        String provider = "SunJCE"; 
        String algoritmo = "AES"; 
        String transformacion = "/CBC/PKCS5Padding";

        int longclave = 128;
        int longbloque;

        FileInputStream ftextoclaro = new FileInputStream ("./textosPrueba/textoclaro.txt");
        FileInputStream  fclave_in = new FileInputStream ("./textosPrueba/fclaveks.txt");

        FileInputStream  ftextocifrado2 = new FileInputStream ("./textosPrueba/textocifrado.txt");
        FileOutputStream ftextoclaro2 = new FileOutputStream("./textosPrueba/textoclaro2.txt");
	    FileInputStream  fparametros_in = new FileInputStream ("./textosPrueba/parametros.txt");

        byte bloquecifrado2[] = new byte[1024];
        byte bloqueclaro2[] = new byte[1048];

        /************************************************************
			                     Leer la clave 
	    ************************************************************/
        ArrayList<Byte> skey_raw = new ArrayList<>();      
        int byteLeido;

        while ((byteLeido = fclave_in.read()) != -1) {
            // Convertir el int leído a Byte y añadirlo a la lista
            skey_raw.add((byte)byteLeido);
        }

        byte[] skey_raw_leido = new byte[skey_raw.size()];

        for(int i = 0; i < skey_raw.size(); i++){
            skey_raw_leido[i] = skey_raw.get(i);
        }

	    // Leerla
        SecretKeySpec ks = new SecretKeySpec(skey_raw_leido, algoritmo); 

        //*****************************************************************************
        //					DESCIFRAR
        //*****************************************************************************
        System.out.println("*************** INICIO DESCIFRADO *****************" );

        Cipher descifrador = Cipher.getInstance(algoritmo + transformacion, provider);

        // Leer los parametros si el algoritmo soporta parametros
        if (provider.equals("SunJCE") && 
                ( algoritmo.equals("AES")                    || 
        		  algoritmo.equals("Blowfish")               || 
        		  algoritmo.equals("DES")                    || 
        		  algoritmo.equals("DESede")                 || 
        		  algoritmo.equals("DiffieHellman")          || 
        		  algoritmo.equals("OAEP")                   || 
        		  algoritmo.equals("PBEWithMD5AndDES")       || 
        		  algoritmo.equals("PBEWithMD5AndTripleDES") || 
        		  algoritmo.equals("PBEWithSHA1AndDESede")   || 
        		  algoritmo.equals("PBEWithSHA1AndRC2_40")   || 
        		  algoritmo.equals("RC2")
			  // -- Aqui se introducirian otros algoritmos
        		  ) )  
        {
            AlgorithmParameters params = AlgorithmParameters.getInstance(algoritmo,provider);        
            byte[] paramSerializados = new byte[fparametros_in.available()];

            int bytesleidos = fparametros_in.read(paramSerializados); 

            //System.out.println(bytesleidos + "-" + fparametros_in.available());

            params.init(paramSerializados);

            System.out.println("Parametros del descifrado ... = " + params.toString());

            descifrador.init(Cipher.DECRYPT_MODE, ks, params);
        
        }
        else
        {
        	descifrador.init(Cipher.DECRYPT_MODE, ks);
        }
        

        while ((longbloque = ftextocifrado2.read(bloquecifrado2)) > 0) {
            bloqueclaro2 = descifrador.update(bloquecifrado2,0,longbloque);
            ftextoclaro2.write(bloqueclaro2);
        }

        bloqueclaro2 = descifrador.doFinal();
 	    ftextoclaro2.write(bloqueclaro2);
        
        ftextocifrado2.close();
        ftextoclaro2.close();

        System.out.println("*************** FIN DESCIFRADO *****************" );
       
	}	
}


