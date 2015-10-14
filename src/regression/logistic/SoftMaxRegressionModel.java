package regression.logistic;

import java.io.FileWriter;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.ClassificationModel;
import tools.FReader;

public class SoftMaxRegressionModel extends ClassificationModel{

	public double[][] weightX; //变量权重
	public double[] weight0; //截距项
	public int inputDim,outputDim; //inputDim为x的输入维度,outputDim为y的分类个数
	public void initWeight(int inputDim,int outputDim)
	{
		this.inputDim=inputDim;
		this.outputDim=outputDim;
		weightX=new double[outputDim][inputDim];
		weight0=new double[outputDim];
//		for(int i=0;i<outputDim-1;i++)
//		{
//			for(int j=0;j<inputDim+1;j++)
//			{
//				weight[i][j]=2*Math.random()-1;
//			}
//		}
	}
	
	public void updateWeight(double[][] deltaWeightX,double[] deltaWeight0)
	{
		for(int o=0;o<outputDim-1;o++)
		{
			weight0[o]+=deltaWeight0[o];
			for(int i=0;i<inputDim;i++)
			{
				weightX[o][i]+=deltaWeightX[o][i];
			}
		}
	}
	
	public void updateWeight(double[][] deltaWeightX,double[] deltaWeight0,double mu)
	{
		for(int o=0;o<outputDim-1;o++)
		{
			weight0[o]+=(mu*deltaWeight0[o]);
			for(int i=0;i<inputDim;i++)
			{
				weightX[o][i]+=(mu*deltaWeightX[o][i]);
			}
		}
	}
	
	public double[] calculateLogitArr(double[] input)
	{
		double[] logitArr=new double[outputDim];
		for(int i=0;i<outputDim;i++)
		{
			double logLogit=calculateLogLogit(input,i);
			
			logitArr[i]=Math.exp(logLogit);
		}
		return logitArr;
	}
	
	public double calculateLogLogit(double[] input,int yIdx)
	{
		double logLogit=weight0[yIdx];
		for(int j=0;j<inputDim;j++)
		{
			logLogit+=input[j]*weightX[yIdx][j];
			
		}
		return logLogit;
	}
	
	@Override
	public void save(String modelSavePath) throws IOException {
		System.out.println("--saving model...");
		FileWriter writer=new FileWriter(modelSavePath);
		JSONObject json=new JSONObject();
		json.put("inputDim",inputDim);
		json.put("outputDim",outputDim);
		json.put("weightX", weightX);
		json.put("weight0", weight0);
		json.put("outputFlagArr", outputFlagArr);
		json.put("standardizeMethod", standardizeMethod);
		if(standardizeMethod==null){}
		else if(standardizeMethod.equals("z-score"))
		{
			json.put("meanArr",meanArr);
			json.put("stdArr", stdArr);
		}
		else if(standardizeMethod.equals("min-max"))
		{
			json.put("minArr",minArr);
			json.put("maxArr", maxArr);
		}
		writer.write(json.toString());
		writer.close();
		System.out.println("--save model succeed!");
	}
	
	public static SoftMaxRegressionModel load(String modelSavePath) throws IOException
	{
		System.out.println("--loading model...");
		FReader reader=new FReader(modelSavePath);
		JSONObject json=JSON.parseObject(reader.nextLine());
		int inputDim=json.getIntValue("inputDim");
		int outputDim=json.getIntValue("outputDim");
		JSONArray jsonWeightX = json.getJSONArray("weightX");
		JSONArray jsonWeight0 = json.getJSONArray("weight0");
		JSONArray jsonOutputFlag = json.getJSONArray("outputFlagArr");
		SoftMaxRegressionModel model=new SoftMaxRegressionModel();
		model.initWeight(inputDim, outputDim);
		model.setWeight(jsonWeightX,jsonWeight0);
		
		model.setOutputFlag(jsonOutputFlag);
		model.setStandardizeMethod(json.getString("standardizeMethod"));
		if(model.standardizeMethod==null) {}
		else if(model.standardizeMethod.equals("min-max"))
		{
			JSONArray JsonMinArr=json.getJSONArray("minArr");
			JSONArray JsonMaxArr=json.getJSONArray("maxArr");
			model.setMinMax(JsonMinArr, JsonMaxArr);
		}
		else if(model.standardizeMethod.equals("z-score"))
		{
			JSONArray JsonMeanArr=json.getJSONArray("meanArr");
			JSONArray JsonStdArr=json.getJSONArray("stdArr");
			model.setMeanStd(JsonMeanArr, JsonStdArr);
		}
		System.out.println("--load model succeed!");
		return model;
	}
	
	private void setWeight(JSONArray jsonWeightX,JSONArray jsonWeight0) {
		// TODO Auto-generated method stub
		for(int o=0;o<outputDim;o++)
		{
			for(int i=0;i<inputDim;i++)
			{
				weightX[o][i]=jsonWeightX.getJSONArray(o).getDoubleValue(i);
			}
			weight0[o]=jsonWeight0.getDoubleValue(o);
		}
	}

	@Override
	public String getOutputClass(double[] input, boolean standardizeTrans) {

		if(standardizeTrans==true) standardizeInput(input);
		double[] logitArr=calculateLogitArr(input);
		int maxLogitIdx=0;
		double maxLogit=0;
		for(int i=0;i<outputDim;i++)
		{
			if(logitArr[i]>maxLogit)
			{
				maxLogitIdx=i;
				maxLogit=logitArr[i];
			}
		}
		return outputFlagArr[maxLogitIdx];
	}
}
