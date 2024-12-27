import java.io.Serializable;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.security.PublicKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.crypto.spec.SecretKeySpec;

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

    public static byte[] cifrarDocumento(FileInputStream documento, byte[] claveSimetrica) {

        try {
            byte bloqueclaro[] = new byte[2024];
            byte bloquecifrado[] = new byte[2048];
            String algoritmo = "AES";
            String transformacion = "/CBC/PKCS5Padding";
            int longclave = 128;
            int longbloque;
            int i;
            double lf;                      // longitud del fichero

            FileOutputStream documentoCifrado = new FileOutputStream("./documento_cifrado.txt");

            SecretKeySpec ks = new SecretKeySpec(claveSimetrica, algoritmo);

            /** --- Cifrado --- */
            Cipher cifrador = Cipher.getInstance(algoritmo + transformacion);

            cifrador.init(Cipher.ENCRYPT_MODE, ks);

            i = 0;
            lf = 0;

            while ((longbloque = documento.read(bloqueclaro)) > 0) {
                i++;

                lf = lf + longbloque;

                bloquecifrado = cifrador.update(bloqueclaro, 0, longbloque);

                documentoCifrado.write(bloquecifrado);

            }

            bloquecifrado = cifrador.doFinal();
            documentoCifrado.write(bloquecifrado);

            documentoCifrado.close();
            documento.close();

            return Cliente.getBytes("./documento_cifrado.txt");
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    public static byte[] cifrarClaveSimetrica(byte[] claveSimetrica, PublicKey clavePublica) {

        try {
            String provider = "SunJCE";
            String algoritmo                = "RSA";
            String transformacion = "/ECB/OAEPPadding"; // Este relleno tiene una longitud mayor y es variable
            int longclave                   = 1024;       // NOTA -- Probar a subir este valor e ir viendo como

            int longbloque;
            double lf = 0;                              // longitud del fichero
            byte bloqueclaro[] = new byte[longclave/8];
            byte bloquecifrado[] = new byte[512];

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

}




