import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WinLog  {

	static {
		System.loadLibrary("EventLogs");
	}

	public static native long getOldestRecord(long handle);

	public static native long openEventLog(String machine);

	public static native void closeEventLog(long handle);

	public static native Properties[] takeLogs(long handle, long pointer, int BUFFER_SIZE);

	static WrapperQueue<Properties> queue = new WrapperQueue<Properties>(1000);
	static ExecutorService pool = Executors.newFixedThreadPool(2);

//	PausableThreadPoolExecutor pool=new PausableThreadPoolExecutor(2);

	String machine;

	WinLog(String machine) {

		this.machine = machine;
	}

	public void start() {

		pool.execute(new Producer(machine));

		pool.execute(new Consumer());

	}

		


	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				pool.shutdownNow();
				System.out.println("Going to terminate");
			}
		});
	    WinLog obj = new WinLog("localhost") ;
			obj.start();
		
	}

}
/*
 * Handling ctrl+c in cmd; handle separate open ,read,and close event log. It
 * should be applicable for both local and remote machine
 */