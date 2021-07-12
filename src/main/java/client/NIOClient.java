package client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {

    private SocketChannel socketChannel;
    private Selector selector;
    private String ip = "127.0.0.1";
    private int port = 8888;

    public NIOClient() {
    }

    public NIOClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private void initNIOClient() throws Exception {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(ip, port));
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public void startNIOClient() {
        try {
            initNIOClient();

//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            buffer.put("Hello, NIO TCP server!".getBytes());
//            buffer.flip();
//            while (buffer.hasRemaining()) {
//                socketChannel.write(buffer);
//            }
//
//            buffer.clear();
//            socketChannel.socket().shutdownOutput();

            while(true){
                selector.select(1);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                while(it.hasNext()){
                    key = it.next();
                    it.remove();
                    try{
                        handleKey(key);
                    }catch(Exception e){
                        if(key != null){
                            key.cancel();
                            if(key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private void handleKey(SelectionKey key) throws IOException {
        if(key.isReadable()){
            String response = receiveData(socketChannel);
            System.out.println(response);
        }
    }


    private String receiveData(SocketChannel sc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String response = "";
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] bytes;
            int count = 0;
            if ((count = sc.read(buffer)) >= 0) {
                buffer.flip();
                bytes = new byte[count];
                buffer.get(bytes);
                response  = new String(bytes,"UTF-8");
                //baos.write(bytes);
                buffer.clear();
            }
            //bytes = baos.toByteArray();
            //response = new String(bytes);
        } finally {
            try {
                baos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return response;
    }
}
