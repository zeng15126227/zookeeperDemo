package UniformConfiguration;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class CallbackUtils implements Watcher, AsyncCallback.DataCallback,AsyncCallback.StatCallback {

    private ZooKeeper zk;
    private ConfData conf;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void setConf(ConfData conf) {
        this.conf = conf;
    }
    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }


    /**
     * AsyncCallback.DataCallback的回调函数
     * @param i
     * @param s
     * @param o
     * @param bytes
     * @param stat
     */
    @Override
    public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
        System.out.println("***** data callback *****");
        if(bytes!=null){
            conf.setData(new String(bytes));
            System.out.println("更新配置");
            countDownLatch.countDown();
        }else {
            System.out.println("数据不存在");
        }
    }

    /**
     * AsyncCallback.StatCallback的回调函数
     * @param i
     * @param s
     * @param o
     * @param stat
     */
    @Override
    public void processResult(int i, String s, Object o, Stat stat) {
        System.out.println("***** stat callback *****");
        if(stat!=null){
            zk.getData("/1246",this,this,"");
        }else {
            System.out.println("path不存在，方法阻塞中");
        }
    }

    /**
     * watch的回调函数
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("***** node watch *****");
        Event.EventType type = watchedEvent.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                zk.getData("/1246",this,this,"");
                break;
            case NodeDeleted:
                break;
            case NodeDataChanged:
                zk.getData("/1246",this,this,"");
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    /**
     * 注册watcher
     */
    public void registerWatcher(){
        //注册watcher
        //监控状态
        //zk.exists("/1246",this,this,"");

        zk.getData("/1246",this,this,"");

        try {
            //异步方式,保证获取到数据
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("结束await");
    }
}
