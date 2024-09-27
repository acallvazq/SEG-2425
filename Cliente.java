import java.util.Scanner;

public class Cliente {
    
    //Atributos
    
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
                "  A) 📝 Registrar un documento       \n" +
                "  B) 📖 Recuperar un documento       \n" +
                "  S) 🚪 Salir                        \n");

            System.out.print("Seleccione una opcion: ");

            // Lee la respuesta del usuario y escoge opcion
            respuesta = scanner.nextLine().toUpperCase();

            switch(respuesta){
                case "A":
                    System.out.println("Registrando documento...");
                    break;
                case "B":
                    System.out.println("Recuperando el documento...");
                    break;
                case "S":
                    System.out.println("Saliendo del sistema...");
                    salir = 0;
                    break;
            }

        }while(salir == 1);
        

    }

    //Metodos


}