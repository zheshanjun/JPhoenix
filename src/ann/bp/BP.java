package ann.bp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import org.json.JSONException;
import applications.ReadColorTest;
import tools.DataModel;
import tools.FReader;

public class BP {

	/*
	 * p为logisticSigmodFunc函数的陡峭性参数，p越小越接近阶跃函数
	 * epsilon为停止迭代的最大误差阈值
	 * mu为梯度下降的学习速率
	 */
	
	public String delimiter=","; //训练数据和测试数据的分隔符，文件格式为每行包含列数相等，因变量放在最后一位。
	public double p=1,epsilon=0.005;
	public int curTrainingTimes=0,maxTrainingTimes=50000,checkFreq=5000; //最大训练次数
	public Random rand=new Random(new Date().getTime());
	public static String modelSavePath="G:\\data\\model.txt";
	public BPAnnModel model;
	public String standardizeMethod=null;
	public String dataType="ID-X-Y";
	public DataModel trainData;
	
	public HashMap<String,Integer> trainCntMap=new HashMap<String,Integer>();
	
	public BP(){};
	public BP(String standardizeMethod)
	{
		if(!standardizeMethod.equals("min-max") && !standardizeMethod.equals("z-score"))
		{
			throw new IllegalArgumentException("请输入正确的标准化方法，'min-max' 或 'z-score'");
		}
		this.standardizeMethod=standardizeMethod;
	}
	
	public void setModel(BPAnnModel model)
	{
		this.model=model;
	}
	
	public void setStandardizeMethod(String standardizeMethod)
	{
		this.standardizeMethod=standardizeMethod;
	}
	
	public void setTrain(String trainPath,String varType,String delimiter) throws IOException, FileNotFoundException
	{
		trainData=new DataModel(trainPath,delimiter,varType,standardizeMethod);
		int inputLayerSize=trainData.getColNumsX();
		int outputLayerSize=trainData.getYFlagArr().length;
		int hiddenLayerSize=(int) Math.round(Math.pow(inputLayerSize*outputLayerSize, 0.5));
		String[] outputFlagArr=trainData.getYFlagArr();

		model=new BPAnnModel(inputLayerSize,hiddenLayerSize,outputLayerSize);
		model.setOutputFlag(outputFlagArr);
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
	
	public void train() throws IOException, JSONException
	{
		calculateGlobalError();
		while((curTrainingTimes++)<maxTrainingTimes)
		{
			int k=rand.nextInt(trainData.getRowNums());
			String id=trainData.getId(k);
			if(!trainCntMap.containsKey(id)) trainCntMap.put(id, 0);
			trainCntMap.put(id,trainCntMap.get(id)+1);
			double[] yArr=model.transYToArr(trainData.getY(k));
			double[] inputLayer=trainData.getX(k);
			model.update(inputLayer,yArr);
			if(curTrainingTimes%checkFreq==0)
			{
				model.save(modelSavePath);
				if(calculateGlobalError()<epsilon) break;
			}
		}
	}
	
	public double calculateSingleError(double[] inputLayer,double[] yArr,boolean standardizeTrans)
	{
		double singleError=0;
		double[] outputLayerOut=model.calculateModelOutput(inputLayer,standardizeTrans);

		for(int k=0;k<model.outputLayerSize;k++)
		{
			singleError+=0.5*Math.pow((outputLayerOut[k]-yArr[k]),2);
			
		}
		return singleError;
	}
	
	public double calculateGlobalError() throws IOException
	{
		double globalError=0;
		double maxSingleError=0;
		for(int i=0;i<trainData.getRowNums();i++)
		{
			double[] inputLayer=trainData.getX(i);
			double[] yArr=model.transYToArr(trainData.getY(i));
			
			double error=calculateSingleError(inputLayer,yArr,false);
			if(error>maxSingleError)
			{
				maxSingleError=error;
			}
			
			globalError+=error;
		}
		globalError=globalError/trainData.getRowNums();
//		System.out.println(curTrainingTimes+","+globalError);
		return globalError;
	}
	
	public void printTrain()
	{
		boolean standardizeTrans=false;
		int rightCnt=0;
		int wrongCnt=0;
		int unknownCnt=0;
		for(int i=0;i<trainData.getRowNums();i++)
		{
			double[] inputLayer=trainData.getX(i);
			String realClass=trainData.getY(i);
			String predictClass=model.getOutputClass(inputLayer,standardizeTrans);
			if(predictClass.equals("unknown"))
			{
				unknownCnt++;
			}
			else if(!realClass.equals(predictClass))
			{
				wrongCnt++;
//				System.out.println(id+" -> "+realClass+","+predictClass+" "+(realClass.equals(predictClass))+", error:"+error);
			}
			else
			{
				rightCnt++;
			}
		}
		System.out.println("train -> rightCnt:"+rightCnt+",wrongCnt:"+wrongCnt+",unknownCnt:"+unknownCnt);
	}
	
	public void printTest(String testPath,String delimiter,String varType) throws IOException
	{
		FReader testReader=new FReader(testPath);
		int xStartIdx=varType.equals("ID-X-Y")?1:0;
		int wrongCnt=0;
		int unknownCnt=0;
		int rightCnt=0;
		while(true)
		{
			String line=testReader.nextLine();
			if(line==null) break;
			String[] vals=line.split(delimiter);
			double[] inputLayer=new double[model.inputLayerSize];
			String realClass=vals[vals.length-1];
			for(int i=0;i<model.inputLayerSize;i++)
			{
				inputLayer[i]=Double.valueOf(vals[i+xStartIdx]);
			}
			String predictClass=model.getOutputClass(inputLayer,true);
			if(predictClass.equals("unknown"))
			{
				unknownCnt++;
			}
			else if(!realClass.equals(predictClass))
			{
				wrongCnt++;
			}
			else
			{
				rightCnt++;
			}
		}
		System.out.println("test -> rightCnt:"+rightCnt+",wrongCnt:"+wrongCnt+",unknownCnt:"+unknownCnt);
	}

	public static void trainModel(String trainingPath,String varType,String delimiter,String standardizeMethod) throws IOException, JSONException
	{
		BP bp=standardizeMethod==null?new BP():new BP(standardizeMethod);
		bp.setTrain(trainingPath,varType,delimiter);
		bp.setStandardizeMethod(standardizeMethod);
//		BPAnnModel model = BPAnnModel.load(modelSavePath);
//		bp.setModel(model);
		bp.train();
		bp.printTrain();
	}
	
	public static void testModel(String testPath,String delimiter,String varType) throws IOException, JSONException
	{
		BPAnnModel model = BPAnnModel.load(modelSavePath);
		BP bp=new BP();
		bp.setModel(model);
		bp.printTest(testPath,delimiter,varType);
	}
	
	public static void recognize(String picturePath) throws IOException, JSONException
	{
		BPAnnModel model = BPAnnModel.load(modelSavePath);
		double[] inputLayer=ReadColorTest.getPictureInput(picturePath);
		System.out.println(model.getOutputClass(inputLayer,true));
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, JSONException
	{
		trainModel("G:\\data\\RecognizeNumber\\train.txt","ID-X-Y",",",null);
		testModel("G:\\data\\RecognizeNumber\\test.txt",",","ID-X-Y");
		
//		testModel("G:\\data\\RecognizeNumber\\train.txt");
	}
}
