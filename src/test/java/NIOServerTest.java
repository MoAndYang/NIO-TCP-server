import message.MsgCreator;
import server.NIOServer;

public class NIOServerTest {
    public static void main(String[] args) {
        new NIOServer(8888).startNIOServer();
        new MsgCreator().createMsginMultithread();
    }
}
