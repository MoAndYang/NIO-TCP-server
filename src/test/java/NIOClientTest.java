import client.NIOClient;

public class NIOClientTest {
    public static void main(String[] args) {
        new NIOClient("127.0.0.1", 8888).startNIOClient();
    }
}
