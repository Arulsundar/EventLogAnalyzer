import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
//	ExecutorService pool = Executors.newFixedThreadPool(2);
	
	PausableThreadPoolExecutor pool=new PausableThreadPoolExecutor(2);

	String machine;

	WinLog(String machine) {

		this.machine = machine;
	}

	public void start() {

		pool.execute(new Producer(machine));

		pool.execute(new Consumer());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				pool.shutdown();
			}
		});
		

	}

	public void close() {
//		pool.shutdown();
//		try {
//			OutputStream writer = Files.newOutputStream(Paths.get("C:\\Users\\gnana-pt4726\\Desktop\\New\\pointer.txt"),
//					StandardOpenOption.WRITE);
//			writer.write(String.format("%s%n", Producer.pointer).getBytes());
//		} catch (IOException e) {
//			throw new RuntimeException();
//		}
        pool.pause();
		Consumer.bulkProcessor.flush();
	}

	public static void main(String[] args) throws InterruptedException {
		WinLog obj = new WinLog("localhost");
		obj.start();
//		Thread.sleep(10000);
//		obj.close();
//		Thread.sleep(10000);
//		obj.start();
	}

}
/*
 * Handling ctrl+c in cmd; handle separate open ,read,and close event log. It
 * should be applicable for both local and remote machine
 */