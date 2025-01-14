import java.io.Serializable;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Signature;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;

class MensajeRegistrarDocumento implements Serializable {

    private byte[] nombreDocumento;
    private byte[] documentoCifrado;
    private byte[] claveSimetricaCifrada;
    private byte[] parametrosCifrado;
    private byte[] firmaDocumento;
    private byte[] certificadoFirmaC;
    private byte[] certificadoCifradoC;

    public MensajeRegistrarDocumento() {}

    public byte[] getNombreDocumento() {
        return nombreDocumento;
    }

    public void setNombreDocumento(byte[] nombreDocumento) {
        this.nombreDocumento = nombreDocumento;
    }

    public byte[] getDocumentoCifrado() {
        return documentoCifrado;
    }

    public void setDocumentoCifrado(byte[] documentoCifrado) {
        this.documentoCifrado = documentoCifrado;
    }

    public byte[] getClaveSimetricaCifrada() {
        return claveSimetricaCifrada;
    }

    public void setClaveSimetricaCifrada(byte[] claveSimetricaCifrada) {
        this.claveSimetricaCifrada = claveSimetricaCifrada;
    }

    public byte[] getParametrosCifrado() {
        return parametrosCifrado;
    }

    public void setParametrosCifrado(byte[] parametrosCifrado) {
        this.parametrosCifrado = parametrosCifrado;
    }

    public byte[] getFirmaDocumento() {
        return firmaDocumento;
    }

    public void setFirmaDocumento(byte[] firmaDocumento) {
        this.firmaDocumento = firmaDocumento;
    }

    public byte[] getCertificadoFirmaC() {
        return certificadoFirmaC;
    }

    public void setCertificadoFirmaC(byte[] certificadoFirmaC) {
        this.certificadoFirmaC = certificadoFirmaC;
    }

    public byte[] getCertificadoCifradoC() {
        return certificadoCifradoC;
    }

    public void setCertificadoCifradoC(byte[] certificadoCifradoC) {
        this.certificadoCifradoC = certificadoCifradoC;
    }

    public byte[] cifrarDocumento(FileInputStream documento, byte[] claveSimetrica) {

        try {
            byte[] bloqueclaro = new byte[2024];
            byte[] bloquecifrado = new byte[2048];
            String algoritmo = "AES";
            String transformacion = "/CBC/PKCS5Padding";
            int longbloque;                   // longitud del fichero

            FileOutputStream documentoCifrado = new FileOutputStream("./documento_cifrado.txt");

            SecretKeySpec ks = new SecretKeySpec(claveSimetrica, algoritmo);

            /** --- Cifrado --- */
            Cipher cifrador = Cipher.getInstance(algoritmo + transformacion);

            cifrador.init(Cipher.ENCRYPT_MODE, ks);

            while ((longbloque = documento.read(bloqueclaro)) > 0) {

                bloquecifrado = cifrador.update(bloqueclaro, 0, longbloque);

                documentoCifrado.write(bloquecifrado);

            }

            bloquecifrado = cifrador.doFinal();
            documentoCifrado.write(bloquecifrado);

            documentoCifrado.close();

            AlgorithmParameters param = AlgorithmParameters.getInstance(algoritmo);
            param = cifrador.getParameters();

            this.setParametrosCifrado(param.getEncoded());

            return Cliente.getBytes("./documento_cifrado.txt");
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    public byte[] descifrarDocumento(FileInputStream documento, byte[] claveSimetrica) {

        try {
            byte[] bloqueclaro = new byte[2024];
            byte[] bloquecifrado = new byte[2048];
            String algoritmo = "AES";
            String transformacion = "/CBC/PKCS5Padding";
            int longbloque;                   // longitud del fichero

            FileOutputStream documentoDescifrado = new FileOutputStream("./documento_descifrado.txt");

            SecretKeySpec ks = new SecretKeySpec(claveSimetrica, algoritmo);

            /** --- Descifrado --- */
            Cipher cifrador = Cipher.getInstance(algoritmo + transformacion, "SunJCE");
           
            AlgorithmParameters params = AlgorithmParameters.getInstance(algoritmo,"SunJCE");     

            params.init(this.getParametrosCifrado());

            cifrador.init(Cipher.DECRYPT_MODE, ks, params);

            while ((longbloque = documento.read(bloquecifrado)) > 0) {

                bloqueclaro = cifrador.update(bloquecifrado, 0, longbloque);
                documentoDescifrado.write(bloqueclaro);

            }

            bloqueclaro = cifrador.doFinal();
            documentoDescifrado.write(bloqueclaro);

            documentoDescifrado.close();

            return Cliente.getBytes("./documento_descifrado.txt");
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    public static byte[] cifrarClaveSimetrica(byte[] claveSimetrica, PublicKey clavePublica) {

        try {
            String algoritmo                = "RSA";
            String transformacion = "/ECB/OAEPPadding"; // Este relleno tiene una longitud mayor y es variable
            int longclave                   = 1024;       // NOTA -- Probar a subir este valor e ir viendo como

            int longbloque;                            // longitud del fichero
            byte[] bloqueclaro = new byte[longclave/8];
            byte[] bloquecifrado = new byte[512];

            Cipher cifrador = Cipher.getInstance(algoritmo + transformacion);

            cifrador.init(Cipher.ENCRYPT_MODE, clavePublica);

            FileOutputStream claveSimetricaOutputFile = new FileOutputStream("./clave_simetrica.txt");

            claveSimetricaOutputFile.write(claveSimetrica);

            claveSimetricaOutputFile.close();

            FileInputStream claveSimetricaInputFile = new FileInputStream("./clave_simetrica.txt");
            FileOutputStream claveSimetricaCifradaOutputFile = new FileOutputStream("./clave_simetrica_cifrada.txt");

            while ((longbloque = claveSimetricaInputFile.read(bloqueclaro)) > 0) {
                            
                bloquecifrado = cifrador.update(bloqueclaro, 0, longbloque);
                bloquecifrado = cifrador.doFinal();

                claveSimetricaCifradaOutputFile.write(bloquecifrado);
            }

            claveSimetricaInputFile.close();
            claveSimetricaCifradaOutputFile.close();

            return Cliente.getBytes("./clave_simetrica_cifrada.txt");
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    public static byte[] firmarDocumento(FileInputStream documento, PrivateKey clavePrivada) {

        String algoritmo = "SHA1withRSA";

        try {
            Signature signer = Signature.getInstance(algoritmo);

            // Inicializamos el objeto para firmar
            signer.initSign(clavePrivada);
            
            // Para firmar primero pasamos el hash al mensaje (metodo "update")
            // y despues firmamos el hash (metodo sign).

            byte[] firma = null;
            
            int longbloque, filesize;
            byte   		bloque[]         = new byte[1512];

            filesize = 0;

            while ((longbloque = documento.read(bloque)) > 0) {
                filesize = filesize + longbloque;    		     
                signer.update(bloque,0,longbloque);
            }  

            firma = signer.sign();

            //documento.close();
            return firma;
        } catch(Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    // public static boolean verificarFirma(PublicKey publicKey, byte[] firma, FileInputStream documento) {

    //     String algoritmo        =  "MD5withRSA"; // Algoritmo de firma
    //     String algoritmo_base   =  "RSA";  
        
    //     Signature verifier = Signature.getInstance(algoritmo);	 

    //     EncodedKeySpec keySpec;
	//     if (publicKey.getFormat().equals("X.509"))
	// 	    keySpec = new X509EncodedKeySpec (publicKey.getEncoded());
	//     else
	// 	    keySpec = new PKCS8EncodedKeySpec(publicKey.getEncoded());

    //     keyFactory = KeyFactory.getInstance(algoritmo_base);
    //     PublicKey  publicKey2 = keyFactory.generatePublic(keySpec);

    //     verifier.initVerify(publicKey2);

    //     while ((longbloque = documento.read(bloque)) > 0) {		     
    // 	    verifier.update(bloque,0,longbloque);
    //     }  

    //     return verifier.verify(firma);
    // }

    public static byte[] descifrarClaveSimetrica(FileInputStream claveSimetricaCifrada, PrivateKey clavePrivada) {

        try {
            String algoritmo = "RSA";
            String transformacion = "/ECB/OAEPPadding";
            int longbloque;                   // longitud del fichero
            byte[] bloqueclaro = new byte[512];
            byte[] bloquecifrado = new byte[512];

            Cipher cifrador = Cipher.getInstance(algoritmo + transformacion);

            cifrador.init(Cipher.DECRYPT_MODE, clavePrivada);

            FileOutputStream claveSimetricaDescifradaOutputFile = new FileOutputStream("./clave_simetrica_descifrada.temp");

            while ((longbloque = claveSimetricaCifrada.read(bloquecifrado)) > 0) {
                bloqueclaro = cifrador.update(bloquecifrado, 0, longbloque);
                bloqueclaro = cifrador.doFinal();
                claveSimetricaDescifradaOutputFile.write(bloqueclaro);
            }

            claveSimetricaDescifradaOutputFile.close();

            byte[] clave_simetrica_descifrada = Cliente.getBytes("./clave_simetrica_descifrada.temp");

            new File("./clave_simetrica_descifrada.temp").delete();

            return clave_simetrica_descifrada;
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    public byte[] convertToBytes(Object object) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos)) {
        out.writeObject(object);
        return bos.toByteArray();
    } 
}

}




