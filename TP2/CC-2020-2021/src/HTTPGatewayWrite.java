import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HTTPGatewayWrite implements Runnable{
    private Socket socket;
    private Packet pacote;
    Map<Integer, Map<Integer, Packet>> ficheiros;
    int nPacotes = 0;

    public HTTPGatewayWrite(Packet pacote, Socket socket, Map<Integer, Map<Integer, Packet>> ficheiros, int nPacotes ){
        this.pacote = pacote;
        this.socket = socket;
        this.ficheiros = ficheiros;
        this.nPacotes = nPacotes;
    }

    public void run(){
        try{
            BufferedWriter writeSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PrintStream out = new PrintStream(socket.getOutputStream());
            if (nPacotes == -1){
                out.println("HTTP/1.1 404 File Not Found");
                out.println("Server: Java HTTP Server from HTTPGW : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + new String(pacote.getData()).replace("\0", ""));
                out.println("Content-length: " + 0);
                out.println();
                out.flush();
            }

            else if (nPacotes == 1)
                writeSocket.write(new String (ficheiros.get(pacote.getId()).get(0).getData()).replace("\0", ""));
            else {
                for (int i = 0; i<nPacotes; i++){
                    writeSocket.write(new String(ficheiros.get(pacote.getId()).get(i).getData()).replace("\0", ""));
                }
            }
            writeSocket.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
