import redis.clients.jedis.Jedis;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class RedisTest {
    private static Integer inventory = 11;
    private static final int NUM = 10;
    private static LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
    static ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingQueue);
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);

        long start = System.currentTimeMillis();
        for (int i = 0; i <= NUM; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Jedis jedis = new Jedis("127.0.0.1", 6379);
                    RLock lock = new RLock(jedis, "lock_key");
                    lock.lock();
                    inventory--;
                    lock.unlock();
                    System.out.println("线程执行:" + Thread.currentThread().getName());
                    countDownLatch.countDown();
                }
            });
        }
        threadPoolExecutor.shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("执行线程数:"+ NUM+" 总耗时" + (end - start) + " 库存数为："+inventory);


    }

}