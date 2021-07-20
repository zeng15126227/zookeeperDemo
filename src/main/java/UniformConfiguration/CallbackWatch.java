package UniformConfiguration;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class CallbackWatch implements Watcher, AsyncCallback.DataCallback,AsyncCallback.StatCallback {

    ZooKeeper zk;
    CallbackResData resData;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    public CallbackResData getResData() {
        return resData;
    }

    public void setResData(CallbackResData resData) {
        this.resData = resData;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }



    public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
        System.out.println("data callback");
        if(bytes!=null){
            resData.setData(new String(bytes));
            System.out.println("更新配置");
            countDownLatch.countDown();
        }else {
            System.out.println("数据不存在");
        }
    }

    public void processResult(int i, String s, Object o, Stat stat) {
        System.out.println("stat callback");
        if(stat!=null){
            zk.getData("/1246",this,this,"");
        }else {
            System.out.println("path不存在，方法阻塞中");
        }
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println("watch callback");
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

    public void await(){
        zk.exists("/1246",this,this,"");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
