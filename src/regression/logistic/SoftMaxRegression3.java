package regression.logistic;

import java.io.IOException;
import optimization.NewTonMethod;
import tools.DataModel;
import tools.VectorTools;

public class SoftMaxRegression3 {
	public DataModel trainData;
	public double mu=0.001;	
	public int curTrainingTimes=0,maxTrainingTimes=5;
	public double epsilon=0.999;
	public String standardizeMethod=null;
	public SoftMaxRegressionModel model;
	public int trainSize,inputDim,outputDim;
	private double[][] dX;
	private double[] d0;
	
	public static String modelSavePath="G:\\EclipseProjects\\PhoenixInJava\\data\\soft_max_regression.model";
	
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
		return Math.exp(-costFunc());
	}
	
	public double costFunc()
	{
		return -logLikelihood()/trainSize;
	}
	
	public double costEta(double eta)
	{
		double pf=0;
//		System.out.println("costEta:"+Arrays.toString(model.weightX[0])+","+eta+","+Arrays.toString(dX[0]));
//		System.out.println("costEta:"+Arrays.toString(VectorTools.addition(model.weightX[0],VectorTools.multiplication(eta,dX[0]))));
//		double[] v=VectorTools.addition(model.weightX[0],VectorTools.multiplication(eta,dX[0]));
		for(int j=0;j<trainSize;j++)
		{
			int yIdx=trainData.getYIdx(j);
			double[] input=trainData.getX(j);			
			double term1=VectorTools.innerProduct(input,model.weightX[yIdx])+model.weight0[yIdx]+eta*(VectorTools.innerProduct(input,dX[yIdx])+d0[yIdx]);
//			if(yIdx==0) System.out.println("term1:"+term1+","+(VectorTools.innerProduct(input,v)+model.weight0[0]+));
			double term2=0;
			for(int o=0;o<outputDim;o++)
			{	
				term2+=Math.exp(
						VectorTools.innerProduct(input,model.weightX[o])+model.weight0[o]
						+
						eta*(VectorTools.innerProduct(input,dX[o])+d0[o])
						);
			}
			pf+=(term1-Math.log(term2))/(-trainSize);
//			System.out.println("costEta -> "+j+","+term1+","+Math.log(term2));
		}
		return pf;
	}
	
	public double logLikelihood()
	{
		double logLikelihood=0;
		for(int i=0;i<trainSize;i++)
		{
			double[] input=trainData.getX(i);
			int yIdx=trainData.getYIdx(i);
			double[] logitArr=model.calculateLogitArr(input);
			double logLogit=Math.log(logitArr[yIdx]);
			double sumLogit=0;
			for(int o=0;o<outputDim;o++)
			{
				sumLogit+=logitArr[o];
			}
			logLikelihood+=(logLogit-Math.log(sumLogit));
//			System.out.println("logLikelihood -> "+i+","+logLogit+","+Math.log(sumLogit));
		}
		return logLikelihood;
	}

	public void train() throws IOException
	{	
		double eta=0;
		double beta=1;
		double lastGradientSquareSum=1;
		while(curTrainingTimes<maxTrainingTimes*inputDim)
		{
			if(curTrainingTimes%inputDim==0)
			{
				System.out.println("**"+curTrainingTimes+" init d");
				dX=new double[outputDim][inputDim];
				d0=new double[outputDim];
			}
			double[][] gradientWeightX=new double[outputDim-1][inputDim];
			double[] gradientWeight0=new double[outputDim-1];
			for(int j=0;j<trainSize;j++)
			{
				int yIdx=trainData.getYIdx(j);
				double[] input=trainData.getX(j);
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
						gradientWeightX[o][i]+=input[i]*((yIdx==o?1:0)-logitArr[o]/sumLogit)/(-trainSize);
					}
					gradientWeight0[o]+=((yIdx==o?1:0)-logitArr[o]/sumLogit)/(-trainSize);
				}
			}
			double gradientSquareSum=0;
			for(int o=0;o<outputDim-1;o++)
			{
				for(int i=0;i<inputDim;i++)
				{
					gradientSquareSum+=Math.pow(gradientWeightX[o][i],2);
				}
				gradientSquareSum+=Math.pow(gradientWeight0[o],2);
			}
			//求beta,更新d
			beta=gradientSquareSum/lastGradientSquareSum;
			lastGradientSquareSum=gradientSquareSum;
			System.out.println("beta:"+beta);
			for(int o=0;o<outputDim-1;o++)
			{
				for(int i=0;i<inputDim;i++)
				{
					dX[o][i]=(-1)*gradientWeightX[o][i]+beta*dX[o][i];
				}
				d0[o]=(-1)*gradientWeight0[o]+beta*d0[o];
			}
			//牛顿法求eta
			EtaByNewTon etaByNewTon=new EtaByNewTon();
			eta=etaByNewTon.solve();
			//更新weigt
			model.updateWeight(dX, d0,eta);
//			System.out.println("costFunc:"+costFunc());
			curTrainingTimes++;
			System.out.println("curTrainingTimes:"+curTrainingTimes+",avgLikehood:"+avgLikliehood());
		}
	}
	
	public static void trainModel(String trainPath,String delimiter,String varType) throws IOException
	{
		SoftMaxRegression3 smr=new SoftMaxRegression3();
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
	
	class EtaByNewTon extends NewTonMethod
	{
		public double df=0,pf=0;
		@Override
		public double primitiveFunc(double x) {
			
			df=0;
			pf=0;
			for(int j=0;j<trainSize;j++)
			{
				int yIdx=trainData.getYIdx(j);
				double[] input=trainData.getX(j);			
				double term1=VectorTools.innerProduct(input,dX[yIdx])+d0[yIdx];
				double term2=0;
				double term3=0;
				double term4=0;
				for(int o=0;o<outputDim;o++)
				{	
					double dProductInput=VectorTools.innerProduct(input,dX[o])+d0[o];
					double e=Math.exp(
							VectorTools.innerProduct(input,model.weightX[o])+model.weight0[o]
							+
							x*dProductInput
							);
					term2+=(e*dProductInput);
					term3+=e;
					term4+=(e*dProductInput*dProductInput);
				}
				pf+=(term1-term2/(term3))/(-trainSize);
				df+=((term4*term3-term2*term2)/(term3*term3))/(trainSize);
			}
			return pf;
		}

		@Override
		public double derivativeFunc(double x) {
			// TODO Auto-generated method stub
			return df;
		}		
	}

	public static void main(String[] args) throws IOException
	{
		trainModel("G:\\data\\RecognizeNumber\\train.txt", ",", "ID-X-Y");
//		trainModel("G:\\git\\JPhoenix\\data\\logistic_regression\\train.TXT", "\t", "X-Y");
//		testModel("G:\\data\\RecognizeNumber\\test.txt", ",", "ID-X-Y");
//		trainModel("G:\\EclipseProjects\\PhoenixInJava\\data\\logistic_regression\\train2.TXT", "\t", "X-Y");

	}
}


