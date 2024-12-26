import java.io.Serializable;

class MensajeRegistrarDocumento implements Serializable {

    private byte[] nombreDocumento;
    private byte[] documentoCifrado;
    private byte[] claveSimetricaCifrada;
    private byte[] parametrosCifrado;
    private byte[] firmaDocumento;
    private byte[] certificadoFirmaC;
    private byte[] certificadoCifradoC;

    public RegistrarDocumento() {}

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

    public []byte crearClaveSimetrica() {
        // Generarla
        KeyGenerator kgen = KeyGenerator.getInstance(algoritmo);
        kgen.init(longclave);

        SecretKey skey = kgen.generateKey();

        // Almacenarla
        return skey.getEncoded();
    }

}

