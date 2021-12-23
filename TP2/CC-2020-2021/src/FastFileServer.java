import java.io.IOException;
import java.net.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;



public class FastFileServer {
    Stack<Packet> pacotes = new Stack<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        FastFileServer fs = new FastFileServer();
        String ip = args[0];
        int porta = Integer.parseInt(args[1]);
        Thread tffss = new Thread(new ThreadFFSSend(ip,porta, fs.pacotes));
        Thread tffsr = new Thread(new ThreadFFSReceive(80, fs.pacotes));
        tffss.start();
        tffsr.start();

    }

}
