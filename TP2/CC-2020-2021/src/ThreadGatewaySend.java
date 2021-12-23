import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadGatewaySend extends Thread{
    DatagramSocket dsSend;
    String path;
    Map<Integer, DadosConexao> fastFilesServers;
    Map<Integer, ArrayList<DadosConexao>> fastFilesPorPedido;
    int index;
    int pedido;
    int size;

    public ThreadGatewaySend(DatagramSocket dsSend, Map<Integer, DadosConexao> fastFilesServers,Map<Integer, ArrayList<DadosConexao>> fastFilesPorPedido, String path, int index, int pedido, int size) throws SocketException {
        this.dsSend = dsSend;
        this.fastFilesServers = fastFilesServers;
        this.path = path;
        this.index = index;
        this.pedido = pedido;
        this.fastFilesPorPedido = fastFilesPorPedido;
        this.size = size;
    }

    public void sendToFastFileServer() {
        if (pedido == 1) {
            try {
                    for (DadosConexao d : fastFilesServers.values()) {
                        byte[] data = new Packet(2, 0, this.index, this.path.getBytes(), 0).toBytes();
                        DatagramPacket packet = new DatagramPacket(data, data.length, d.getIp(), d.getPorta());
                        dsSend.send(packet);
                    }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if (this.pedido == 0){
                ArrayList<DadosConexao> aux = fastFilesPorPedido.get(index);
                int nPacotes = size / 512;
                if ((size % 512) != 0) {
                    nPacotes++;
                }
                int frag = 0;
                int sizeServers = aux.size();
                int z = 0;
                for (int j = 0; j < nPacotes; j++) {
                    if (z == sizeServers) z = 0;
                    try {
                        if (nPacotes > 1) frag = 1;
                        Packet novo = new Packet(5, j, index, this.path.getBytes(StandardCharsets.UTF_8), frag);
                        byte[] data = novo.toBytes();
                        DatagramPacket send = new DatagramPacket(data, data.length, aux.get(z).getIp(), aux.get(z).getPorta());
                        dsSend.send(send);
                        z++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public void run(){
                sendToFastFileServer();
    }


}
