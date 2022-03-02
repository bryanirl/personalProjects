import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Receptionist extends Thread
{
	private ServerSocket server;
	public ReentrantLock re;
	
	public Receptionist(ServerSocket s, ReentrantLock re)
	{
		this.re = re;
		server = s;
	}
	
	@Override
	public void run() 
	{
		try 
		{	
			while(true)
			{
				Socket worker = server.accept();
				System.out.println("Accepted Worker");
				Waiter waiter = new Waiter(worker, re);
				waiter.start();
				System.out.println("Started Waiter");
			} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}
