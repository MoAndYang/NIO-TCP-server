package message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class MsgCreator {
    public static  ConcurrentLinkedQueue<String> msgQueue = new ConcurrentLinkedQueue<>();
    private Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public void createMsginMultithread(){

        /*
         * 创建线程池
         * Runtime.getRuntime().availableProcessors():JVM运行所能创建的最大线程数
         * 空闲线程存活时间为120s
         */
        ExecutorService executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 100, 120L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000));


        for (int i = 0; i < 5; i++){
            executor.execute(new msgThread());
        }

    }


    private class msgThread implements Runnable{

        @Override
        public void run() {
            while(true){
                while (!msgQueue.offer("Message created by Thread " + Thread.currentThread().getId() + " at " + formatter.format(date)));

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
