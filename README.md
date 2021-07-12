# NIO-tcp-server
一个基于NIO网络I/O模型的TCP服务器。

---

2021/7/12更新说明：
  
  本项目是在实际中一个大型项目的网络传输部分的Java实现，基于Java NIO包的同步非阻塞网络I/O模型，其业务场景可以简单归类为生产者-消费者模型：一个基于线程池的多线程消息生产类`MsgCreator`不断生产消息，同时将生产的消息加入线程安全队列`ConcurrentLinkedQueue`中，`NIOServer`作为消费者在接受到客户端连接时，从队列中取出消息然后发送给客户端。
  
  使用JMeter对该NIO TCP服务器进行压测，在本人计算机上测试1分钟内8000次并发连接(设置线程的thinktime为1秒)，可以做到无异常。
  ![](https://github.com/MoAndYang/NIO-tcp-server/blob/main/Jmeter_test/JMeter-test-results.png)

