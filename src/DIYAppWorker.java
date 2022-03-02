import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class DIYAppWorker 
{
	public static void main(String[]args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{
		boolean firstSum = true;
		//grabs slice from waiter
		try(Socket socket = new Socket(args[0], Integer.parseInt(args[1])))
		{
			System.out.println("WORKER: Starting up worker");
			ObjectOutputStream toWaiter = new ObjectOutputStream(socket.getOutputStream());
			toWaiter.flush();
			ObjectInputStream fromWaiter = new ObjectInputStream(socket.getInputStream());

			while(true)
			{
				try {
					if(!firstSum)
					{
						DataSlice slice = (DataSlice) fromWaiter.readObject();
						//System.out.println("WORKER: Recieved Slice from Waiter");
						double sum = 0;
						
						if(slice.getSize()==0)
						{
							System.out.println("WORKER: Poison Detected... Breaking connection from waiter");
							toWaiter.writeObject(new PartialSum(0));
							toWaiter.flush();
							Shared.workerCount--;
							break;
						}
						
						for(int i = 0; i<slice.getSize(); i++)
						{
							sum += slice.get(i);
						}
						
						//System.out.println("WORKER: Sending sum to waiter");
						toWaiter.writeObject(new PartialSum(sum));
						toWaiter.flush();
					}
					else
					{
						toWaiter.writeObject(new PartialSum((double)-1));
						toWaiter.flush();
						firstSum=false;
					}
				}
				catch(NullPointerException e)
				{
					System.out.println("somehow received a null slice, asking for a new one");
					toWaiter.writeObject(new PartialSum((double)-1));
					toWaiter.flush();
					continue;
				}
				
			}
			System.out.println("WORKER: Finished because of poison");
	
		}
		
		catch (IOException e)
		{
			System.out.println("Server not started or closed...");
			System.exit(0);
		}
		catch (Exception e)
		{
			System.out.println("Server address or port unacceptable...\nRun and try again...");
		}
		//adds slice 
		//sends partial sum to waiter
		
	}

}
