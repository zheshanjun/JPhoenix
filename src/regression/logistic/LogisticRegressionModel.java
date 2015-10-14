package regression.logistic;
import java.io.FileWriter;
import java.io.IOException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import tools.ClassificationModel;
import tools.FReader;

public class LogisticRegressionModel extends ClassificationModel{

	public double[] weight;
	public double b;
		
	public void initWeight(int weightSize)
	{
		weight=new double[weightSize];
		b=0;
	}
	
	public void setWeight(JSONArray jsonWeight,double b)
	{
		int size=jsonWeight.size();
		this.weight=new double[size];
		for(int i=0;i<size;i++)
		{
			this.weight[i]=jsonWeight.getDoubleValue(i);
		}
		this.b=b;
	}
	
	public void updateWeight(double[] deltaWeight,double delta_b)
	{
		for(int j=0;j<weight.length;j++)
		{
			weight[j]+=deltaWeight[j];
		}
		b+=delta_b;
	}
	
	public double logLogit(double[] input)
	{
		double logLogit=b;
		for(int i=0;i<weight.length;i++)
		{
			logLogit+=input[i]*weight[i];
		}
		return logLogit;
	}
	
	public double logit(double[] input)
	{
		double logLogit=logLogit(input);
		return Math.exp(logLogit);
	}
	
	public String getOutputClass(double[] input,boolean standardizeTrans) throws IllegalArgumentException
	{	
		if(standardizeTrans==true) standardizeInput(input);
		double logit=logit(input);
		double p1=logit/(1+logit);
		double p0=1/(1+logit);
//		System.out.println(logit+","+p0+","+p1);
		int outputIdx=p1>p0?1:0;
		return outputFlagArr[outputIdx];
	}
	
	@Override
	public void save(String modelSavePath) throws IOException {
		System.out.println("--saving model...");
		FileWriter writer=new FileWriter(modelSavePath);
		JSONObject json=new JSONObject();
		json.put("weight", weight);
		json.put("b",b);
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

	public static LogisticRegressionModel load(String modelSavePath) throws IOException {

//		System.out.println("--loading model...");	
		FReader reader=new FReader(modelSavePath);
		JSONObject json=JSON.parseObject(reader.nextLine());
		double b=json.getDoubleValue("b");
		JSONArray jsonWeight = json.getJSONArray("weight");
		JSONArray jsonOutputFlag = json.getJSONArray("outputFlagArr");
		
		LogisticRegressionModel model=new LogisticRegressionModel();
		model.setWeight(jsonWeight, b);
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
//		System.out.println("--load model succeed!");
		return model;
	}
}
