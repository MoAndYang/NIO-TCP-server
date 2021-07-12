package server;

import message.MsgCreator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServerHandler implements Runnable {
    private int port = 8888;
    private Selector selector;
    private ServerSocketChannel servChannel;

    public NIOServerHandler() {
        initNIOServerHandler();
    }

    public NIOServerHandler(int port) {
        this.port = port;
        initNIOServerHandler();
    }


    private void initNIOServerHandler() {
        try {
            //1、打开ServerSocketChannel，用于监听客户端连接，是所有客户端连接的父通道
            servChannel = ServerSocketChannel.open();
            //2、绑定监听端口号，设置连接为非阻塞IO
            servChannel.configureBlocking(false);
            //这里的1024是请求传入连接队列的最大长度
            servChannel.socket().bind(new InetSocketAddress(port), 1024);
            //3、创建选择器
            selector = Selector.open();
            //4、将管道注册到Selector上，监听accept事件
            servChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("The NIO TCP server is start. The server IP is : " + servChannel.getLocalAddress() + ". The bound port is : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Override
    public void run() {
        while(true){
            try {
                //其中的数字为休眠时间，Selector每隔1ms都被唤醒一次
                selector.select(1);
                //5、Selector轮询注册在它上面的所有SocketChannel，并返回所有有服务器感兴趣事件发生的SocketChannel
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                //6、服务器迭代处理所有需要处理的SocketChannel
                while(it.hasNext()){
                    key = it.next();
                    try{
                        handKey(key);
                        //移除出未处理的队列
                        it.remove();
                    }catch(Exception e){
                        if(key != null){
                            key.cancel();
                            if(key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private  void handKey(SelectionKey key) throws IOException {
        //判断key值是否有效
        if(key.isValid()){
            //6、Selector监听到有新的客户端接入，处理新接入的连接请求
            if(key.isAcceptable()){
                /*
                 * 通过ServerSocketChannel的accept接收客户端的连接请求并创建SocketChannel实例
                 * 完成上述操作之后相当于完成了TCP的三次握手，TCP物理链路正式建立
                 * 我们将SocketChannel设置为异步非阻塞
                 * 同时也可以对其TCP参数进行设置，例如TCP发送和接收缓存区的大小等
                 */
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                //设置客户端链路为非阻塞模式
                sc.configureBlocking(false);

                String welcome = "Welcome, here is NIO TCP Serve! \n";
                doWrite(sc,welcome);

                //8、将新接入的SocketChannel注册到Selector上，监听读操作
                //sc.register(selector,SelectionKey.OP_READ);

                //8.1、将新接入的SocketChannel注册到Selector上，监听写操作
                sc.register(selector,SelectionKey.OP_WRITE);

            }
            //9、监听到注册的SocketChannel有可读事件发生，进行处理
//            if(key.isReadable()){
//                /*
//                 * 读取客户端的请求消息
//                 * 我们无法得知客户端发送的码流大小，作为例程，我们开辟一个1k的缓冲区
//                 * 然后调用read方法读取请求码流
//                 */
//                //读取数据
//                SocketChannel sc = (SocketChannel) key.channel();
//                //10、分配一个新的缓存空间，大小为1024，异步读取客户端的消息
//                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
//                int readBytes = sc.read(readBuffer);
//                /*
//                 * read()方法的三种返回值
//                 * 返回值大于0：读到了直接，对字节进行编解码
//                 * 返回值等于0：没有读到字节，属于正常场景，忽略
//                 * 返回值为-1：链路已经关闭，需要关闭SocketChannel释放资源
//                 */
//                if(readBytes > 0){
//                    readBuffer.flip();
//                    //开辟一个空间，大小为缓存区中还剩余的字节数
//                    byte[] bytes = new byte[readBuffer.remaining()];
//                    readBuffer.get(bytes);
//                    String body = new String(bytes,"UTF-8");
//                    System.out.println("The NIO TCP server receive order : " + body);
//
//                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
//                    doWrite(sc,currentTime);
//
//                }else if(readBytes < 0){
//                    //对端链路关闭
//                    key.cancel();
//                    sc.close();
//                }
//                //读到0字节忽略
//            }

            //9.1、监听到注册的SocketChannel有可写事件发生，进行处理
            if(key.isWritable()){
                SocketChannel sc = (SocketChannel) key.channel();
                String msg = null;
                if(!MsgCreator.msgQueue.isEmpty()){
                    msg = MsgCreator.msgQueue.poll();
                    doWrite(sc,msg);
                    System.out.println(msg);
                }else{
                     //msg = "Warning！ There is no message now！";
                     //doWrite(sc,msg);
                }
            }

        }
    }

    private  void doWrite(SocketChannel sc, String response) throws IOException {
        //如果接受到消息不为空，并且不是空白行
        //strim方法可用于从字符串的开始和结束处修剪空白(如上所定义)。
        if(response != null && response.trim().length() > 0){
            byte[] bytes = response.getBytes();
            //9、将消息异步发送给客户端
            //分配写空间缓存区
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            //把bytes放入到写缓存中
            writeBuffer.put(bytes);
            /*
             * 翻转这个缓冲区，将limit设为当前位置，
             * 当使用完put()方法时，position位于数据的末尾，我们需要把它移动到0
             * 这样调用get操作时我们才能把缓存区中的字节数组复制到新创建的直接数组中
             */
            writeBuffer.flip();
            //调用write方法将缓存区中的字节数组发送出去
            //需要处理写半包的场景
            sc.write(writeBuffer);
        }
    }

}
