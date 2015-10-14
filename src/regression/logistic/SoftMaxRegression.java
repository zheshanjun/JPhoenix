package regression.logistic;

import java.io.IOException;
import optimization.NewTonMethod;
import tools.DataModel;
import tools.VectorTools;

public class SoftMaxRegression {
	public DataModel trainData;
	public double mu=0.1;	
	public int curTrainingTimes=0,maxTrainingTimes=50;
	public int maxCGRound=3;
	public double epsilon=0.999;
	double lambda=1;
	
	public String standardizeMethod=null;
	public SoftMaxRegressionModel model;
	public int trainSize,inputDim,outputDim;
	private double[][] dX;
	private double[] d0;
	
	public static String modelSavePath="G:\\git\\JPhoenix\\data\\soft_max_regression.model";
	
	public void setTrain(String trainPath,String varType,String delimiter) throws IOException
	{
		trainData=new DataModel(trainPath,delimiter,varType,standardizeMethod);
		trainSize=trainData.getRowNums();
		inputDim=trainData.getColNumsX();
		outputDim=trainData.getYFlagArr().length;
		lambda=lambda/trainSize;
//		lambda=lambda/((outputDim-1)*(inputDim+1));
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
		return Math.exp(logLikelihood()/trainSize);
	}
	
	public double costFunc()
	{
		double regularizationTerm=0; //正则项
		for(int o=0;o<outputDim-1;o++)
		{
			for(int i=0;i<inputDim;i++)
			{
				regularizationTerm+=Math.pow(model.weightX[o][i],2);
			}
			regularizationTerm+=Math.pow(model.weight0[o],2);
		}
		return -logLikelihood()/trainSize+lambda*regularizationTerm/2;
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
		double lastAvgLikelihood=0;
		
		end:for(int outLoop=0;outLoop<maxCGRound;outLoop++)
		{
			/*
			 * 因为参数为维度为:(inputDim+1)*(outputDim-1)，
			 * 为了确保更新方向的共轭性，每(inputDim+1)*(outputDim-1)次更新后将d重置为gradientWeight
			 */
			double lastGradientSquareSum=1;
			dX=new double[outputDim][inputDim];
			d0=new double[outputDim];
			/*
			 * 按照共轭梯度d的方向，以最佳步长eta更新权重参数。
			 * eta通过牛顿法计算
			 */
			for(int inLoop=0;inLoop<(inputDim+1)*(outputDim-1);inLoop++)
			{
				double[][] gradientWeightX=new double[outputDim-1][inputDim];
				double[] gradientWeight0=new double[outputDim-1];
				for(int o=0;o<outputDim-1;o++)
				{
					for(int i=0;i<inputDim;i++)
					{
						gradientWeightX[o][i]=lambda*model.weightX[o][i];
					}
					gradientWeight0[o]=lambda*model.weight0[o];
				}
				
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
				//求beta,根据beta值更新d，更新后的d与原d共轭
				beta=gradientSquareSum/lastGradientSquareSum;
				lastGradientSquareSum=gradientSquareSum;
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
				
				curTrainingTimes++;
				
				double avgLikelihood=avgLikliehood();
				if(Math.abs(avgLikelihood-lastAvgLikelihood)<0.001) 
				{
					model.save(modelSavePath);
					break end;
				}
				lastAvgLikelihood=avgLikelihood;
				System.out.println("curTrainingTimes:"+curTrainingTimes+",avgLikehood:"+avgLikliehood());
			}
//			model.save(modelSavePath);
		}
	}
	
	public static void trainModel(String trainPath,String delimiter,String varType) throws IOException
	{
		SoftMaxRegression smr=new SoftMaxRegression();
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
			
			pf=0;
			df=0;
			
			for(int o=0;o<outputDim-1;o++)
			{
				for(int i=0;i<inputDim;i++)
				{
					pf+=lambda*(model.weightX[o][i]+x*dX[o][i])*dX[o][i];
					df+=lambda*Math.pow(dX[o][i],2);
				}
				pf+=lambda*(model.weight0[o]+x*d0[o])*d0[o];
				df+=lambda*Math.pow(d0[o],2);
			}
			
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
//		long t1=System.currentTimeMillis();
//		trainModel("G:\\data\\RecognizeNumber\\train.txt", ",", "ID-X-Y");
//		long t2=System.currentTimeMillis();
//		System.out.println(t2-t1);
		trainModel("G:\\git\\JPhoenix\\data\\logistic_regression\\train.TXT", "\t", "X-Y");
//		testModel("G:\\data\\RecognizeNumber\\test.txt", ",", "ID-X-Y");
//		trainModel("G:\\EclipseProjects\\PhoenixInJava\\data\\logistic_regression\\train2.TXT", "\t", "X-Y");

	}
}


