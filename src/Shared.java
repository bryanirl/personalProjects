import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Shared {
	static double count;
	static BlockingQueue<DataSlice> sliceQueue = new LinkedBlockingQueue<DataSlice>(1000);
	static double sum;
	static int workerCount = 0;
}
