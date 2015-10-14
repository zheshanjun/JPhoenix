package tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;

public abstract class ClassificationModel {
	
	public String[] outputFlagArr;
	public Map<String,Integer> outputFlagMap;
	public double[] minArr,maxArr;
	public double[] meanArr,stdArr;
	public String standardizeMethod;
	
	public abstract void save(String modelSavePath) throws IOException;
//	public static ClassificationModel load(String modelSavePath) throws IOException;
	public abstract String getOutputClass(double[] input,boolean standardizeTrans);
	
	
	
	public String getOutputClass(double[] input)
	{
		return getOutputClass(input,false);
	}
	public void setOutputFlag(String[] outputFlagArr)
	{
		this.outputFlagArr=outputFlagArr;
		outputFlagMap=new HashMap<String,Integer>(outputFlagArr.length);
		for (int i=0;i<outputFlagArr.length;i++)
		{
			outputFlagMap.put(outputFlagArr[i],i);
		}
	}
	
	public void setOutputFlag(JSONArray jsonOutputFlagArr)
	{
		int size=jsonOutputFlagArr.size();
		outputFlagMap=new HashMap<String,Integer>(size);
		this.outputFlagArr=new String[size];
		for (int i=0;i<size;i++)
		{
			outputFlagMap.put(outputFlagArr[i],i);
			this.outputFlagArr[i]=jsonOutputFlagArr.getString(i);
		}
	}
	
	public void setMinMax(double[] minArr,double[] maxArr)
	{
		this.minArr=minArr;
		this.maxArr=maxArr;
	}
	
	public void setMinMax(JSONArray jsonMinArr,JSONArray jsonMaxArr)
	{
		int size=jsonMinArr.size();
		this.minArr=new double[size];
		this.maxArr=new double[size];
		
		for(int i=0;i<size;i++)
		{
			this.minArr[i]=jsonMinArr.getDoubleValue(i);
			this.maxArr[i]=jsonMaxArr.getDoubleValue(i);
		}
	}
	
	public void setMeanStd(double[] meanArr,double[] stdArr)
	{
		this.meanArr=meanArr;
		this.stdArr=stdArr;
	}
	
	public void setMeanStd(JSONArray jsonMeanArr,JSONArray jsonStdArr)
	{
		int size=jsonMeanArr.size();
		this.meanArr=new double[size];
		this.stdArr=new double[size];
		for(int i=0;i<size;i++)
		{
			this.meanArr[i]=jsonMeanArr.getDoubleValue(i);
			this.stdArr[i]=jsonStdArr.getDoubleValue(i);
		}
	}
	
	public void setStandardizeMethod(String standardizeMethod) 
	{
		this.standardizeMethod=standardizeMethod;
	}
	
	public void standardizeInput(double[] input)
	{
		if(standardizeMethod==null){}
		else if(standardizeMethod.equals("z-score"))
		{
			for(int i=0;i<input.length;i++)
			{
				double denominator=stdArr[i];
				input[i]=denominator==0?0:(input[i]-meanArr[i])/denominator;
			}
		}
		else if(standardizeMethod.equals("min-max"))
		{
			for(int i=0;i<input.length;i++)
			{
				double denominator=maxArr[i]-minArr[i];
				input[i]=denominator==0?0:(input[i]-minArr[i])/denominator;
			}
		}
		
	}
}
