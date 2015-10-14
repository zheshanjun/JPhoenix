package optimization;

public abstract class SecantMethod {

//	public double x_0=Math.random(),x_1=Math.random();
	public double epsilon=0.0000001;
	public double maxIterTimes=100;
	protected double maxSteps=500;
	
	public abstract double primitiveFunc(double x);
	public abstract double derivativeFunc(double x);
	
	public double solve()
	{
		int secantStep=0;
		double x0=0; //当前迭代点
		double pf0=primitiveFunc(x0); //当前迭代点函数值
		double df0=derivativeFunc(x0); //当前迭代点一阶导数值
		
		double x1=x0-pf0/df0; //下一个迭代点
		double pf1=primitiveFunc(x1); //下一个迭代点函数值
		double x2=x1; //x1更新前记录x1的值，用于回溯上一次x1的状态
		
		while(df0==0)
		{
			x0+=Math.random();
			pf0=primitiveFunc(x0);
			df0=derivativeFunc(x0);                                            
		}
		//二分法求近似解
		{
			int binaryStep=0;
			while((++binaryStep)<maxSteps)
			{
				if(Double.isFinite(pf1) && Math.signum(pf1)*Math.signum(pf0)>=0)
				{
					x0=x2;
					pf0=primitiveFunc(x0);
					df0=derivativeFunc(x0);
					break;
				}
				else
				{
					x2=x1;
					x1=(x1+x0)/2;
					pf1=primitiveFunc(x1);
				}
			}
		}
		while((++secantStep)<=maxSteps)
		{
			x2=x1-pf1*(x1-x0)/(pf1-pf0);
			double pf2=primitiveFunc(x2);
//			System.out.println("secantStep:"+secantStep+",pf2:"+pf2);
			if(Math.abs(pf2)<epsilon) break;
			x0=x1;
			pf0=pf1;
			x1=x2;
			pf1=pf2;
			
		}
		return x2;
	}
	
	public static void main(String[] args)
	{
		TestFunc3 f=new TestFunc3();
		System.out.println(f.solve()+","+f.primitiveFunc(f.solve()));
	}
}

class TestFunc3 extends SecantMethod
{

	@Override
	public double primitiveFunc(double x) {
		// TODO Auto-generated method stub
		return Math.exp(x)-100000;
	}

	@Override
	public double derivativeFunc(double x) {
		// TODO Auto-generated method stub
		return Math.exp(x);
	}
	
}
