package interfaces;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lucas
 */
public class Tx implements Runnable {
    
    public Tx() {
        client = null;
        output = null;
    }
    
    public void put(char[] stream, String ipAddress, int port) throws IOException {
        
        if(client == null)
            client = new Socket(ipAddress,port);
        if(output == null)
            output = new PrintStream(client.getOutputStream());
        
        String frame = binaryToString(stream);
        System.out.println(" - Thread Tx: Enviando "+frame.substring(0, 5)+"...");
        
        output.println(frame);
        
        //output.close();
        //client.close();
    }

    @Override
    public void run() {
        
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Tx.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private String binaryToString(char[] entrada) {
        
        String saida = "";
        
        for(int i = 0; i < entrada.length; i ++)
            saida = saida + entrada[i];
        
        return saida;
    }
    
    private PrintStream output;
    private Socket client;
}
