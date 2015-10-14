package optimization;

public abstract class NewTonMethod {

	private double epsilon=0.0000001;
	protected double maxSteps=100;
	public NewTonMethod() {}
	public NewTonMethod(double epsilon,int maxSteps)
	{
		this.epsilon=epsilon;
		this.maxSteps=maxSteps;
	}
	
	public abstract double primitiveFunc(double x);
	public abstract double derivativeFunc(double x);
	
	public double solve()
	{
		
		int newtonStep=0;
		
		double x0=0; //当前迭代点
		double pf0=primitiveFunc(x0); //当前迭代点函数值
		double df0=derivativeFunc(x0); //当前迭代点一阶导数值
		while(df0==0)
		{
			x0+=Math.random();
			pf0=primitiveFunc(x0);
			df0=derivativeFunc(x0);                                            
		}
		//二分法求近似解
		{
			int binaryStep=0;
			double x1=x0-pf0/df0; //下一个迭代点
			double pf1=primitiveFunc(x1); //下一个迭代点函数值
			double x2=x1; //x1更新前记录x1的值，用于回溯上一次x1的状态

			
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
		//牛顿法求精确解
		while((++newtonStep)<=maxSteps)
		{
			if(Math.abs(pf0)<epsilon) break;
			double x1=x0-pf0/df0;
			double pf1=primitiveFunc(x1);
			double df1=derivativeFunc(x1);
//			System.out.println("x0:"+x0+",x1:"+x1);
//			System.out.println("pf0:"+pf0+",pf1:"+pf1);
			x0=x1;
			pf0=pf1;
			df0=df1;
		}
		System.out.println("newtonStep:"+newtonStep);
		return x0;
	}
	
	public static void main(String[] args)
	{
		TestFunc f=new TestFunc();
		System.out.println(f.solve()+","+f.primitiveFunc(f.solve()));
	}
}
class TestFunc extends NewTonMethod
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
