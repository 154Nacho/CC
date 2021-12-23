import jdk.dynalink.linker.GuardedInvocation;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.locks.Lock;

public class ThreadFFSReceive extends Thread{
    int porta;
    private DatagramSocket dsReceive;
    private Stack<Packet> pacotes;

    public ThreadFFSReceive(int porta, Stack<Packet> pacotes) throws SocketException {
        this.porta = porta;
        this.dsReceive = new DatagramSocket(porta);
        this.pacotes = pacotes;
    }


    public void receiveFromGateway() throws IOException {
        byte[] buffer = new byte[128];
        DatagramPacket packet = new DatagramPacket(buffer, 128);
        dsReceive.receive(packet);
        Packet p = new Packet(packet.getData());
        if(p.getType() == 5 && p.getFrag() == 0){
            File diretoria = new File("/home/anabela/" + (new String(p.getData()).replace("\0", "")));
            byte[] ficheiro = Files.readAllBytes(diretoria.toPath());
            Packet novo = new Packet(6, 0, p.getId(), ficheiro, 0);
            pacotes.push(novo);
        }

        else if (p.getType() == 5 && p.getFrag() == 1) {
            File diretoria = new File("/home/anabela/" + (new String(p.getData()).replace("\0", "")));
            byte[] ficheiro = Files.readAllBytes(diretoria.toPath());
            int nPacotes = ficheiro.length/512;
            if((ficheiro.length % 512) != 0){
                nPacotes++;
            }
            byte[] dados;
            int frag = 1;
            if(p.getOffset() == (nPacotes-1)) {
                dados = Arrays.copyOfRange(ficheiro, (p.getOffset() * 512), ficheiro.length);
                frag = 0;

            } else {
                dados = Arrays.copyOfRange(ficheiro, (p.getOffset() * 512), ((p.getOffset() + 1) * 512));
            }

            Packet novo = new Packet(6, p.getOffset(), p.getId(), dados, frag);
            pacotes.push(novo);
            //System.out.println("Guardei pacote com ficheiro fragmentado");
        }
        else if (p.getType() == 2){
            File ficheiro = new File( ("/home/anabela/" + new String(p.getData()).replace("\0", "")));
            if (ficheiro.exists()) {
                byte[] conteudo = Files.readAllBytes(ficheiro.toPath());
                int size = conteudo.length;
                Packet novo = new Packet(3, size, p.getId(), p.getData() , 0);
                pacotes.push(novo);
            } else{
                Packet aux2 = new Packet(4, 0, p.getId(),p.getData() , 0);
                pacotes.push(aux2);
            }
        }


    }

    public void run(){
        try {
            while(true) {
                receiveFromGateway();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
