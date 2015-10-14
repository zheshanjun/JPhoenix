package regression.logistic;

import java.io.IOException;
import tools.DataModel;

public class LogisticRegression {

	private static DataModel trainData;
	private double mu=0.001;
	public int curTrainingTimes=0,maxTrainingTimes=200;
	public double epsilon=0.00001;
	public int trainSize;
	public String standardizeMethod=null;
	public LogisticRegressionModel model;
	public static String modelSavePath="G:\\git\\JPhoenix\\data\\logistic_regression\\model.txt";
	
	public void setTrain(String trainPath,String varType,String delimiter) throws IOException
	{
		trainData=new DataModel(trainPath,delimiter,varType,standardizeMethod);
		trainSize=trainData.getRowNums();
		int weigthSize=trainData.getColNumsX();
		model=new LogisticRegressionModel();
		model.initWeight(weigthSize);
		model.setOutputFlag(trainData.getYFlagArr());
		model.setStandardizeMethod(standardizeMethod);
		
		if(standardizeMethod==null) return;
		else if(standardizeMethod.equals("min-max"))
		{
			model.setMinMax(trainData.getMinArr(),trainData.getMaxArr());
		}
		else if(standardizeMethod.equals("z-score"))
		{
			model.setMeanStd(trainData.getMeanArr(), trainData.getStdArr());
		}
	}
	
	public double avgLikelihood()
	{
		return Math.exp(logLikelihood()/trainSize);
	}
	
	public double costFunc()
	{
		return -logLikelihood();
	}
	
	public double logLikelihood()
	{
		double logLikelihood=0;
		for(int i=0;i<trainData.getRowNums();i++)
		{
			double[] input=trainData.getX(i);
			int y=trainData.getYIdx(i);
			double logLogit=model.logLogit(input);
			logLikelihood+=(y*logLogit-Math.log(1+Math.exp(logLogit)));
		}
		return logLikelihood;
	}
	
 	public void train() throws IOException
	{
		int trainSize=trainData.getRowNums();
		int weightSize=trainData.getColNumsX();
		double lastAvgLikelihood=Integer.MIN_VALUE;
		
		while((curTrainingTimes++)<maxTrainingTimes)
		{
			double[] deltaWeight=new double[weightSize];
			double delta_b=0;
			
			for(int i=0;i<trainSize;i++)
			{
				int y=trainData.getYIdx(i);
				double[] input=trainData.getX(i);
				double logit=model.logit(input);
				double pi=logit/(1+logit);
				for(int j=0;j<weightSize;j++)
				{
					double x_ij=input[j];
					deltaWeight[j]+=(x_ij*(y-pi)*mu);
				}			
				delta_b+=(y-pi)*mu;
			}
			model.updateWeight(deltaWeight,delta_b);
			model.save(modelSavePath);
			double curAvgLikelihood=avgLikelihood();
			System.out.println("curTrainingTimes:"+curTrainingTimes+", avgLikelihood:"+curAvgLikelihood);
			if(Math.abs(curAvgLikelihood-lastAvgLikelihood)<epsilon)
			{
				break;
			}
			lastAvgLikelihood=curAvgLikelihood;
		}	
	}
	
	public static void trainModel(String trainPath,String delimiter,String varType) throws IOException
	{
		LogisticRegression lr=new LogisticRegression();
		lr.setTrain(trainPath, varType,delimiter);
		lr.train();

		int trainSize=trainData.getRowNums();
		int t=0,f=0;
		for(int i=0;i<trainSize;i++)
		{
			String y=trainData.getY(i);
			double[] input=trainData.getX(i);
			String output = lr.model.getOutputClass(input, false);
			if(y.equals(output)) t++;
			else 
			{
				f++;
//				System.out.println(i+"-> "+y+","+output+" logit:"+model.logit(input, false));
			}
		}
		System.out.println("t:"+t+",f:"+f+","+((float)t/(t+f)));
	}
	
	public static void testModel(String testPath,String delimiter,String varType) throws IOException
	{
		LogisticRegressionModel model=LogisticRegressionModel.load(modelSavePath);
		DataModel testData = new DataModel(testPath,delimiter,varType,null);
		for(int i=0;i<testData.getRowNums();i++)
		{
			double[] input=testData.getX(i);
			String y=testData.getY(i);
			String output=model.getOutputClass(input);
			System.out.println(y+": "+output);
			
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		trainModel("G:\\git\\JPhoenix\\data\\logistic_regression\\train.TXT", "\t", "X-Y");
//		testModel("G:\\EclipseProjects\\PhoenixInJava\\data\\logistic_regression\\train.TXT", "\t", "X-Y");
	}
}
