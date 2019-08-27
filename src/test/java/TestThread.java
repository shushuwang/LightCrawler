import java.util.concurrent.*;

/**
 * @author shushuwang
 * @create 2019-04-23 19:49
 **/
public class TestThread {
    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100));

//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        System.out.println(executor.toString());
        for (int i = 0; i < 20; i++) {

            executor.execute(new Task(i));
            System.out.println("====");
            System.out.println(executor.toString());
        }

        Thread.sleep(5000);
        new Thread();

        for (int i = 0; i < 20; i++) {

            executor.execute(new Task(i));
            System.out.println("====");
            System.out.println(executor.toString());
        }

        while (true) {
            System.out.println(executor.toString());
            Thread.sleep(1000);
        }

//        System.out.println("running..");
    }
}

class MyThreadPoolExecutor extends ThreadPoolExecutor {
    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }
}

class Task implements Runnable{
    private int i;
    Task(int i){
        this.i = i;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String x = Thread.currentThread().toString();
        System.out.println(x + " start " + i);
//        if (true) {
//            x = null;
//        }
//        Integer.parseInt(x);
        System.out.println(x + " end " + i);


    }

    @Override
    public String toString() {
        return "task:" + i;
    }
}
