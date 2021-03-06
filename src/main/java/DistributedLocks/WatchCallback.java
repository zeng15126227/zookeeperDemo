package DistributedLocks;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, AsyncCallback.StatCallback {

    ZooKeeper zk;
    String threadName;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    String pathName;

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void tryLock() throws InterruptedException {
        zk.create("/lock",
                getThreadName().getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL,
                this,
                "");
        countDownLatch.await();
    }

    public void unLock() throws KeeperException, InterruptedException {
        System.out.println("释放锁："+pathName);
        //删除获取锁的节点
        zk.delete(pathName,-1);
    }


    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("***** node watcher *****");
        Event.EventType type = watchedEvent.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //删除节点的下一个节点获取锁
                zk.getChildren("/", false, this, "");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    @Override
    public void processResult(int i, String s, Object o, String s1) {
        System.out.println("***** create callback *****");
        System.out.println("create path:" + s1);
        pathName = s1;
        if (s1 != null) {
            //监控前一个节点
            zk.getChildren("/", false, this, "");
        }

    }

    @Override
    public void processResult(int i, String s, Object o, List<String> list) {
        System.out.println("***** children callback *****");
        if (!list.isEmpty()) {
            //seq node path排序
            Collections.sort(list);
            int index = list.indexOf(pathName.substring(1));
            //seq node path最小的获取锁
            if (index == 0) {
                System.out.println(getThreadName() + "获得锁");
                countDownLatch.countDown();
            } else {
                //监控前一个seq node path
                zk.exists("/" + list.get(index - 1), this, this, "");
            }
        }


    }

    @Override
    public void processResult(int i, String s, Object o, Stat stat) {
        System.out.println("***** exists callback *****");
    }
}
