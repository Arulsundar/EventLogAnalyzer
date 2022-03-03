import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class Producer implements Runnable {

	long pointer;
	int BUFFER_SIZE = 1024 * 10;
	long handle;
	String user;
	File directory = new File("C:\\Users\\gnana-pt4726\\Desktop\\New\\pointer");

	public Producer(String machine) {
		this.handle = WinLog.openEventLog(machine);
		if (directory.isDirectory()) {
			if (directory.length() > 0) {
				File file = new File(directory.toPath() + machine + ".txt");
				try {
					BufferedReader reader = Files.newBufferedReader(file.toPath());
					this.pointer = Long.valueOf(reader.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					file.delete();
				}
			}
			this.pointer = WinLog.getOldestRecord(handle);
		}
		this.user = machine;
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.println(user + " " + handle + "  " + pointer);
				Properties[] records = WinLog.takeLogs(handle, pointer, BUFFER_SIZE);
				if (records.length > 0)
					for (Properties record : records) {
						WinLog.queue.put(record);
					}
				pointer += records.length;
//			System.out.println(pointer);
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
	}

	long getPointer() {
		return pointer;
	}
}
