import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final ZooKeeper zk = new ZooKeeper("172.16.21.111:12181,172.16.21.112:12181,172.16.21.114:12181",
                3000,
                //新建zk时的watch时session级别
                new Watcher() {
                    public void process(WatchedEvent watchedEvent) {
                        Event.KeeperState state = watchedEvent.getState();
                        Event.EventType type = watchedEvent.getType();
                        System.out.println(watchedEvent.getPath());
                        System.out.println(watchedEvent.toString());

                        switch (state) {
                            case Unknown:
                                break;
                            case Disconnected:
                                break;
                            case NoSyncConnected:
                                break;
                            case SyncConnected:
                                countDownLatch.countDown();
                                break;
                            case AuthFailed:
                                break;
                            case ConnectedReadOnly:
                                break;
                            case SaslAuthenticated:
                                break;
                            case Expired:
                                break;
                        }

                        switch (type) {
                            case None:
                                break;
                            case NodeCreated:
                                break;
                            case NodeDeleted:
                                break;
                            case NodeDataChanged:
                                break;
                            case NodeChildrenChanged:
                                break;
                        }
                    }
                });

        countDownLatch.await();
        ZooKeeper.States states = zk.getState();
        switch (states) {
            case CONNECTING:
                System.out.println("ing...");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        String pathName = zk.create("/rockets","cc".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        //第二种watch绑定在get，exsit操作
        final Stat stat = new Stat();
        byte[] res = zk.getData("/rockets", new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent.toString());
                try {
                    //此处watch是new zk时的watch
                    //zk.getData("/rockets",true,stat);
                    zk.getData("/rockets",this,stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },stat);
        System.out.println(new String(res));
        Stat stat1 = zk.setData("/rockets","green".getBytes(),0);

        Stat stat2 = zk.setData("/rockets","mobri".getBytes(),stat1.getVersion());

        System.out.println("before get");
        zk.getData("/rockets", false, new AsyncCallback.DataCallback() {
            public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
                System.out.println("do callback");
                System.out.println(new String(bytes));
            }
        },"");
        System.out.println("after get");

        Thread.sleep(20000);
    }
}
