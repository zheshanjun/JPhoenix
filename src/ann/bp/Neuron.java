package ann.bp;

abstract class Neuron {

	public double p=1; //p为logisticSigmodFunc函数的陡峭性参数，p越小越接近阶跃函数
	public double theta=0.2; //theta 为神经元兴奋度阈值
	public double[] weight;
	public double mu=0.1; //mu为梯度下降的学习速率
	public int idx;
	
	public Neuron(int weightArrLength,int idx)
	{
		this.idx=idx;
		weight=new double[weightArrLength];
		for(int i=0;i<weight.length;i++)
		{
			weight[i]=2*Math.random()-1;
		}
	}

	public void updateWeight(double[] inputLayer, double[] yArr){}
	public void updateWeight(double[] inputLayer, double[] yArr,
			                 double[] tmpHiddenLayerOutput, 
			                 double[] tmpModelOutput){}
	
	public double calculateActivation(double[] layerInput)
	{
		double activation=0;
		for(int i=0;i<weight.length;i++)
		{
			activation+=weight[i]*layerInput[i];
		}
		return activation;
	}
	
	public double calculateOutput(double[] layerInput)
	{
		double activation=calculateActivation(layerInput);
		return logisticSigmod(activation-theta,p);
	}
	
	public double logisticSigmod(double fixedActivation,double p)
	{
		return 1/(1+Math.exp(-fixedActivation/p));
		
	}
	
	public double logisticSigmod_1(double output)
	{
		
		return (1/p)*output*(1-output);
	}
	
	public static void main(String[] args)
	{
//		Neuron neuron = new Neuron(new double[]{1,2,3,4,5},new double[]{0.1,0.2,0.3,0.4,0.5});
//		System.out.println(neuron.logisticSigmod_1());
//		System.out.println(neuron.logisticSigmod_2());
	}

	

	
}
