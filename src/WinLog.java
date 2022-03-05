import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	static ExecutorService pool = Executors.newFixedThreadPool(10);
	static List<Producer> list = new ArrayList<>();

	public void start() {
		try(BufferedReader reader=Files.newBufferedReader(Paths.get("C:\\Users\\gnana-pt4726\\Desktop\\New\\ipConf\\conf.txt")))
		{
//		Producer obj = new Producer("localhost");
//      Producer p1=new Producer("ip1");
//      Producer p2=new Producer("ip2");
		list.add(new Producer(reader.readLine()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Producer i : list)
			pool.execute(i);

		pool.execute(new Consumer());

	}

	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				pool.shutdown();
				System.out.println("Going to terminate");
				for (Producer p : list)
					try {
						p.close();
						new Consumer().close();
					} catch (IOException e1) {
						throw new RuntimeException();
					}
			}
		});
		WinLog obj = new WinLog();
		obj.start();

	}

}
