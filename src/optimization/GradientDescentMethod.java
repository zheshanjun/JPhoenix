package optimization;

public abstract class GradientDescentMethod {

	private double epsilon=0.00001;
	private double maxSteps=10000;
	
	public GradientDescentMethod(){}
	public GradientDescentMethod(double epsilon,int maxSteps)
	{
		this.epsilon=epsilon;
		this.maxSteps=maxSteps;
	}
	
	public abstract double primitiveFunc(double x);
	public abstract double derivativeFunc(double x);
	
	public double solve()
	{
		double x=Math.random();
		int cnt=0;
		double mu=0.001;
		while(true)
		{
			double pf=primitiveFunc(x);
			double df=derivativeFunc(x);
			System.out.printf("x:%f,pf:%f,df:%f\n",x,pf,df);
			if(Math.abs(df)<epsilon || (++cnt)>maxSteps) break;
			x-=mu*df;
		}
		return x;
	}
	
	public static void main(String[] args)
	{
		TestFunc2 f=new TestFunc2();
		System.out.println("------\n"+f.solve());
	}
	
}
class TestFunc2 extends GradientDescentMethod
{
	@Override
	public double primitiveFunc(double x) {
		// TODO Auto-generated method stub
		return Math.exp(x)-10000*x;
//		return x*x-9;
	}

	@Override
	public double derivativeFunc(double x) {
		// TODO Auto-generated method stub
		return Math.exp(x)-10000;
	}
}
