import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Waiter extends Thread
{
	private final Socket client;
	public ReentrantLock re;
	
	public Waiter(Socket c, ReentrantLock re)
	{
		client = c;
		this.re = re;
		Shared.workerCount++;
	}
	
	@Override
	public void run() 
	{
		//Sets up streams
		this.setName(String.format("Waiter %d", Shared.workerCount));
		ObjectOutputStream workerOut = null;
		ObjectInputStream workerIn = null;
		System.out.printf("%s: Started\n", this.getName());
		try 
		{
			//initializes streams and a temp dataslice object for efficiency I guess
			workerOut = new ObjectOutputStream(client.getOutputStream());
			workerOut.flush();
			workerIn = new ObjectInputStream(client.getInputStream());
			DataSlice temp;
			while(true)
			{
				PartialSum doubleSum = (PartialSum) workerIn.readObject();
				//System.out.printf("%s: Recieved data from worker\n", this.getName());
				double partialSum = doubleSum.sum;
				if(partialSum > 0 || partialSum < 0)
				{
					//System.out.printf("%s: Preparing to send worker some data\n", this.getName());
					
					while(true)
					{
						boolean getLock = re.tryLock(1,TimeUnit.MILLISECONDS);
						if(getLock)
						{
							//System.out.printf("%s: locked and is trying to read from queue\n", this.getName());
							try 
							{
								temp = Shared.sliceQueue.peek();
								if(temp==null)
								{
									//System.out.printf("%s: slice was null, waiting and trying again\n", this.getName());
									continue;
								}
								else 
								{
									if(partialSum>0)
									{
										Shared.sum += partialSum;
										if(Shared.sliceQueue.peek().getSize()==0)
											temp = Shared.sliceQueue.peek();
										else
											temp = Shared.sliceQueue.poll();
										
										System.out.printf("%s: CURRENT SUM: %f\n", this.getName(), Shared.sum);
										workerOut.writeObject(temp);
										workerOut.flush();
										break;
									}
									else
									{
										temp = Shared.sliceQueue.poll();
										workerOut.writeObject(temp);
										workerOut.flush();
										break;
									}
								}
								
								
							}
							finally 
							{
								re.unlock();
								Thread.sleep(5);
							}
						}
					}
				}
				else
				{
					System.out.printf("%s: worker is done and I will now attempt to kill it\n", this.getName());
					workerOut.close();
					workerIn.close();
					client.close();
					Shared.workerCount--;
					System.out.printf("%s: unlocked\n", this.getName());
					break;
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		System.out.printf("%s: finished and worker is dead\n", this.getName());
		}
}