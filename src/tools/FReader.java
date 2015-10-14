package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FReader {
	
	private String srcEncoding="utf-8";
	private BufferedReader bufferReader;
	private int curLineIdx=0;
	public FReader(String filePath) throws IOException
	{
		File srcFile = new File(filePath);
		InputStreamReader streamReader = new InputStreamReader(new FileInputStream(srcFile),srcEncoding);
		bufferReader=new BufferedReader(streamReader);
	}
	
	public String nextLine() throws IOException
	{
		curLineIdx++;
		return bufferReader.readLine();
	}
	
	public int getCurLineIdx()
	{
		return curLineIdx;
	}
	
	public void close() throws IOException
	{
		bufferReader.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		FReader fr=new FReader("G:\\data\\ttt.TXT");
		for (String line=fr.nextLine();line!=null;line=fr.nextLine())
		{
			System.out.println(line);
		}
	}

}
