package tools;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomReader {

	public RandomAccessFile f;
	public long[] positionArr;
	long fileLength;

	public RandomReader(String filePath,int rowNums) throws IOException
	{
		f=new RandomAccessFile(filePath,"r");
		positionArr=new long[rowNums];		
		long pointer=0;
		for(int idx=0;pointer<f.length();idx++)
		{
			positionArr[idx]=pointer;
			f.readLine();
			pointer=f.getFilePointer();
		}
	}
	
	public void setPointerToStart() throws IOException
	{
		f.seek(0);
	}
	
	public String nextLine() throws IOException
	{
		return f.readLine();
	}
	
	public String readLine(int randomNums) throws IOException
	{
		if(randomNums>positionArr.length)
		{
			throw new IllegalArgumentException(String.format("输入行号(%d)超出了文件行数(%d)！", randomNums,positionArr.length));
		}
		if(randomNums<=0)
		{
			throw new IllegalArgumentException("行号计算从1开始，请确保输入行号大于0！");
		}
		f.seek(positionArr[randomNums-1]);
		return f.readLine();
	}
	
	public void close() throws IOException
	{
		f.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		RandomReader rr=new RandomReader("G:\\data\\test.txt",75);
		System.out.println(rr.readLine(1));
		System.out.println(rr.readLine(5));
		System.out.println(rr.readLine(75));
	}
	
	
}
