package applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.io.CopyUtils;

@SuppressWarnings("deprecation")
public class RecognizeNumber {
	
	String dataRootPath= "G:\\data\\NNT_binT";
	String trainDir="G:\\data\\RecognizeNumber\\train";
	String testDir="G:\\data\\RecognizeNumber\\test";
	
	int trainSizePerClass=1000;
	int testSizePerClass=500;
	Random rand=new Random();
	ReadColorTest rc = new ReadColorTest();
	
	public RecognizeNumber(){}
	
	public void addTrain() throws Exception
	{
		FileWriter trainWriter=new FileWriter("G:\\data\\RecognizeNumber\\train.txt");
		FileWriter testWriter=new FileWriter("G:\\data\\RecognizeNumber\\test.txt");
		
		File rootDir=new File(dataRootPath);
		File[] subDirs=rootDir.listFiles();
		
		new File(trainDir).mkdir();
		new File(testDir).mkdir();
		for(File subDir:subDirs)
		{	
			String className=subDir.getName();
			if(Integer.valueOf(className)>=5) continue;
			
			File classTrainDir=new File(trainDir+"\\"+className);
			File classTestDir=new File(testDir+"\\"+className);
			
//			classTrainDir.delete();
//			classTestDir.delete();
			
			classTrainDir.mkdir();
			classTestDir.mkdir();
			
			
			File[] pictures=subDir.listFiles();
			
			for(File picture:pictures)
			{
				int tmpRand=rand.nextInt(pictures.length);
				if(tmpRand<trainSizePerClass)
				{
					InputStream input=new FileInputStream(picture);
					OutputStream output=new FileOutputStream(new File(classTrainDir+"\\"+picture.getName()));
					CopyUtils.copy(input, output);
					trainWriter.write(picture.getName()+","+rc.getImagePixel(picture.getPath())+className+"\n");
				}
				else if(tmpRand>=trainSizePerClass && tmpRand<trainSizePerClass+testSizePerClass)
				{
					InputStream input=new FileInputStream(picture);
					OutputStream output=new FileOutputStream(new File(classTestDir+"\\"+picture.getName()));
					CopyUtils.copy(input, output);
					testWriter.write(picture.getName()+","+rc.getImagePixel(picture.getPath())+className+"\n");
					System.out.println(picture.getName());
				}
			}
		}
		trainWriter.close();
		testWriter.close();
		
	}

	public static void main(String[] args) throws Exception
	{
		RecognizeNumber rn=new RecognizeNumber();
		rn.addTrain();
		
	}
}
