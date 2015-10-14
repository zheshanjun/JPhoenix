package applications;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ReadColorTest {
	/**
	 * ��ȡһ��ͼƬ��RGBֵ
	 * 
	 * @throws Exception
	 */
	public String getImagePixel(String image) throws Exception {
		int[] rgb = new int[3];
		File file = new File(image);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		System.out.println("width=" + width + ",height=" + height + ".");
		System.out.println("minx=" + minx + ",miniy=" + miny + ".");
		StringBuilder outData=new StringBuilder();
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j); // �������д��뽫һ������ת��ΪRGB����
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				
				if(rgb[0]+rgb[1]+rgb[2]>255)
				{
					outData.append("1,");
				}
				else
				{
					outData.append("0,");
				}
//				System.out.println("i=" + i + ",j=" + j + ":(" + rgb[0] + ","+ rgb[1] + "," + rgb[2] + ")");
			}
		}
//		outData.setLength(outData.length()-1);
		return outData.toString();
	}

	/**
	 * ������Ļɫ��ֵ
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws AWTException
	 */
//	public int getScreenPixel(int x, int y) throws AWTException { // ��������ֵΪ��ɫ��RGBֵ��
//		Robot rb = null; // java.awt.image���е��࣬��������ץȡ��Ļ����������
//		rb = new Robot();
//		Toolkit tk = Toolkit.getDefaultToolkit(); // ��ȡȱʡ���߰�
//		Dimension di = tk.getScreenSize(); // ��Ļ�ߴ���
//		System.out.println(di.width);
//		System.out.println(di.height);
//		Rectangle rec = new Rectangle(0, 0, di.width, di.height);
//		BufferedImage bi = rb.createScreenCapture(rec);
//		int pixelColor = bi.getRGB(x, y);
//
//		return 16777216 + pixelColor; // pixelColor��ֵΪ��������ʵ���ó���������ɫ���ֵ����ʵ����ɫֵ��
//	}

	/**
	 * @param args
	 */
	
	
	public static double[] getPictureInput(String picturePath)
	{
		int[] rgb = new int[3];
		File file = new File(picturePath);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		
		double[] resArr = new double[width*height];
		int idx=0;
//		System.out.println("width=" + width + ",height=" + height + ".");
//		System.out.println("minx=" + minx + ",miniy=" + miny + ".");
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j); // �������д��뽫һ������ת��ΪRGB����
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				
				if(rgb[0]+rgb[1]+rgb[2]>0)
				{
					resArr[idx++]=1;
				}
				else
				{
					resArr[idx++]=0;
				}
//				System.out.println("i=" + i + ",j=" + j + ":(" + rgb[0] + ","+ rgb[1] + "," + rgb[2] + ")");
			}
		}
//		outData.setLength(outData.length()-1);
		return resArr;
	}
	
	public static void main(String[] args) throws Exception {
//		int x = 0;
//		ReadColorTest rc = new ReadColorTest();
//		x = rc.getScreenPixel(100, 345);
//		System.out.println(x + " - ");
//		System.out.println(28*28);
//		System.out.println(rc.getImagePixel("G:\\data\\RecognizeNumber\\train\\0\\0_7689.jpg"));
	}

}
