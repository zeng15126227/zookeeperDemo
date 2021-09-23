package UniformConfiguration;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class main {

    private static volatile ZooKeeper zk;
    private static String address = "172.16.21.111:12181,172.16.21.112:12181,172.16.21.114:12181/20210718";
    private static SessionWatch sessionWatcher = new SessionWatch();
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    //单例创建zk实例
    public static ZooKeeper getZk() throws IOException, InterruptedException {
        if (zk == null) {
            synchronized (main.class){
                if(zk==null){
                    //地址，超时时间，sessionWatcher
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
            ConfData conf = new ConfData();

            CallbackUtils callbackUtils = new CallbackUtils();
            callbackUtils.setZk(zk);
            callbackUtils.setConf(conf);
            callbackUtils.registerWatcher();


            while(true){
                System.out.println(conf.getData());
                TimeUnit.MILLISECONDS.sleep(2000);
            }

        }finally {
            //close zk
            zk.close();
        }


    }
}
