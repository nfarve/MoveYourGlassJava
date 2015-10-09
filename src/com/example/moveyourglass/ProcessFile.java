package com.example.moveyourglass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;




public class ProcessFile extends MoveService{
	int BUFFERSIZE = 256;
	ArrayList<Float> xdata = new ArrayList<Float>(BUFFERSIZE); 
	ArrayList<Float> ydata = new ArrayList<Float>(BUFFERSIZE); 
	ArrayList<Float> zdata = new ArrayList<Float>(BUFFERSIZE); 
	double[] walking1 = new double[] {0.71760,8.97209,2.94596,0.86400,1.66990,3.17197,248.11445,39.11080,242.30703,2424.46988};
	double[] sitting1 = new double[] {0.93828,9.90025,-0.32120,0.06570,0.05016,0.06201,10.65594,1.00435,-35.17400,2547.14928};
	double[] walking2 = new double[] {0.01108,9.35039,-0.16112,1.10064,1.95360,3.39216,20644.50728,45.80241,-4685.32396,2394.05715};
	double[] sitting2 = new double [] {0.69886,9.88666,-0.43119,0.42753,0.37953,0.18998,67.31226,2.38073,-68.38347,2539.70147};
	double[] walking3 = new double[] {0.65009,9.33718,0.90729,0.99683,1.87399,3.33119,310.80111,42.70806,841.00144,2407.33469};
	double[] sitting3 = new double[] {0.50102,9.72886,1.45615,0.23112,0.11052,1.45968,59.05249,2.10842,218.14146,2521.59420};
	double[] walking4 = new double[] {0.47020,9.47392,-1.31536,1.00208,2.23090,2.67948,409.63215,52.20910,-378.12479,2451.54514};
	double[] sitting4 = new double[] {0.56718,9.70825,2.17677,0.04183,0.03938,0.05513,15.08742,0.84789,5.52148,2551.15426};
	double[] walking5 = new double[] {0.19245,9.53532,-1.59836,0.83510,2.41962,2.10746,858.92723,53.86827,-259.20535,2475.58779};
	double[] sitting5 = new double[] {0.46571,9.78828,-0.58311,0.24765,0.12818,1.50398,67.07511,2.25967,-537.83895,2513.07102};
	double[] walking6 = new double[] {0.04172,9.52583,-1.93673,0.92687,2.18481,1.81598,4387.94041,50.60138,-182.07239,2488.52728};
	double[] sitting6 = new double [] {0.54775,9.81802,-1.36287,0.04124,0.04702,0.04502,15.34000,0.89618,-6.95850,2541.38538};
	double[] walking7 = new double[] {-0.56812,9.77784,-1.84455,1.08411,1.98415,1.47821,-372.24783,40.82104,-145.39044,2551.42614};
	double[] sitting7 = new double [] {0.69616,9.79574,-1.39312,0.18123,0.09544,0.21779,38.29112,1.54300,-24.64398,2539.20448};
	double[] walking8 = new double[] {0.05872,9.65085,-1.07522,1.07128,2.16917,2.42432,3757.76497,46.52499,-462.75137,2485.94904};
	double[] sitting8 = new double [] {0.82984,9.82559,-1.06736,0.25053,0.05450,0.13160,52.50512,1.04506,-22.01497,2539.05146};
	double[] walking9 = new double[] {0.25338,9.76042,-0.93561,1.12430,2.55002,2.03035,849.10044,51.94127,-422.72355,2510.95875};
	double[] sitting9 = new double [] {0.75558,9.82426,-0.99088,0.24940,0.13526,0.58306,55.72249,2.47940,-119.68124,2535.16065};
	double[] walking10 = new double[] {0.58175,9.65097,0.96976,0.99964,2.26549,2.69583,360.15163,51.11638,623.29532,2487.55109};
	double[] sitting10 = new double [] {0.66459,9.75989,-1.61995,0.04187,0.04799,0.05300,13.20199,0.99555,-6.74755,2538.42238};
	double[] walking11 = new double[] {1.05764,9.23005,2.83129,0.99421,1.99839,2.19024,198.01358,46.36323,154.22039,2486.34704};
	double[] sitting11 = new double[] {0.83690,9.89309,-0.14397,0.36023,0.11778,0.23600,52.54304,1.69906,-280.33935,2541.94503};
	

	List<double[]> TrainingSet = Arrays.asList(walking1, sitting1, walking2, sitting2, walking3, sitting3, walking4,
			sitting4, walking5, sitting5, walking6, sitting6, walking7, sitting7, walking8, sitting8, walking9, sitting9, 
			walking10, sitting10, walking11, sitting11);
	
	public String processData (float data[]){ 
		xdata.add(0, data[0]);
		ydata.add(0, data[1]);
		zdata.add(0, data[2]);
		if (xdata.size()==BUFFERSIZE){
			float xsum = sumArrayList(xdata);
			float ysum = sumArrayList(ydata);
			float zsum = sumArrayList(zdata);
			
			float xaverage = xsum/BUFFERSIZE;
			float yaverage = ysum/BUFFERSIZE;
			float zaverage = zsum/BUFFERSIZE;
			
			float xstd = standardDeviation(xaverage, xdata);
			float ystd = standardDeviation(yaverage, ydata);
			float zstd = standardDeviation(zaverage, zdata);
			
			float xaad = absoluteDifference(xaverage, xdata);
			float yaad = absoluteDifference(yaverage, ydata);
			float zaad = absoluteDifference(zaverage, zdata);
			
			float ara = (float) Math.sqrt(Math.pow(sumArrayList(xdata),2) + Math.pow(sumArrayList(ydata),2) + Math.pow(sumArrayList(zdata),2));
			
			double[] newData = new double[]{xaverage,yaverage,zaverage,xstd,ystd,zstd,xaad,yaad,zaad,ara};
			List<Double> distances = new ArrayList<Double>();
			for (int i = 0; i < TrainingSet.size(); i++)
			{
				distances.add(calculateDistance(TrainingSet.get(i), newData));
				
			}
			
			int shortestDistance = distances.indexOf(Collections.min(distances)); 
			xdata.remove(xdata.size() - 1);
			ydata.remove(ydata.size() - 1);
			zdata.remove(zdata.size() - 1);
			if (shortestDistance%2 ==0){
				//Log.i("process", String.valueOf(shortestDistance));
				return "walking";
			}
			else{
				return "sitting";
			}
		}
		else{
			 return "nothing";
		}
	}
	
	private float sumArrayList(ArrayList<Float> x){
		float sum = 0;
		for (Float d:x)
			sum+=d;
		return sum;
	}
	
	private float standardDeviation (float average, ArrayList<Float> x){
		float variance = 0;
		for (int i = 0; i<BUFFERSIZE; i++){
			variance += Math.pow((x.get(i)-average), 2)/BUFFERSIZE;
		}
		return (float) Math.sqrt(variance);
	}
	
	private float absoluteDifference(float average, ArrayList<Float> x){
		float sum = 0; 
		for (int i=0; i<BUFFERSIZE; i++){
			sum = sum + Math.abs(x.get(i)-average);
		}
		return sum/average;
	}
	
	private double calculateDistance(double[]x, double[]y){
		int count = 0;
	    double distance = 0.0;
	    double sum = 0.0;
	    if(x.length != y.length)
	    {
	        throw new IllegalArgumentException("the number of elements" + 
	                  " in x must match the number of elements in y");
	    }
	    else{
	    	count = x.length;
	    }
	    for (int i = 0; i < count; i++)
	    {
	        sum = sum + Math.pow(Math.abs(x[i] - y[i]),2);
	    }
	    distance = Math.sqrt(sum);
	    return distance;
	}
	
}