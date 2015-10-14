package ann.bp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import tools.ClassificationModel;
import tools.FReader;

public class BPAnnModel extends ClassificationModel{

	public HiddenNeuron[] hiddenLayer;
	public OutputNeuron[] outputLayer;
	public int inputLayerSize,hiddenLayerSize,outputLayerSize;
	
	public BPAnnModel(int inputLayerSize,int hiddenLayerSize,int outputLayerSize)
	{
		this.inputLayerSize=inputLayerSize;
		this.hiddenLayerSize=hiddenLayerSize;
		this.outputLayerSize=outputLayerSize;
		
		hiddenLayer=new HiddenNeuron[hiddenLayerSize];
		outputLayer=new OutputNeuron[outputLayerSize];
		outputFlagArr=new String[outputLayerSize];
		outputFlagMap=new HashMap<String,Integer>(outputLayerSize);
		
		for(int h=0;h<hiddenLayerSize;h++)
		{
			hiddenLayer[h]=new HiddenNeuron(inputLayerSize,h);
		}
		
		for(int o=0;o<outputLayerSize;o++)
		{
			outputLayer[o]=new OutputNeuron(hiddenLayerSize,o);
		}
		
		minArr=new double[inputLayerSize];
		maxArr=new double[inputLayerSize];
	}

	public double[] transYToArr(String y)
	{
		int nonzeroIdx=outputFlagMap.get(y);
		double[] yArr=new double[outputLayerSize];
		yArr[nonzeroIdx]=1;
		return yArr;
	}
		
	public double[] calculateHiddenLayerOutput(double[] inputLayer)
	{
		double[] hiddenLayerOut=new double[hiddenLayer.length];
		for(int h=0;h<hiddenLayerSize;h++)
		{
			hiddenLayerOut[h]=hiddenLayer[h].calculateOutput(inputLayer);
		}
		return hiddenLayerOut;
	}

	public void standardizeInputLayer(double[] inputLayer)
	{
		if(standardizeMethod==null){}
		else if(standardizeMethod.equals("z-score"))
		{
			for(int i=0;i<inputLayerSize;i++)
			{
				double denominator=stdArr[i];
				inputLayer[i]=denominator==0?0:(inputLayer[i]-meanArr[i])/denominator;
			}
		}
		else if(standardizeMethod.equals("min-max"))
		{
			for(int i=0;i<inputLayerSize;i++)
			{
				double denominator=maxArr[i]-minArr[i];
				inputLayer[i]=denominator==0?0:(inputLayer[i]-minArr[i])/denominator;
			}
		}
	}
	
	public double[] calculateModelOutput(double[] inputLayer,boolean standardizeTrans) throws IllegalArgumentException
	{
		if(inputLayer.length!=inputLayerSize)
		{
			throw new IllegalArgumentException("输入层数组长度与模型输入层数目不匹配！");
		}
		
		if(standardizeTrans==true)
		{
			standardizeInputLayer(inputLayer);
		}
		
		double[] hiddenLayerOut=calculateHiddenLayerOutput(inputLayer);
		double[] outputLayerOut=new double[outputLayer.length];
		for(int o=0;o<outputLayerSize;o++)
		{
			outputLayerOut[o]=outputLayer[o].calculateOutput(hiddenLayerOut);
		}
		return outputLayerOut;
	}
	
//	public double[] calculateModelOutput(double[] inputLayer) throws IllegalArgumentException
//	{
//		return calculateModelOutput(inputLayer,false);
//	}
	
//	public String getOutputClass(double[] inputLayer)
//	{
//		return getOutputClass(inputLayer,false);
//	}

	public String getOutputClass(double[] inputLayer,boolean standardizeTrans) throws IllegalArgumentException
	{
		double[] outArr=calculateModelOutput(inputLayer,standardizeTrans);
		double maxOut=0;
		int outIdx=0;
		for(int i=0;i<outArr.length;i++)
		{
			if(outArr[i]>maxOut)
			{
				maxOut=outArr[i];
				outIdx=i;
			}
		}
		if(maxOut>=0.7)
		{
			return outputFlagArr[outIdx];
		}
		else
		{
			return "unknown";
		}
	}
	
	public void save(String modelSavePath) throws IOException, JSONException
	{
//		System.out.println("--saving model...");
		FileWriter writer=new FileWriter(modelSavePath);
		JSONObject json=new JSONObject();
		json.put("inputLayerSize",inputLayerSize);
		json.put("hiddenLayerSize", hiddenLayerSize);
		json.put("outputLayerSize",outputLayerSize);
		json.put("outputFlagArr", outputFlagArr);
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

		json.put("standardizeMethod", standardizeMethod);
		
		double[][] WHI=new double[hiddenLayerSize][];
		double[][] WOH=new double[outputLayerSize][];

		for(int h=0;h<hiddenLayerSize;h++)
		{
			WHI[h]=hiddenLayer[h].weight;
		}
		for(int o=0;o<outputLayerSize;o++)
		{
			WOH[o]=outputLayer[o].weight;
		}
		json.put("WHI",WHI);
		json.put("WOH", WOH);
		writer.write(json.toString());
		writer.close();
//		System.out.println("--save model succeed!");
	}
	
	public static BPAnnModel load(String modelSavePath) throws IOException, JSONException
	{
//		System.out.println("--loading model...");
		
		FReader reader=new FReader(modelSavePath);
		
		JSONObject json=JSON.parseObject(reader.nextLine());
		int inputLayerSize=json.getInteger("inputLayerSize");
		int hiddenLayerSize=json.getInteger("hiddenLayerSize");
		int outputLayerSize=json.getInteger("outputLayerSize");
		
		JSONArray JsonOutputFlag = json.getJSONArray("outputFlagArr");
		BPAnnModel model=new BPAnnModel(inputLayerSize,hiddenLayerSize,outputLayerSize);
		model.setOutputFlag(JsonOutputFlag);
		model.setStandardizeMethod(json.getString("standardizeMethod"));
		if(model.standardizeMethod==null) {}
		else if(model.standardizeMethod.equals("min-max"))
		{
			JSONArray jsonMinArr=json.getJSONArray("minArr");
			JSONArray jsonMaxArr=json.getJSONArray("maxArr");
			model.setMinMax(jsonMinArr, jsonMaxArr);
		}
		else if(model.standardizeMethod.equals("z-score"))
		{
			JSONArray jsonMeanArr=json.getJSONArray("meanArr");
			JSONArray jsonStdArr=json.getJSONArray("stdArr");
			model.setMeanStd(jsonMeanArr, jsonStdArr);			
		}

		JSONArray JsonWHI = json.getJSONArray("WHI");
		for(int h=0;h<JsonWHI.size();h++)
		{
			JSONArray tmpArr = JsonWHI.getJSONArray(h);
			for(int i=0;i<tmpArr.size();i++)
			{
				model.hiddenLayer[h].weight[i]=tmpArr.getDouble(i);
			}
		}
		
		JSONArray JsonWOH = json.getJSONArray("WOH");
		for(int o=0;o<JsonWOH.size();o++)
		{
			JSONArray tmpArr = JsonWOH.getJSONArray(o);
			for(int h=0;h<tmpArr.size();h++)
			{
				model.outputLayer[o].weight[h]=tmpArr.getDouble(h);
			}
			
		}
//		System.out.println("--load model succeed!");
		return model;
	}
	
	public void update(double[] inputLayer,double[] yArr)
	{
		
		double[] tmpHiddenLayerOutput=calculateHiddenLayerOutput(inputLayer);
		double[] tmpModelOutput=new double[outputLayerSize];
		
		for(int o=0;o<outputLayerSize;o++)
		{
			outputLayer[o].updateWeight(tmpHiddenLayerOutput, yArr);
			tmpModelOutput[o]=outputLayer[o].calculateOutput(tmpHiddenLayerOutput);
		}
		for(int h=0;h<hiddenLayerSize;h++)
		{
			hiddenLayer[h].updateWeight(inputLayer, yArr,tmpHiddenLayerOutput,tmpModelOutput);
		}
	}

	class HiddenNeuron extends Neuron
	{

		public HiddenNeuron(int weightArrLength, int idx) {
			super(weightArrLength, idx);
		}

		@Override
		public void updateWeight(double[] inputLayer,double[] yArr,
				                 double[] tmpHiddenLayerOutput,
				                 double[] tmpModelOutput) 
		{
			double sigma_delta_w=0;
			for(int o=0;o<outputLayerSize;o++)
			{
				double delta_o=outputLayer[o].delta_o(yArr[o],tmpModelOutput[o]);
				double weight_oh=outputLayer[o].weight[idx];
				sigma_delta_w+=delta_o*weight_oh;
			}
			
			double delta_h=sigma_delta_w*logisticSigmod_1(tmpHiddenLayerOutput[idx]);
			for(int i=0;i<inputLayerSize;i++)
			{
				double delta_w=delta_h*inputLayer[i];
				weight[i]=weight[i]+mu*delta_w;
			}
		}
		
	}
	
	class OutputNeuron extends Neuron
	{
		public OutputNeuron(int weightArrLength, int idx) {
			super(weightArrLength, idx);
		}

		@Override
		public void updateWeight(double[] hiddenLayerOutput,double[] yArr) {
			
			double y=yArr[idx];			
			double output=calculateOutput(hiddenLayerOutput);
			double delta_o=delta_o(y,output);
			
			for(int h=0;h<weight.length;h++)
			{
				double delta_w=delta_o*hiddenLayerOutput[h];
				weight[h]=weight[h]+mu*delta_w;
			}
		}
		public double delta_o(double y,double output)
		{
			return (y-output)*logisticSigmod_1(output);
		}		
	}
}

