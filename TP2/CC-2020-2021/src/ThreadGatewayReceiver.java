import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadGatewayReceiver extends Thread {
    DatagramSocket dsReceive;
    DatagramSocket send;
    Map<Integer, DadosConexao> fastFilesServers;
    int index;
    Map<Integer, Socket> clientes;
    Map<Integer, ArrayList<DadosConexao>> fastfilesPorPedido = new HashMap<>();
    Map<Integer, Map<Integer, Packet>> ficheiros;
    int i;
    int size;

    public ThreadGatewayReceiver(DatagramSocket send, DatagramSocket dsReceive, Map<Integer, DadosConexao> fastFilesServers, Map<Integer, Socket> clientes) throws SocketException {
        this.index = 0;
        this.dsReceive = dsReceive;
        this.fastFilesServers = fastFilesServers;
        this.send = send;
        this.clientes = clientes;
        this.i = 0;
        this.size = 0;
        this.ficheiros = new HashMap<>();
    }



    public void receiveFromFastFileServer() throws IOException {
        byte[] buffer = new byte[544];
        DatagramPacket dpacket = new DatagramPacket(buffer, 544);
        dsReceive.receive(dpacket);
        Packet packet = new Packet(buffer);
        if (packet.getType() == 1) {
            String data = new String(packet.getData());
            data = data.replace("\0", "");
            int porta = Integer.parseInt(data);
            DadosConexao dc = new DadosConexao(dpacket.getAddress(), porta);
        } else if (packet.getType() == 3) {
                DadosConexao dc = new DadosConexao(dpacket.getAddress(), 80);
                int id = packet.getId();
                if (!fastfilesPorPedido.containsKey(id)) {
                    ArrayList<DadosConexao> aux = new ArrayList<>();
                    aux.add(dc);
                    fastfilesPorPedido.put(id, aux);
                } else {
                    ArrayList<DadosConexao> dcs = fastfilesPorPedido.get(id);
                    dcs.add(dc);
                    fastfilesPorPedido.put(id, dcs);
                }
                this.size = packet.getOffset();
                i++;
                if (i == index) {
                    Thread nova = new Thread(new ThreadGatewaySend(send, fastFilesServers, fastfilesPorPedido, new String(packet.getData()).replace("\0", ""), id, 0, size));
                    this.i = 0;
                    nova.start();
                }

        } else if (packet.getType() == 4) {
                i++;
                if (i == index) {
                    if (!fastfilesPorPedido.containsKey(packet.getId())) {
                        int id = packet.getId();
                        Socket socket = clientes.get(id);
                        Map<Integer, Packet> novo = new HashMap<>();
                        novo.put(-1, packet);
                        ficheiros.put(id, novo);
                        Thread write = new Thread(new HTTPGatewayWrite(packet, socket, ficheiros, -1));
                        write.start();
                    } else {
                        Thread nova = new Thread(new ThreadGatewaySend(send, fastFilesServers, fastfilesPorPedido, new String(packet.getData()).replace("\0", ""), packet.getId(), 0, size));
                        this.i = 0;
                        nova.start();
                    }

                }

        } else if (packet.getType() == 6) {
                int id = packet.getId();
                Socket socket = clientes.get(id);
                if (ficheiros.containsKey(id)) {
                    Map<Integer, Packet> aux = ficheiros.get(id);
                    aux.put(packet.getOffset(), packet);
                } else {
                    Map<Integer, Packet> novo = new HashMap<>();
                    novo.put(packet.getOffset(), packet);
                    ficheiros.put(id, novo);
                }
                int aux = -1;
                boolean flag = false;
                int nPacotes = 1;
                for (Map.Entry<Integer, Packet> entry : this.ficheiros.get(id).entrySet()) {
                    if (aux + 1 == entry.getKey()) {
                        aux++;
                        if (entry.getValue().getFrag() == 0) {
                            flag = true;
                            nPacotes = aux + 1;
                            break;
                        }
                    } else break;

                }
                if (flag) {
                    Thread write = new Thread(new HTTPGatewayWrite(packet, socket, ficheiros, nPacotes));
                    write.start();
                }
        }

    }

    public void run(){
        try {
            while(true) {
                receiveFromFastFileServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
