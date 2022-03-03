import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Producer implements Runnable,Closeable{

	static long pointer;
	int BUFFER_SIZE = 1024 * 10;
	long handle;
	String user;
	final String folderPath = "C:\\Users\\gnana-pt4726\\Desktop\\New\\pointer";
	File directory = new File(folderPath);

	public Producer(String machine) {
		this.handle = WinLog.openEventLog(machine);
		if (directory.isDirectory()) {
			if (directory.length() > 0) {
				File file = new File(directory.toPath() + machine + ".txt");
				try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {

					this.pointer = Long.valueOf(reader.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					file.delete();
				}
			}
			
		}
		else
		{
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
				Thread.currentThread().interrupt();
   			}
		}
	}

	long getPointer() {
		return pointer;
	}
    @Override
	public void close() throws IOException{

			OutputStream writer = Files.newOutputStream(Paths.get(folderPath + File.separator + user + ".txt"));
			writer.write(String.format("%s%n", getPointer()).getBytes());
			WinLog.closeEventLog(handle);
			System.out.println("closing producer");
		
	}
}
