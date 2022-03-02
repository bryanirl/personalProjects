import java.io.IOException;
import java.net.ServerSocket;

public class demo {
	public static void main(String [] args) throws IOException
	{
		//Generate 500,000 random floats in test.dat for reading

		ServerSocket server = null;
		try {
			server = new ServerSocket(Integer.parseInt(args[0]));
		}
		catch(Exception E)
		{
			System.out.println("Port number is unacceptable... \nShutting down...");
			System.exit(0);
		}
		server.setReuseAddress(true);
		System.out.println("Opening new ServerSocket port: " + 6969);
		//Launch receptionist thread and read file
		DIYAppController controller = new DIYAppController(server);
		new Thread(controller).start();

	}
	
	
}
