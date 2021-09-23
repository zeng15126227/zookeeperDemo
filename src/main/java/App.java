import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

//watch注册只发生在读事件：get、exist
public class App {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final ZooKeeper zk = new ZooKeeper("172.16.21.111:12181,172.16.21.112:12181,172.16.21.114:12181/20210718",
                3000,
                //第一种watch：新建zk时的watch是session级别
                new Watcher() {
                    public void process(WatchedEvent watchedEvent) {
                        Event.KeeperState state = watchedEvent.getState();
                        Event.EventType type = watchedEvent.getType();
                        System.out.println("*********session watcher********");
                        System.out.println(watchedEvent.toString());

                        //zk与客户端连接状态
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
                        //节点事件类型
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
        //zk与客户端连接状态，与watcher中状态一致
        switch (states) {
            case CONNECTING:
                System.out.println("ing...");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                //System.out.println("ed");
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
        //创建节点
        String pathName = zk.create("/rockets", "green".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);


        final Stat stat = new Stat();
        //同步获取数据
        //第二种watch绑定在get，exsit操作
        byte[] res = zk.getData("/1246", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("*********node watcher********");
                System.out.println(watchedEvent.toString());
                try {
                    //重复注册，此处watch是new zk时的watch
                    //zk.getData("/rockets",true,stat);
                    //重复注册，此处传一个watch对象
                    zk.getData("/rockets", this, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);
        //System.out.println(new String(res));
        //第一次修改node
        Stat stat1 = zk.setData("/1246", "hahkkkkkkaha".getBytes(),8);
        //第二次修改node
        //Stat stat2 = zk.setData("/rockets", "shenjin".getBytes(), stat1.getVersion());


        /*System.out.println("++++++++before get+++++++++");
        //获取节点数据异步写法
        zk.getData("/rockets", true, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
                System.out.println("do callback");
                System.out.println(new String(bytes));
            }
        }, "");
        System.out.println("+++++++++after get+++++++++++");
*/

        Thread.sleep(20000);
    }
}