package tools;

import java.io.IOException;

public class FileDetector{
	
	public final static int ROW=0;
	public final static int COL=1;
	
	public double[] minArrX,maxArrX,meanArrX,stdArrX;
	public int colNumsX,rowNums,xStartIdx,yIdx;
	public FileDetector(String filePath,String delimiter,String varType) throws IOException
	{
		FReader reader=new FReader(filePath);
		int rowIdx=reader.getCurLineIdx();
		String line=reader.nextLine();
		String[] vals=line.split(delimiter);
		
		xStartIdx=varType.equals("ID-X-Y")?1:0;
		yIdx=vals.length-1;
		colNumsX=yIdx-xStartIdx;
		
		minArrX=new double[colNumsX];
		maxArrX=new double[colNumsX];
		meanArrX=new double[colNumsX];
		stdArrX=new double[colNumsX];
		double[] sumArrX=new double[colNumsX];
		double[] seqSumArrX=new double[colNumsX];
		
		for(int i=0;i<colNumsX;i++)
		{
			double val=Double.valueOf(vals[i+xStartIdx]);
			minArrX[i]=val;
			maxArrX[i]=val;
			sumArrX[i]=val;
			seqSumArrX[i]=Math.pow(val,2);
		}
		
		while(true)
		{
			rowIdx=reader.getCurLineIdx();
			line=reader.nextLine();
			if(line==null) break;
			
			vals=line.split(delimiter);
			for(int i=0;i<colNumsX;i++)
			{
				double val=Double.valueOf(vals[i+xStartIdx]);
				if(val<minArrX[i]) minArrX[i]=val;
				if(val>maxArrX[i]) maxArrX[i]=val;
				sumArrX[i]=sumArrX[i]+val;
				seqSumArrX[i]=seqSumArrX[i]+Math.pow(val,2);
			}
		}
		
		rowNums=rowIdx;
		for(int i=0;i<colNumsX;i++)
		{
			meanArrX[i]=sumArrX[i]/rowNums;
			stdArrX[i]=Math.pow(seqSumArrX[i]/(rowNums-1)-Math.pow(sumArrX[i],2)/(rowNums*(rowNums-1)),0.5);
		}
	}
	
	public double[] getMeanArrX()
	{
		return meanArrX;
	}
	
	public double[] getStdArrX()
	{
		return stdArrX;
	}
	
	public double[] getMinArrX()
	{
		return minArrX;
	}
	
	public double[] getMaxArrX()
	{
		return maxArrX;
	}
	
	public int getRowNums()
	{
		return rowNums;
	}
	
	public int getColNumsX()
	{
		return colNumsX;
	}
	
	public int getXStartIdx()
	{
		return xStartIdx;
	}
	
	public int getYIdx()
	{
		return yIdx;
	}
	
	public static int[] detectShape(String filePath,String delimiter) throws IOException
	{
		int[] shape=new int[2];
		int rowCnt=1;
		FReader reader=new FReader(filePath);
		String line=reader.nextLine();
		String[] vals=line.split(delimiter);

		for(line=reader.nextLine();line!=null;line=reader.nextLine())
		{
			rowCnt+=1;
		}
		shape[ROW]=rowCnt;
		shape[COL]=vals.length;
		
		return shape;
	}
	
	public static void main(String[] args) throws IOException
	{
//		int[] shape=detectShape("G:\\data\\test.TXT",",");

		FileDetector fileDetector=new FileDetector("G:\\data\\RecognizeNumber\\test.TXT",",","ID-X-Y");
		System.out.println(fileDetector.getXStartIdx());
		System.out.println(fileDetector.getYIdx());
		System.out.println(fileDetector.getRowNums());
		System.out.println(fileDetector.getColNumsX());
//		System.out.println(Arrays.toString(fileDetector.getMeanArrX()));
//		System.out.println(Arrays.toString(fileDetector.getStdArrX()));
//		System.out.println(Arrays.toString(fileDetector.getMinArrX()));
//		System.out.println(Arrays.toString(fileDetector.getMaxArrX()));
	}
	
	
}
