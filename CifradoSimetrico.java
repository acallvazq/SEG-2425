import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.AlgorithmParameters;

public class CifradoSimetrico {

        public static void main(String[] args) throws Exception {
    
                String provider         = "SunJCE";
                
                FileInputStream  ftextoclaro   = new FileInputStream ("./textosPrueba/textoclaro.txt");
                FileInputStream  fclave_in     = new FileInputStream ("./textosPrueba/fclaveks.txt");

                FileOutputStream ftextocifrado = new FileOutputStream("./textosPrueba/textocifrado.txt");
                FileOutputStream fparametros   = new FileOutputStream("./textosPrueba/parametros.txt");
                FileOutputStream fclave        = new FileOutputStream("./textosPrueba/fclaveks.txt");

                BufferedWriter   fmedidas      = new BufferedWriter(new FileWriter("./textosPrueba/medidasCifrado.txt"));
                
                byte   bloqueclaro[]    = new byte[2024];
                byte   bloquecifrado[]  = new byte[2048];
                String algoritmo        = "AES";
                String transformacion   = "/CBC/PKCS5Padding";
                int    longclave        = 128;
                int    longbloque;
                int    i;
                double t, tbi,tbf;		// tiempos totales y por bucle
                double lf;              // longitud del fichero

                // Generarla
                KeyGenerator kgen = KeyGenerator.getInstance(algoritmo);
                kgen.init(longclave);

                SecretKey skey = kgen.generateKey();

                // Almacenarla
                byte[] skey_raw = skey.getEncoded();
                fclave.write(skey_raw);
                fclave.close();

                // Leerla
                fclave_in.read(skey_raw);
                SecretKeySpec ks = new SecretKeySpec(skey_raw, algoritmo);

                /** --- Cifrado --- */

                Cipher cifrador = Cipher.getInstance(algoritmo + transformacion);

                System.out.println("*** INICIO CIFRADO " + algoritmo + "-" + longclave + " ************");

                cifrador.init(Cipher.ENCRYPT_MODE, ks);

                i = 0;
                t = 0;
                lf = 0;
                tbi = 0;
                tbf = 0;

                while ((longbloque = ftextoclaro.read(bloqueclaro)) > 0) {

                        i++;

                        lf = lf + longbloque;

                        tbi = System.nanoTime();
                        bloquecifrado = cifrador.update(bloqueclaro, 0, longbloque);
                        tbf = System.nanoTime();

                        t = t + tbf - tbi;

                        fmedidas.write("T.iteracion" + i + "=" + (tbf-tbi) + "nanoseg\n");

                        ftextocifrado.write(bloquecifrado);

                }

                tbi = System.nanoTime();
                bloquecifrado = cifrador.doFinal();
                tbf = System.nanoTime();

                t = t + tbf - tbi;

                ftextocifrado.write(bloquecifrado);

                // Resultados

                System.out.println("*** FIN CIFRADO" + algoritmo + "-" + longclave + " Provider: " + provider);
                System.out.println("Bytes cifrados=" + (int)lf);
                System.out.println("Tiempo cifrado=" + t/1000000 + " mseg");
                System.out.println("Velocidad   =" + (lf*8*1000)/t + " Mbps");

                // Cerrar ficheros

                ftextocifrado.close();
                ftextoclaro.close();
                fmedidas.close();

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
        		  ) )  
                {


                        AlgorithmParameters param = AlgorithmParameters.getInstance(algoritmo);
                        param = cifrador.getParameters();

                        System.out.println("Parametro del cifrado..." + param.toString());

                        byte[] paramSerializados = param.getEncoded();
                        System.out.println(paramSerializados);
                        fparametros.write(paramSerializados);
                        fparametros.close();


                }

        }

}