import java.net.InetAddress;
import java.util.List;

public class DadosConexao {
    private InetAddress ip;
    private int porta;

    public DadosConexao(InetAddress ip, int porta){
        this.ip = ip;
        this.porta = porta;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPorta() {
        return porta;
    }
}
