package ctp;

import interfaces.Tx;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.*;

/**
 * A classe que implementa o método Stop and Wait ARQ
 * @author Lucas
 */
public class ARQ implements Runnable {
    
    public ARQ(Tx ifaceTx, Messenger app) {
        this.app = app;
        this.ifaceTx = ifaceTx;
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
        lastReceivedSequence = 1;
        lastQueuedSequence = 1;
        lastSentSequence = 1;
        ackReceived = true;
        thread = null;
    }
    
    @Override
    public void run() {
        while(true) {
            if(!outputQueue.isEmpty()) {
                try {
                    transmitFrame();
                } catch (IOException ex) {
                    Logger.getLogger(ARQ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ARQ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(!inputQueue.isEmpty()) {
                giveUpMessage();
            }
        }
    }
    
    /**
     * Método que executa o stop and wait ARQ
     */
    private void transmitFrame() throws IOException {
        
        Frame frame = outputQueue.remove(0);
        lastSentSequence = frame.getSequence();
        ackReceived = false;
        
        while(!ackReceived) {
            if(Messenger.verbose)
                System.out.println(" - Thread ARQ: Executando envio Stop and Wait");
            ifaceTx.put(frame.getStream(), frame.getDestAddress(), frame.getDestPort());
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                //Logger.getLogger(ARQ.class.getName()).log(Level.SEVERE, null, ex);
                if(Messenger.verbose)
                    System.out.println(" - Thread ARQ: Timer interrompido");
            }
        }
    }
    
    /**
     * Entrega uma mensagem da fila de entrada para a "camada superior"
     */
    private void giveUpMessage() {
        
        Frame receivedFrame = inputQueue.remove(0);
        if(Messenger.verbose)
            System.out.println(" - Thread ARQ: Devolvendo mensagem à aplicação");
        app.receiveMessage(receivedFrame.toString());
    }
    
    /**
     * Enfilera uma nova mensagem para ser enviada
     * @param message a mensagem a ser enviada
     * @param destAddress o endereço de destino da mensagem
     * @param destPort a porta de destino da mensagem
     */
    public void sendMessage(String message, String destAddress, int destPort) {
        
        lastQueuedSequence = 1-lastQueuedSequence;
        if(Messenger.verbose)
            System.out.println(" - Thread ARQ: Enfileirando mensagem para envio. Seq = "+lastQueuedSequence);
        Frame frame = new Frame(lastQueuedSequence, message, destAddress, destPort);
        outputQueue.add(frame);
    }
    
    /**
     * O método por onde o ARQ recebe os dados da "camada inferior". Aqui ocorre
     * o envio de ACKs dos frames recebidos intactos e a interrupção do stop and
     * wait quando recebido o ACK esperado.
     * @param stream os dados recebidos que constituem o frame
     * @param address o endereço de remetente do frame recebido
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public void receiveFrame(char[] stream, String address) throws IOException, InterruptedException {
        
        if(Messenger.induceLoss)
            return;
        if(Messenger.induceError) 
            stream[2] = stream [5] = stream[6] = stream[7] = '1';
        if(Messenger.induceInterruption) {
            char[] interruptedStream = new char[stream.length-3];
            System.arraycopy(stream, 0, interruptedStream, 0, interruptedStream.length);
            stream = interruptedStream;
        }
        if(Messenger.verbose)
            System.out.println(" - Thread ARQ: Recebendo frame da rede");
        
        Frame receivedFrame = new Frame(stream);
        
        if(receivedFrame.crcVerify()) {
            if(receivedFrame.isACK()) {
                if(Messenger.verbose)
                    System.out.println(" - Thread ARQ: ACK recebido");
                if(receivedFrame.getSequence() != lastSentSequence && !ackReceived) {
                    if(Messenger.verbose)
                        System.out.println(" - Thread ARQ: ACK ok");
                    ackReceived = true;
                    this.thread.interrupt();
                }
            }
            else {
                if(Messenger.verbose)
                    System.out.println(" - Thread ARQ: Mensagem recebida");
                if(receivedFrame.getSequence() != lastReceivedSequence) {
                    inputQueue.add(receivedFrame);
                    lastReceivedSequence = receivedFrame.getSequence();
                }
                if(Messenger.verbose)
                    System.out.println(" - Thread ARQ: Enviando AKC. Seq = "+(1-lastReceivedSequence));
                
                if(Messenger.induceDelay)
                    Thread.sleep(6000);
                
                ifaceTx.put(new Frame(1-lastReceivedSequence, "", null, 0).getStream(), 
                        address, 12345);
            }
        }
    }
    
    /**
     * Recebe a própria referência de thread
     * @param thisThread a referência
     */
    public void setThreadReference(Thread thisThread) {
        
        this.thread = thisThread;
    }
    
    private Thread thread;
    
    private int lastReceivedSequence;
    private int lastQueuedSequence;
    private int lastSentSequence;
    private boolean ackReceived;
    private final Tx ifaceTx;
    private final Messenger app;
    private final List<Frame> inputQueue;
    private final List<Frame> outputQueue;
    
    private static final int timeout = 5000;
}
