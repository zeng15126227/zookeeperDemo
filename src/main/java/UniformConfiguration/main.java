package UniformConfiguration;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class main {

    private static volatile ZooKeeper zk;
    private static String address = "172.16.21.111:12181,172.16.21.112:12181,172.16.21.114:12181/20210718";
    private static SessionWatch sessionWatcher = new SessionWatch();
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ZooKeeper getZk() throws IOException, InterruptedException {
        if (zk == null) {
            synchronized (main.class){
                if(zk==null){
                    zk = new ZooKeeper(address, 3000, sessionWatcher);
                    sessionWatcher.setCountDownLatch(countDownLatch);
                    countDownLatch.await();
                }
            }
        }
        return zk;
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {


        try{
            ZooKeeper zk = getZk();
            CallbackResData resData = new CallbackResData();

            CallbackWatch callbackWatch = new CallbackWatch();
            callbackWatch.setZk(zk);
            callbackWatch.setResData(resData);
            callbackWatch.await();


            while(true){
                System.out.println(resData.getData());
                TimeUnit.MILLISECONDS.sleep(2000);
            }

        }finally {
            //close zk
            zk.close();
        }


    }
}
