package tools;

public class VectorTools {

	public static double innerProduct(double[] v1,double[] v2)
	{
		if(v1.length!=v2.length)
		{
			throw new IllegalArgumentException("v1,v2长度不相等，无法计算内积");
		}
		double s=0;
		for(int i=0;i<v1.length;i++)
		{
			s+=v1[i]*v2[i];
		}
		return s;
	}
	
//	public static double fixInnerProduct(double[] v1,double[] v2)
//	{
//		if(v1.length+1!=v2.length)
//		{
//			throw new IllegalArgumentException("v1长度加1不等于v2长度，无法计算内积");
//		}
//		double s=0;
//		for(int i=0;i<v1.length;i++)
//		{
//			s+=v1[i]*v2[i];
//		}
//		s+=v2[v1.length];
//		return s;
//	}
	
	public static double[] multiplication(double lambda,double[] v)
	{
		double[] v1=new double[v.length];
		for(int i=0;i<v.length;i++)
		{
			v1[i]=v[i]*lambda;
		}
		return v1;
	}
	
	public static double[] addition(double[] v1,double[] v2)
	{
		if(v1.length!=v2.length)
		{
			throw new IllegalArgumentException("v1,v2长度不相等，无法相加");
		}
		double[] v3=new double[v1.length];
		for(int i=0;i<v3.length;i++)
		{
			v3[i]=(v1[i]+v2[i]);
		}
		return v3;
	}
	
	public static void main(String[] args)
	{
		double[] v1={1,2,3};
		double[] v2={2,3,4};
		System.out.println(innerProduct(v1, v2));
	}
}
