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
                arq.sendMessage(input, address, 12345);
        }
    }
    
    public void receiveMessage(String message) {
        
        System.out.println(message);
    }
    
    private void runCommand(char command) {
        
        switch(command) {
            case 'v':
                Messenger.verbose = !Messenger.verbose;
                break;
            case 'i':
                Messenger.induceInterruption = !Messenger.induceInterruption;
                break;
            case 'e':
                Messenger.induceError = !Messenger.induceError;
                break;
            case 'd':
                Messenger.induceDelay = !Messenger.induceDelay;
                break;
            case 'l':
                Messenger.induceLoss = !Messenger.induceLoss;
                break;
            case 'q':
                System.exit(0);
            default:
                System.out.println("Comandos válidos:");
                System.out.println("\t/v\t- Ativa o modo verboso");
                System.out.println("\t/i\t- Induz interrupção prematura na transmissão");
                System.out.println("\t/e\t- Induz erros no fluxo de bits transmitido");
                System.out.println("\t/d\t- Induz atraso no envio do ack");
                System.out.println("\t/l\t- Induz perda de pacote (ignora pacote recebido)");
                System.out.println("\t/1\t- Sair");
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
    public static boolean induceInterruption;
    public static boolean induceDelay;
    public static boolean induceError;
    public static boolean induceLoss;
}
