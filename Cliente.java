import java.util.Scanner;

public class Cliente {
    
    //Atributos
    private final String keyStore = "keyStore";
    private final String trustStore = "trustStore";
    private final String keyStorePathCliente = "./keyStoreCliente";
    private final String trustStorePathCliente = "./trustStoreCliente";
    private final String contrasinal = "abc123.";
    
    //Clases
    public static void main (String[] args){
        String respuesta;
        int salir = 1;
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("\n" + 
                "╔════════════════════════════════════╗\n" +
                "║        GESTOR DE DOCUMENTOS        ║\n" +
                "╚════════════════════════════════════╝\n" +
                "  1) 📝 Registrar un documento       \n" +
                "  2) 📖 Recuperar un documento       \n" +
                "  X) 🚪 Salir                        \n");

            System.out.print("Seleccione una opcion: ");

            // Lee la respuesta del usuario y escoge opcion
            respuesta = scanner.nextLine().toUpperCase();

            switch(respuesta){
                case "1":
                    registrarDocumento();
                    break;
                case "2":
                    recuperarDocumento();   
                    break;
                case "X":
                    System.out.println("Saliendo del sistema...");
                    salir = 0;
                    break;
            }

        }while(salir == 1);
        

    }

    //Metodos
    private static void registrarDocumento(){
        System.out.println("Registrando documento...");

    }

    private static void recuperarDocumento(){
        System.out.println("Recuperando el documento...");
    }

}