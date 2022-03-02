import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectStore<T> {
	private final String folderPath = "C:\\Users\\gnana-pt4726\\Desktop\\New\\files" ;
	private final File folder = new File(folderPath);

	private final AtomicLong size = new AtomicLong();

	public ObjectStore() {
		for (File subfile : folder.listFiles())
		{
			if(subfile!=null)
		      subfile.delete();
			else
				break;
		}	}

	

	public void writeQueue(MyArrayBlockingQueue<T> queue) throws IOException {
		size.incrementAndGet();

		final long timeStamp = System.nanoTime();

		Path lockedFilePath = Paths.get(folderPath + File.separator + timeStamp + ".txt.writing");
		Path unlockedFilePath = Paths.get(folderPath + File.separator + timeStamp + ".txt") ;

		try (OutputStream fos = Files.newOutputStream(lockedFilePath);
			 ObjectOutputStream oos = new ObjectOutputStream(fos))
		{
//			System.out.println("Writing to file");
			oos.writeObject(queue);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		Files.move(lockedFilePath, unlockedFilePath);
	}

	@SuppressWarnings("unchecked")
	public MyArrayBlockingQueue<T> readQueue() {
		final FileFilter unlockedFileFilter = f -> f.getName().endsWith("txt") ;
		File[] files=folder.listFiles(unlockedFileFilter);
		Arrays.sort(files);
		MyArrayBlockingQueue<T> queue = null;
		if(files.length>0)
		{
		   File file = files[0];
		   size.getAndDecrement();

		try (InputStream is = new FileInputStream(file);
			 ObjectInputStream ois = new ObjectInputStream(is))
		{
//			System.out.println("Reading occurs");
			queue = (MyArrayBlockingQueue<T>) ois.readObject();
//			System.out.println("Read from file");
			return queue ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		} finally {
			file.delete();
//			System.out.println("Reading over");
		}
		}
		return queue;
	}

	public long size() {
		return size.get();
	}
}