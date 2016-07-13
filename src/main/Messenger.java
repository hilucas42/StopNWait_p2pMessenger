package main;

import interfaces.*;
import ctp.ARQ;
import java.util.Scanner;

/**
 *
 * @author Lucas
 */
public class Messenger {

    private Messenger() {
        iFaceTx = new Tx();
        arq = new ARQ(iFaceTx, this);
        iFaceRx = new Rx(12345, arq);
        teclado = new Scanner(System.in);
    }
    
    private void run() {
        
        Thread arqT = new Thread(arq);
        new Thread(iFaceTx).start();
        arqT.start();
        arq.setThreadReference(arqT);
        new Thread(iFaceRx).start();
        
        System.out.print("Digite o endereço de destino: ");
        String address = teclado.nextLine();
        
        while(teclado.hasNextLine()) {
            
            String input = teclado.nextLine();
            if(input.charAt(0) == '/' && input.length() > 1)
                runCommand(input.charAt(1));
            else 
                arq.sendMessage(input, address, 12346);
        }
    }
    
    public void receiveMessage(String message) {
        
        System.out.println(message);
    }
    
    private void runCommand(char command) {
        
        switch(command) {
            case 'v':
                Messenger.verbose = true;
                break;
            case 't':
                Messenger.verbose = false;
                break;
            case 'c':
                break;
            case 'b':
                break;
            default:
                System.out.println("Comandos válidos:");
                System.out.println("\t/verbose\t- Ativa o modo verboso");
                System.out.println("\t/transparent\t- Desativa o modo verboso");
                System.out.println("\t/binaryStream\t- Mostra as saídas binárias");
                System.out.println("\t/clear\t- Limpa a tela");
        }
    }
    
    public static void main(String[] args) {
        
        new Messenger().run();
    }
    
    private final Scanner teclado;
    private final Tx iFaceTx;
    private final ARQ arq;
    private final Rx iFaceRx;
    public static boolean verbose;
}
