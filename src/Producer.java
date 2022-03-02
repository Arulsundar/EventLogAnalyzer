import java.util.Properties;

public class Producer implements Runnable {
	 
	 
	 long pointer;
     int BUFFER_SIZE=1024*10;
     long handle;
     String user;
     public Producer(String machine)
     {   
    	 this.handle=WinLog.openEventLog(machine);
    	 this.pointer=WinLog.getOldestRecord(handle);
    	 this.user=machine;
     }
	@Override
	public void run() {
		while (true) {
			try {
				System.out.println(user+" "+handle+"  "+pointer);
			Properties[] records =WinLog.takeLogs(handle,pointer,BUFFER_SIZE);
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

}
