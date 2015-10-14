package tools;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DataModel {

	private double[][] xMatrix;
	private double[] minArr,maxArr,meanArr,stdArr;
	private String[] idVector,yVector;
	private int colNumsX; //
	private int rowNums;
	private String varType;
	private HashMap<String,Integer> yFlagMap=new HashMap<String,Integer>();
	private String[] yFlagArr;
	/*
	 * 返回自变量个数、数据大小、变量均值、标准差、最大值、最小值、自变量X矩阵,因变量Y向量
	 */
	
	public DataModel(String srcPath,String delimiter,String varType,String standardizeMethod) throws IOException
	{
		if(!varType.equals("ID-X-Y") && !varType.equals("X-Y"))
		{
			throw new IllegalArgumentException("请输入正确的变量排列类型：'ID-X-Y' 或 'X-Y'");
		}
		
		FileDetector fileDetector=new FileDetector(srcPath,delimiter,varType);
		int xStartIdx=fileDetector.getXStartIdx();
		this.varType=varType;
		rowNums=fileDetector.getRowNums();
		idVector=varType.equals("ID-X-Y")?new String[rowNums]:null;
		colNumsX=fileDetector.getColNumsX();
		xMatrix=new double[rowNums][colNumsX];
		yVector=new String[rowNums];
		minArr=fileDetector.getMinArrX();
		maxArr=fileDetector.getMaxArrX();
		meanArr=fileDetector.getMeanArrX();
		stdArr=fileDetector.getStdArrX();
		
		FReader reader=new FReader(srcPath);
		while (true)
		{
			int rowIdx=reader.getCurLineIdx();
			String line=reader.nextLine();
			if(line==null) break;
			String[] vals=line.split(delimiter,-1);
			String y=vals[fileDetector.getYIdx()];
			yVector[rowIdx]=y;
//			System.out.println(fileDetector.getYIdx()+","+y);
			if(!yFlagMap.containsKey(y))
			{
				int curYNums=yFlagMap.size();
				yFlagMap.put(y,curYNums);
			}
			
			if(varType.startsWith("ID"))
			{
				idVector[rowIdx]=vals[0];
			}
			for(int i=0;i<colNumsX;i++)
			{
				double val=Double.valueOf(vals[i+xStartIdx]);
				if(standardizeMethod==null)
				{
					xMatrix[rowIdx][i]=val;
				}
				else if(standardizeMethod.equals("min-max"))
				{
					double denominator=maxArr[i]-minArr[i];
					xMatrix[rowIdx][i]=denominator==0?0:(val-minArr[i])/denominator;
				}
				else if(standardizeMethod.equals("z-score"))
				{
					double denominator=stdArr[i];
					xMatrix[rowIdx][i]=denominator==0?0:(val-meanArr[i])/denominator;
				}
			}
		}
		
		int outputLayerSize=yFlagMap.size();
		yFlagArr=new String[outputLayerSize];		
		Iterator<Entry<String, Integer>> iterator = yFlagMap.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String, Integer> entry = iterator.next();
			yFlagArr[entry.getValue()]=entry.getKey();
		}
	}
	
	public double[][] getXMatrix()
	{
		return xMatrix;
	}
	
	public double[] getX(int idx)
	{
		return xMatrix[idx];
	}
	
	public String[] getYVector()
	{
		return yVector;
	}
	
	public String getY(int idx)
	{
		return yVector[idx];
	}
	
	public String[] getIdVector()
	{
		return idVector;
	}
	
	public String getId(int idx)
	{
		
		return idVector==null?String.valueOf(idx):idVector[idx];
	}
	
	
	public double[] getMeanArr()
	{
		return meanArr;
	}
	
	public double[] getStdArr()
	{
		return stdArr;
	}
	
	public double[] getMinArr()
	{
		return minArr;
	}
	
	public double[] getMaxArr()
	{
		return maxArr;
	}
	
	public String getVarType()
	{
		return varType;
	}
	
	public int getRowNums()
	{
		return rowNums;
	}
	
	public int getColNumsX()
	{
		return colNumsX;
	}
	
	public HashMap<String,Integer> getYFlagMap()
	{
		return yFlagMap;
	}
	
	public String[] getYFlagArr()
	{
		return yFlagArr;
	}
	
	public int getYIdx(int idx)
	{
		return yFlagMap.get(getY(idx));
	}
	
//	public static void main(String[] args) throws IOException
//	{
//		long startTime=System.currentTimeMillis();
//		DataModel dataModel=new DataModel("G:\\data\\RecognizeNumber\\test.TXT",",","ID-X-Y","z-score");
//		System.out.println(dataModel.getRowNums());
//		System.out.println(dataModel.getColNumsX());
//		System.out.println(dataModel.getVarType());
//		System.out.println(Arrays.toString(dataModel.getYVector()));
//		System.out.println(Arrays.toString(dataModel.getIdVector()));
//		System.out.println(Arrays.toString(dataModel.getMeanArr()));
//		System.out.println(Arrays.toString(dataModel.getStdArr()));
//		System.out.println(Arrays.toString(dataModel.getMinArr()));
//		System.out.println(Arrays.toString(dataModel.getMaxArr()));
//		long endTime  = System.currentTimeMillis();
//		System.out.println("运行时间：" + (endTime - startTime) + "ms");
//	}
}
