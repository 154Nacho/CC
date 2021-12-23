import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Classe que implementa o nosso protocolo
 */
public class Packet implements Serializable {
    private int type ;
    private int offset ;
    private int id ;
    private byte [] data;
    private int frag ;


    /**
     * construtor parameterizado para um packet/pacote
     * @param type  tipo de pacote
     * @param offset    Número da partição
     * @param id    Identificador do pacote
     * @param data  byte a transportar
     * @param frag      Flag que indica se o pacote foi fragmentado ou não
     */
     //* @param file      Ficheiro que pretendemos transferir
    public Packet(int type, int offset, int id, byte[] data, int frag) {
        this.type = type;
        this.offset = offset;
        this.id = id;
        this.data = data;
        this.frag = frag;
    }

    public Packet(byte[] array){
        this.id = ByteBuffer.wrap(array, 0, 8).getInt();
        this.type = ByteBuffer.wrap(array, 8, 8).getInt();
        this.offset = ByteBuffer.wrap(array, 16, 8).getInt();
        this.frag = ByteBuffer.wrap(array, 24, 8).getInt();


        byte[] data = new byte[array.length - 32];

        System.arraycopy(array,32,data,0,array.length-32);

        this.data = data;

    }

    byte[] toBytes() throws IOException {
        byte[] tipo = converteBytes(this.type);
        byte[] offset = converteBytes(this.offset);
        byte[] id = converteBytes(this.id);
        byte[] frag = converteBytes(this.frag);


        byte [] buffer = new byte[8*4 + this.data.length];

        System.arraycopy(id,0,buffer,0,8);
        System.arraycopy(tipo,0,buffer,8,8);
        System.arraycopy(offset,0,buffer,16,8);
        System.arraycopy(frag,0,buffer,24,8);
        System.arraycopy(data,0,buffer,32,data.length);

        return buffer;

    }

    byte[] converteBytes(int info){
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(info);
        return bb.array();
    }


    public byte[] getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    public int getOffset() {
        return offset;
    }

    public int getType() {
        return type;
    }

    public  int getFrag(){
        return frag;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", offset=" + offset +
                ", id=" + id +
                ", data=" + Arrays.toString(data) +
                ", frag=" + frag +
                '}';
    }
}
