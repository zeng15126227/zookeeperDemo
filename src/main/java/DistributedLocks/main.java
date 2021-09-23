package DistributedLocks;

import UniformConfiguration.SessionWatch;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class main {

    private static volatile ZooKeeper zk;
    private static String address = "172.16.21.111:12181,172.16.21.112:12181,172.16.21.114:12181/20210718/1949";
    private static SessionWatch sessionWatcher = new SessionWatch();
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static ZooKeeper getZk() throws IOException, InterruptedException {
        if (zk == null) {
            synchronized (UniformConfiguration.main.class) {
                if (zk == null) {
                    zk = new ZooKeeper(address, 1000, sessionWatcher);
                    sessionWatcher.setCountDownLatch(countDownLatch);
                    countDownLatch.await();
                }
            }
        }
        return zk;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ZooKeeper zk = getZk();

        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                WatchCallback watchCallback = new WatchCallback();
                watchCallback.setZk(zk);
                watchCallback.setThreadName(Thread.currentThread().getName());
                //加锁
                try {
                    watchCallback.tryLock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //干活
                System.out.println(Thread.currentThread().getName()+" 正在执行。。。");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //解锁
                try {
                    watchCallback.unLock();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        TimeUnit.MILLISECONDS.sleep(20000);
        zk.close();
    }
}
