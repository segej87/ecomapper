package dataproc;
import java.util.List;

public final class Stats {

	public Stats(){
		
	}
	
	public static float getMean(List<Float> vals){
		float vSum = 0;
		for (float v : vals){
			vSum = vSum + v;
		}
		float vMean = vSum/vals.size();
		return vMean;
	}
	
	public static float getSig(List<Float> vals, float mu){
		float vRes = 0;
		for (float v : vals){
			vRes = (v - mu) * (v - mu) + vRes;
		}
		float vSig = (float) Math.pow(vRes/vals.size(), (float) 0.5);
		return vSig;
	}
	
	public static float getMax(List<Float> vals){
		float vMax = vals.get(0);
		for (float v : vals){
			if (v > vMax) { vMax = v; }
		}
		return vMax;
	}
	
	public static float getMin(List<Float> vals){
		float vMin = vals.get(0);
		for (float v : vals){
			if (v < vMin) { vMin = v; }
		}
		return vMin;
	}
}
