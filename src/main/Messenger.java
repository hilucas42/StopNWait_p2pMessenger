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
            
            arq.sendMessage(teclado.nextLine(), address, 12346);
        }
    }
    
    public void receiveMessage(String message) {
        
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        
        new Messenger().run();
    }
    
    private final Scanner teclado;
    private final Tx iFaceTx;
    private final ARQ arq;
    private final Rx iFaceRx;
}
