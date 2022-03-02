import java.io.File;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;

public class DIYAppController extends Thread implements Serializable
{

	private static final long serialVersionUID = 1L;
	public ServerSocket server;
	public static ReentrantLock re = new ReentrantLock(true);
	Receptionist receptionist;

	
	public DIYAppController(ServerSocket s)
	{
		server = s;
	}
	
	@Override
	public void run()
	{
	
		try {	
			//Controller setting up receptionist
			System.out.println("Starting up Receptionist");
			receptionist = new Receptionist(server, re);
			receptionist.setDaemon(true);
			receptionist.start();
			System.out.println("Receptionist Started");	
			Shared.sliceQueue = new LinkedBlockingQueue<DataSlice>(1000);
			System.out.println("Queue created");
			Shared.sum = 0;
			Shared.count = 0;
			File file = new File("test.dat");
			Scanner infile = new Scanner(file);
			while(infile.hasNext())
			{
				//make and fill a slice
				double[] fSlice = new double[50];
				for(int i = 0; i < 50; i++)
				{
					if(infile.hasNext())
					{
						fSlice[i] = infile.nextDouble();
						Shared.count++;
					}	
				}
				DataSlice slice = new DataSlice(fSlice.length, fSlice);

				while(true)
				{
					try 
					{
						Shared.sliceQueue.add(slice);	
						System.out.println("Added slice number: " + Shared.count/50);
						break;
					}
					catch(Exception e)
					{
						continue;
					}
			
				}
	
			}
				
			
			infile.close();
			DataSlice poison = new DataSlice(0);
			while(true)
			{
				boolean getLock = re.tryLock(1,TimeUnit.MILLISECONDS);
				if(getLock)
				{
					try 
					{
						System.out.println("Controller: trying to put in poison");
						Shared.sliceQueue.add(poison);
						System.out.println("Poison queued");
						break;
					}
					catch(Exception e)
					{
						sleep(50);
						continue;
					}
					finally
					{
						re.unlock();
					}
				}
				else
				{
					//System.out.println("Controller: still waiting for poison lock...");
					Thread.sleep(10);
				}
			}
			
			
			while(Shared.workerCount!=0)
			{
				System.out.println("Waiting for workers to finish...");
				Thread.sleep(1000);
			}
			System.out.println("Sum is : " + Shared.sum);
			server.close();
			System.out.println("Server shutdown....");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("ERROR: Server shutdown");
		}

	}
	

}
