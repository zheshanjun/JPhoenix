package regression.logistic;

import java.io.IOException;
import tools.DataModel;

public class SoftMaxRegression2 {

	public DataModel trainData;
	public double mu=0.001;	
	public int curTrainingTimes=0,checkFreq=25,maxTrainingTimes=1000;
	public double epsilon=0.99;
	public String standardizeMethod=null;
	public SoftMaxRegressionModel model;
	public int trainSize,inputDim,outputDim;
	
	public static String modelSavePath="G:\\git\\JPhoenix\\data\\soft_max_regression.model";
	
	public void setTrain(String trainPath,String varType,String delimiter) throws IOException
	{
		trainData=new DataModel(trainPath,delimiter,varType,standardizeMethod);
		trainSize=trainData.getRowNums();
		inputDim=trainData.getColNumsX();
		outputDim=trainData.getYFlagArr().length;
		model=new SoftMaxRegressionModel();
		model.initWeight(inputDim,outputDim);
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
	
	public double avgLikliehood()
	{
		
		return Math.exp(costFunc());
	}
	
	public double costFunc()
	{
		
		return logLikelihood()/trainSize;
	}
	
	public double logLikelihood()
	{
		double logLikelihood=0;
		for(int i=0;i<trainSize;i++)
		{
			double[] input=trainData.getX(i);
			int yIdx=trainData.getYIdx(i);
			for(int j=0;j<model.outputDim;j++)
			{
				double logLogit=model.calculateLogLogit(input,j);
				double[] logitArr=model.calculateLogitArr(input);
				double sumLogit=0;
				for(int o=0;o<outputDim;o++)
				{
					sumLogit+=logitArr[o];
				}
				
				logLikelihood+=((yIdx==j?1:0)*(logLogit-Math.log(sumLogit)));
			}
		}
		return logLikelihood;
	}
	
	public void train() throws IOException
	{	
		long startTs=System.currentTimeMillis();
//		double lastAvgLikelihood=0;
		while((curTrainingTimes++)<maxTrainingTimes)
		{
			double[][] deltaWeightX=new double[outputDim-1][inputDim+1];
			double[] deltaWeight0=new double[outputDim-1];
			for(int n=0;n<trainSize;n++)
			{
				int yIdx=trainData.getYIdx(n);
				double[] input=trainData.getX(n);
				double[] logitArr=model.calculateLogitArr(input);
				double sumLogit=0;
				for(int o=0;o<outputDim;o++)
				{
					sumLogit+=logitArr[o];
				}
				for(int o=0;o<outputDim-1;o++)
				{
					for(int i=0;i<inputDim;i++)
					{
						deltaWeightX[o][i]+=input[i]*((yIdx==o?1:0)-logitArr[o]/sumLogit);
					}
					deltaWeight0[o]+=((yIdx==o?1:0)-logitArr[o]/sumLogit);
				}
			}
//			System.out.println(Arrays.toString(deltaWeightX[0]));
//			System.out.println(maxNorm(deltaWeight)/trainSize);
			model.updateWeight(deltaWeightX,deltaWeight0,mu);
			if(curTrainingTimes%checkFreq==0)
			{
				model.save(modelSavePath);
				double avgLikelihood=avgLikliehood();
				System.out.println("curTrainingTimes:"+curTrainingTimes+",avgLikehood:"+avgLikliehood()+",mu:"+mu);
//				lastAvgLikelihood=avgLikelihood;
				if(avgLikelihood>=epsilon) break;
			}
//			break;
		}
		long stopTs=System.currentTimeMillis();
		System.out.println(stopTs-startTs);
	}
	
	public double maxNorm(double[][] matrix)
	{
		double maxNorm=0;
		for(int i=0;i<matrix.length;i++)
		{
			double norm=norm(matrix[i]);
			maxNorm=maxNorm>norm?maxNorm:norm;
		}
		return maxNorm;
	}
	public double norm(double[] vector)
	{
		double sumSeq=0;
		for(int j=0;j<vector.length;j++)
		{
			sumSeq+=Math.pow(vector[j],2);
		}
		return Math.pow(sumSeq,0.5);
	}
	
	public static void trainModel(String trainPath,String delimiter,String varType) throws IOException
	{
		SoftMaxRegression2 smr=new SoftMaxRegression2();
		smr.setTrain(trainPath, varType,delimiter);
		smr.train();
		int t=0,f=0;
		for(int i=0;i<smr.trainSize;i++)
		{
			String y=smr.trainData.getY(i);
			double[] input=smr.trainData.getX(i);
			String output = smr.model.getOutputClass(input, false);
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
		SoftMaxRegressionModel model=SoftMaxRegressionModel.load(modelSavePath);
		DataModel testData = new DataModel(testPath,delimiter,varType,null);
		int t=0,f=0;
		for(int i=0;i<testData.getRowNums();i++)
		{
			double[] input=testData.getX(i);
			String y=testData.getY(i);
			String output=model.getOutputClass(input);
			if(y.equals(output)) t++;
			else f++;
//			System.out.println(y+": "+output);
		}
		System.out.println("t:"+t+",f:"+f+","+((float)t/(t+f)));
	}
	
	public static void main(String[] args) throws IOException
	{
		long t1=System.currentTimeMillis();
		trainModel("G:\\data\\RecognizeNumber\\train.txt", ",", "ID-X-Y");
		long t2=System.currentTimeMillis();
		System.out.println(t2-t1);
		testModel("G:\\data\\RecognizeNumber\\test.txt", ",", "ID-X-Y");
		
//		trainModel("G:\\EclipseProjects\\PhoenixInJava\\data\\logistic_regression\\train2.TXT", "\t", "X-Y");

	}
}
