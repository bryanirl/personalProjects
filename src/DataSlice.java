import java.io.Serializable;

public class DataSlice implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int sliceSize;
	private double[] sliceData;
	
	public DataSlice(int size, double[] list)
	{
		sliceData = new double[size];
		sliceSize = size;
		for(int i = 0; i<size; i++)
		{
			sliceData[i] = list[i];
		}
	}
	public DataSlice(int size)
	{
		sliceSize = size;
		sliceData = null;
	}
	
	public int getSize()
	{
		return sliceSize;
	}
	
	public double get(int i)
	{
		return sliceData[i];
	}
}
