package ctp;

/**
 *
 * @author Lucas
 */
public class Frame {
    
    public Frame(int sequence, String data, String address, int port) {
        
        this.address = address;
        this.port = port;
        
        char[] binaryFrameData = new char[1+charSize*data.length()];
        binaryFrameData[0] = (char)(sequence+48);
        System.arraycopy(stringToBinary(data), 0, binaryFrameData, 1, binaryFrameData.length-1);
        
        char[] bitsCRC = crcGen(binaryFrameData);
        
        stream = new char[bitsCRC.length+binaryFrameData.length];
        System.arraycopy(binaryFrameData,0,stream,0,binaryFrameData.length);
        System.arraycopy(bitsCRC, 0, stream, binaryFrameData.length, bitsCRC.length);
    }
    
    public Frame(char[] stream) {
        this.address = null;
        this.port = 0;
        this.stream = stream;
    }
    
    /**
     * Faz a verificação CRC do frame extraindo os últimos bits do fluxo de bits
     * @return true se não foi detectado erro ou false se os dados estã corrompidos
     */
    public boolean crcVerify() {
        
        char[] crcCheck = CRC.crcDiv(stream);
        for(char bit:crcCheck)
            if(bit == '1')
                return false;
        return true;
    }
    
    public boolean isACK() {
        
        return (stream.length-divisor.length <= 0);
    }
    
    public String getDestAddress() {
        
        return this.address;
    }
    
    public int getDestPort() {
        
        return this.port;
    }
    
    public char[] getStream() {
        
        return stream;
    }
    
    public int getSequence() {
        
        return stream[0]-48;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String toString() {
        
        char[] data = new char[stream.length-divisor.length]; //[stream.l-(divisor.l-1)-1]
        System.arraycopy(stream, 1, data, 0, data.length);
        return binaryToString(data);
    }
    
    /**
     * Gera os bits de verificação CRC
     * @param data o array de dados
     * @return um array de char com os bits de verificação CRC
     */
    private char[] crcGen(char[] data) {
        
        char[] dividendo = new char[data.length+divisor.length-1];
        
        System.arraycopy(data, 0, dividendo, 0, data.length);
        for(int i = data.length; i < dividendo.length; i++)
            dividendo[i] = '0';
        
        return CRC.crcDiv(dividendo);
    }
    
    /**
     * Converte uma string ascii em uma string binária com relação charSize:1
     * @param entrada a string ascii a ser convertida
     * @return um array de caracteres ascii com linguagem binária {0,1}
     */
    private char[] stringToBinary(String entrada) {
        
        char[] saida = new char[charSize*entrada.length()];
        String ascii;
        for(int i = 0; i < entrada.length(); i++) {
            ascii = Integer.toBinaryString((int)entrada.charAt(i));
            
            for(;ascii.length() < charSize;)
                ascii = "0"+ascii;
                
            for(int j = 0; j < charSize; j++)
                saida[j+i*charSize] = ascii.charAt(j);
        }
        return saida;
    }
    
    /**
     * Recupera uma string ascii de uma string binária com relação charSize:1
     * @param entrada a string binária, com linguagem {0,1}
     * @return a string ascii
     */
    private String binaryToString(char[] entrada) {
        
        String entradaConv = "";
        String saida = "";
        
        for(int i = 0; i < entrada.length; i++)
            entradaConv = entradaConv + entrada[i];
        
        for(int i = 0; i < entrada.length; i += charSize)
            saida = saida + (char)Integer.parseInt(entradaConv.substring(i, i+charSize), 2);
        
        return saida;
    }
    
    private final char[] stream;
    private final int port;
    private final String address;
    private static final int charSize = 8;
    private static final char[] divisor = {'1','0','0','1','1','0','0','0','1'};
    
    /**
     * Os métodos usados pelos cálculos de CRC
     */
    private static class CRC {
        
        /**
         * O método divisor de polinômios do CRC
         * @param data o dividendo
         * @return o campo CRC ou os bits de verificação
         */
        static char[] crcDiv(char[] num) {

            int ref = 0;
            char[] crc = new char[divisor.length-1];
            char[] pol = new char[divisor.length];
            
            for(int i = 0; i < pol.length; i++)
                pol[i] = '0';
            
            while(true) {
                while(pol[0] == '0') {
                    if(ref == num.length) {
                        System.arraycopy(pol, 1, crc, 0, crc.length);
                        return crc;
                    }
                    pol = leftShift(pol);
                    pol[pol.length-1] = num[ref++];
                }
                pol = xor(pol, divisor);
            }
        }
        
        /**
         * Operação de XOR entre os campos de um array binário
         * @param array1 um array de caracteres com linguagem {0,1}
         * @param array2 outro array de caracteres com linguagem {0,1}
         * @return o resultado da operação, com o mesmo tamanho de array1
         */
        private static char[] xor(char[] array1, char[] array2) {
        
            for(int i = 0; i < array1.length; i++) {
                if(array1[i] == array2[i])
                    array1[i] = '0';
                else
                    array1[i] = '1';
            }
            return array1;
        }
        
        /**
         * Operação de deslocamento à esquerda em array binário de char
         * @param array o array a ser deslocado
         * @return o array com um deslocamento à esquerda e preenchido com um 0 à direita
         */
        private static char[] leftShift(char[] array) {

            for(int i = 1; i < array.length; i++)
                array[i-1] = array[i];
            array[array.length-1] = '0';
            return array;
        }
    }
}