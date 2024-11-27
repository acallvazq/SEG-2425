
public class Servidor {
    //Atributos
    private final String keyStore = "keyStore";
    private final String trustStore = "trustStore";
    private final String keyStorePathServidor = "./keyStoreServidor";
    private final String trustStorePathServidor = "./trustStoreServidor";
    private final String contrasinal = "abc123.";

    //Clases
    public static void main(String[] args){
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

}