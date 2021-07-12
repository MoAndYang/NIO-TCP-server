package server;

public class NIOServer {

    private int port = 8888;

    public NIOServer() {
    }

    public NIOServer(int port) {
        this.port = port;
    }

    public void startNIOServer() {
        NIOServerHandler nioServerHandler = setUpNIOServer(port);
        new Thread(nioServerHandler, "NIO-TCP-Server").start();
    }

    private NIOServerHandler setUpNIOServer(int port){
        return new NIOServerHandler(port);
    }
}
