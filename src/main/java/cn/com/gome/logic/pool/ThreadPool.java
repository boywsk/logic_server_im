package cn.com.gome.logic.pool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 */
public class ThreadPool {
	private final LinkedBlockingQueue<Runnable> queue =  new LinkedBlockingQueue<Runnable>(3000);
	private final int availableProcessors = Runtime.getRuntime().availableProcessors();
	private final ThreadPoolExecutor te = new ThreadPoolExecutor(availableProcessors,
			availableProcessors * 2 + 1, 10L,TimeUnit.MICROSECONDS, queue);
	
    private static ThreadPool instance = new ThreadPool();
    
    private ThreadPool() {
    	te.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    public static ThreadPool getInstance() {
    	return instance;
    }
    
    /**
     * 加入线程池
     * @param run
     */
    public void addTask(Runnable run) {
    	te.execute(run);
    }
    
    /**
     * 取得线程池当前剩余容量
     * @return
     */
    public int getRemainingCapacity() {
    	return queue.remainingCapacity();
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
    	te.shutdown();
    }
}
