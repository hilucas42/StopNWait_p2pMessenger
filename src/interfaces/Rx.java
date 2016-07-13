package interfaces;

import ctp.ARQ;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Messenger;

/**
 *
 * @author Lucas
 */
public class Rx implements Runnable {
    
    public Rx(int listenPort, ARQ arq) {
        this.listenPort = listenPort;
        this.arq = arq;
    }
    
    @Override
    public void run() {
        
        ServerSocket server = null;
        Socket client = null;
        Scanner s = null;
        
        try {
            server = new ServerSocket(listenPort);
            client = server.accept();
            s = new Scanner(client.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(Rx.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if(Messenger.verbose)
            System.out.println(" - Thread Rx: Interlocutor conectado");
        while(s.hasNextLine()) {
            try {
                if(Messenger.verbose)
                    System.out.println(" - Thread Rx: Mensagem recebida");
                arq.receiveFrame(stringToBinary(s.nextLine()),
                        client.getInetAddress().getHostAddress());
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Rx.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    private char[] stringToBinary(String entrada) {
        
        char[] saida = new char[entrada.length()];
        
        for(int i = 0; i < entrada.length(); i++) {
            saida[i] = entrada.charAt(i);
        }
        return saida;
    }
    
    private final int listenPort;
    private final ARQ arq;
}
