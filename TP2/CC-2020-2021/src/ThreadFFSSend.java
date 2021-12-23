import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;

public class ThreadFFSSend extends Thread{
    private InetAddress ip;
    private int porta;
    private DatagramSocket dsSend = new DatagramSocket();
    private Stack<Packet> pacotes;

    public ThreadFFSSend(String ip, int porta, Stack<Packet> pacotes) throws SocketException {
        try {
            this.ip = InetAddress.getByName(ip);
            this.porta = porta;
            this.pacotes = pacotes;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendToGateway(DatagramSocket ds) {
        try {
            Packet packet = new Packet(1, 0, 21, Integer.toString(this.porta).getBytes(), 0);
            byte[] data = packet.toBytes();
            DatagramPacket dpacket = new DatagramPacket(data, data.length, this.ip, this.porta);
            ds.send(dpacket);
            System.out.println("Enviou o pedido a pedir conexão");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        while (true) {
            while (!pacotes.isEmpty()) {
                try {
                    Packet packet1 = pacotes.pop();
                    byte[] sending = packet1.toBytes();
                    DatagramPacket send = new DatagramPacket(sending, sending.length, this.ip, this.porta);
                    ds.send(send);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    public void run(){
        sendToGateway(dsSend);
    }


}
