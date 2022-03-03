import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WinLog {

	static {
		System.loadLibrary("EventLogs");
	}

	public static native long getOldestRecord(long handle);

	public static native long openEventLog(String machine);

	public static native void closeEventLog(long handle);

	public static native Properties[] takeLogs(long handle, long pointer, int BUFFER_SIZE);

	static WrapperQueue<Properties> queue = new WrapperQueue<Properties>(1000);
	static ExecutorService pool = Executors.newFixedThreadPool(2); 
	static List<Producer> list=new ArrayList<>();
//	PausableThreadPoolExecutor pool=new PausableThreadPoolExecutor(2);

	String machine;

	WinLog(String machine) {

		this.machine = machine;
	}

	public void start() {
        Producer obj=new Producer(machine);
        list.add(obj);
		pool.execute(obj);

		pool.execute(new Consumer());

	}

	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				pool.shutdown();
				System.out.println("Going to terminate");
					for(Producer p:list)
						try {
							p.close();
							new Consumer().close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
			}
		});
		WinLog obj = new WinLog("localhost");
		obj.start();

	}

}
/*
 * Handling ctrl+c in cmd; handle separate open ,read,and close event log. It
 * should be applicable for both local and remote machine
 */