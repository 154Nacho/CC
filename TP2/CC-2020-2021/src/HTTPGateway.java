import java.io.*;
import java.net.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HTTPGateway {
   ServerSocket ss;
   DatagramSocket dsReceive = new DatagramSocket(80);
   DatagramSocket dsSend = new DatagramSocket();
   Socket s = null;
   Map<Integer, DadosConexao> fastFilesServers = new HashMap<>();
   Map<Integer, Socket> clientes = new HashMap<>();
   int index = 0;

   public HTTPGateway() throws SocketException {}

   public void executeTCP(){
      try {
         ss = new ServerSocket(80);

         while((s = ss.accept()) != null){
            System.out.println("Cliente conetado");
            BufferedReader readSocket = new BufferedReader(new InputStreamReader(s.getInputStream()));


            String path = readSocket.readLine().split(" ")[1];
            path = path.split("/")[1];
            Map<Integer, ArrayList<DadosConexao>> fastfilesPorPedidos = new HashMap<>();
            Thread gws = new Thread(new ThreadGatewaySend(dsSend,fastFilesServers, fastfilesPorPedidos, path,index, 1, 0));
            index++;
            gws.start();

         }

         ss.close();

      } catch (IOException e){
         System.out.println(e.getMessage());
      }
}

   public void executeUDP() throws SocketException {

      Thread gwr = new Thread(new ThreadGatewayReceiver(dsSend,dsReceive,fastFilesServers, clientes));

      gwr.start();

   }


   public static void main(String[] args) throws IOException {
      HTTPGateway gw = new HTTPGateway();
      gw.executeUDP();
      gw.executeTCP();
   }
}

