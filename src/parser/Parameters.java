package parser;

import java.io.Serializable;
import utils.FeatureVector;
import utils.Utils;

public class Parameters implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean useGP;
	private float C;
	private float gammaL;

    public void setGammaL(float gammaL) {
        this.gammaL = gammaL;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setRank2(int rank2) {
        this.rank2 = rank2;
    }

    private int rank;
	private int	rank2;

    private int numberWordFeatures;
    private int T;
    private int DL;

    public int getT() {
        return T;
    }

    public int getNumberWordFeatures() {
        return numberWordFeatures;
    }

    public int getDL() {
        return DL;
    }

    private float[] paramsL;

    public float[] getParamsL() {
        return paramsL;
    }

    private transient float[] totalL;

    private float[][] U;
    private float[][] V;
    private float[][] WL;

    public float[][] getU() {
        return U;
    }

    public float[][] getV() {
        return V;
    }

    public float[][] getWL() {
        return WL;
    }

    private float[][] U2;
    private float[][] V2;
    private float[][] W2;
    private float[][] X2L;

    public float[][] getU2() {
        return U2;
    }

    public float[][] getV2() {
        return V2;
    }

    public float[][] getW2() {
        return W2;
    }

    public float[][] getX2L() {
        return X2L;
    }

    public float[][] getY2L() {
        return Y2L;
    }

    private float[][] Y2L;


    private transient float[][] totalU;
    private transient float[][] totalV;
    private transient float[][] totalWL;
    private transient float[][] totalU2;
    private transient float[][] totalV2;
    private transient float[][] totalW2;
    private transient float[][] totalX2L;
    private transient float[][] totalY2L;

    private transient FeatureVector[] dU;
    private transient FeatureVector[] dV;
    private transient FeatureVector[] dWL;
    private transient FeatureVector[] dU2;
    private transient FeatureVector[] dV2;
    private transient FeatureVector[] dW2;
    private transient FeatureVector[] dX2L;
    private transient FeatureVector[] dY2L;
	
	public Parameters(DependencyPipe pipe, Options options) 
	{
		numberWordFeatures = pipe.getSynFactory().getNumWordFeats();
		T = pipe.getTypes().length;
		DL = T * 3;
        useGP = options.isUseGP();
		C = options.C;
		gammaL = options.gammaLabel;
		rank = options.R;
		rank2 = options.R2;

		int sizeL = pipe.getSynFactory().getNumLabeledArcFeats() + 1;
		paramsL = new float[sizeL];
		totalL = new float[sizeL];

		U = new float[numberWordFeatures][rank];
		V = new float[numberWordFeatures][rank];
		WL = new float[DL][rank];
		totalU = new float[numberWordFeatures][rank];
		totalV = new float[numberWordFeatures][rank];
		totalWL = new float[DL][rank];
		dU = new FeatureVector[rank];
		dV = new FeatureVector[rank];
		dWL = new FeatureVector[rank];
		
		if (useGP) {
			U2 = new float[numberWordFeatures][rank2];
			V2 = new float[numberWordFeatures][rank2];
			W2 = new float[numberWordFeatures][rank2];
			X2L = new float[DL][rank2];
			Y2L = new float[DL][rank2];
			totalU2 = new float[numberWordFeatures][rank2];
			totalV2 = new float[numberWordFeatures][rank2];
			totalW2 = new float[numberWordFeatures][rank2];
			totalX2L = new float[DL][rank2];
			totalY2L = new float[DL][rank2];
			dU2 = new FeatureVector[rank2];
			dV2 = new FeatureVector[rank2];
			dW2 = new FeatureVector[rank2];
			dX2L = new FeatureVector[rank2];
			dY2L = new FeatureVector[rank2];
		}
	}
	
	private void copyValue(float[][] target, float[][] source)
	{
		int n = source.length;
		for (int i = 0; i < n; ++i)
			target[i] = source[i].clone();
	}
	
	public void assignTotal()
	{
		copyValue(totalU, U);
		copyValue(totalV, V);
		copyValue(totalWL, WL);
		if (useGP) {
			copyValue(totalU2, U2);
			copyValue(totalV2, V2);
			copyValue(totalW2, W2);
			copyValue(totalX2L, X2L);
			copyValue(totalY2L, Y2L);
		}

	}
	
	private void assignColumn(float[][] mat, int col, float[] values)
	{
		for (int id = 0, tot=values.length; id < tot; ++id)
			mat[id][col] = values[id];
	}
	
	public void randomlyInit()
	{
		
		for (int i = 0; i < rank; ++i) {
			assignColumn(U, i, Utils.getRandomUnitVector(numberWordFeatures));
			assignColumn(V, i, Utils.getRandomUnitVector(numberWordFeatures));
			assignColumn(WL, i, Utils.getRandomUnitVector(DL));
		}
		if (useGP) {
			for (int i = 0; i < rank2; ++i) {
				assignColumn(U2, i, Utils.getRandomUnitVector(numberWordFeatures));
				assignColumn(V2, i, Utils.getRandomUnitVector(numberWordFeatures));
				assignColumn(W2, i, Utils.getRandomUnitVector(numberWordFeatures));
				assignColumn(X2L, i, Utils.getRandomUnitVector(DL));
				assignColumn(Y2L, i, Utils.getRandomUnitVector(DL));
			}
		}
		assignTotal();
	}
	
	private void averageTheta(float[] a, float[] totala, int T, float c)
	{
		int n = a.length;
		for (int i = 0; i < n; ++i) {
			a[i] += c*totala[i]/T;
		}
	}
	
	private void averageTensor(float[][] a, float[][] totala, int T, float c)
	{
		int n = a.length;
		if (n == 0)
			return;
		int m = a[0].length;
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < m; ++j) {
				a[i][j] += c*totala[i][j]/T;
			}
	}
	
	public void averageParameters(int T, float c) 
	{
		averageTheta(paramsL, totalL, T, c);
		
		averageTensor(U, totalU, T, c);
		averageTensor(V, totalV, T, c);
		averageTensor(WL, totalWL, T, c);
		
		if (useGP) {
			averageTensor(U2, totalU2, T, c);
			averageTensor(V2, totalV2, T, c);
			averageTensor(W2, totalW2, T, c);
			averageTensor(X2L, totalX2L, T, c);
			averageTensor(Y2L, totalY2L, T, c);
		}
	}

	private void printStat(float[][] a, String s) 
	{
		int n = a.length;
		float sum = 0;
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < n; ++i) {
			sum += Utils.squaredSum(a[i]);
			min = Math.min(min, Utils.min(a[i]));
			max = Math.max(max, Utils.max(a[i]));
		}
		System.out.printf(" |%s|^2: %f min: %f\tmax: %f%n", s, sum, min, max);
	}
	
	public void printStat()
	{
		printStat(U, "U");
		printStat(V, "V");
		printStat(WL, "WL");

		if (useGP) {
			printStat(U2, "U2");
			printStat(V2, "V2");
			printStat(W2, "W2");
			printStat(X2L, "X2L");
			printStat(Y2L, "Y2L");
		}
	}
	
	private void projectMat(float[][] mat, FeatureVector fv, float[] proj)
	{
		int rank = proj.length;
		for (int id = 0, n = fv.size(); id < n; ++id) {
			int i = fv.x(id);
			float w = fv.value(id);
			for (int j = 0; j < rank; ++j)
				proj[j] += mat[i][j] * w;
		}
	}
	
	public void projectU(FeatureVector fv, float[] proj)
	{
		projectMat(U, fv, proj);
	}
	
	public void projectV(FeatureVector fv, float[] proj) 
	{
		projectMat(V, fv, proj);
	}
	
	public void projectU2(FeatureVector fv, float[] proj)
	{
		projectMat(U2, fv, proj);
	}
	
	public void projectV2(FeatureVector fv, float[] proj)
	{
		projectMat(V2, fv, proj);
	}
	
	public void projectW2(FeatureVector fv, float[] proj)
	{
		projectMat(W2, fv, proj);
	}

	public float dotProductL(float[] proju, float[] projv, int lab, int dir)
	{
		float sum = 0;
		for (int r = 0; r < rank; ++r)
			sum += proju[r] * projv[r] * (WL[lab][r] + WL[dir*T+lab][r]);
		return sum;
	}
	
	public float dotProduct2L(float[] proju, float[] projv, float[] projw,
			int plab, int lab, int pdir, int dir)
	{
		float sum = 0;
		for (int r = 0; r < rank2; ++r)
			sum += proju[r] * projv[r] * projw[r] * (X2L[plab][r] + X2L[pdir*T+plab][r])
					* (Y2L[lab][r] + Y2L[dir*T+lab][r]);
		return sum;
	}
	
	private void addTheta(float[] a, float[] totala, FeatureVector da,
			float coeff, float coeff2)
	{
		if (da == null)
			return;
		for (int i = 0, K = da.size(); i < K; ++i) {
    		int x = da.x(i);
    		float z = da.value(i);
    		a[x] += coeff * z;
    		totala[x] += coeff2 * z;
		}
	}
	
	private void addTensor(float[][] a, float[][] totala, FeatureVector[] da,
			float coeff, float coeff2)
	{
		int n = da.length;
		for (int k = 0; k < n; ++k) {
    		FeatureVector dak = da[k];
    		if (dak == null)
    			continue;
    		for (int i = 0, K = dak.size(); i < K; ++i) {
    			int x = dak.x(i);
    			float z = dak.value(i);
    			a[x][k] += coeff * z;
    			totala[x][k] += coeff2 * z;
    		}
    	}
	}
	
	public float updateLabel(DependencyInstance gold, int[] predDeps, int[] predLabs,
			LocalFeatureData lfd, int updCnt)
	{
    	int[] actDeps = gold.getHeads();
    	int[] actLabs = gold.getDeplbids();
    	
    	float Fi = getLabelDis(actLabs, predLabs);
        	
    	FeatureVector dtl = lfd.getLabeledFeatureDifference(gold, predDeps, predLabs);
    	float loss = - dtl.dotProduct(paramsL)*gammaL + Fi;
        float l2norm = dtl.squaredL2NormUnsafe() * gammaL * gammaL;
    	
        // update U
    	for (int k = 0; k < rank; ++k) {        		
    		FeatureVector dUk = getdUL(k, lfd, actDeps, actLabs, predDeps, predLabs);
        	l2norm += dUk.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
        	for (int u = 0, n = dUk.size(); u < n; ++u)
        		loss -= U[dUk.x(u)][k] * dUk.value(u) * (1-gammaL);
        	dU[k] = dUk;
    	}
    	// update V
    	for (int k = 0; k < rank; ++k) {
    		FeatureVector dVk = getdVL(k, lfd, actDeps, actLabs, predDeps, predLabs);
        	l2norm += dVk.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
        	dV[k] = dVk;
    	}        	
        // update WL
    	for (int k = 0; k < rank; ++k) {
    		FeatureVector dWLk = getdWL(k, lfd, actDeps, actLabs, predDeps, predLabs);
        	l2norm += dWLk.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
        	dWL[k] = dWLk;
    	}
    	
    	if (useGP) {
	    	// update U2
	    	for (int k = 0; k < rank2; ++k) {
	    		FeatureVector dU2k = getdU2L(k, lfd, actDeps, actLabs, predLabs);
	        	l2norm += dU2k.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
	        	for (int u = 0, n = dU2k.size(); u < n; ++u)
	        		loss -= U2[dU2k.x(u)][k] * dU2k.value(u) * (1-gammaL);
	        	dU2[k] = dU2k;
	    	}
	    	// update V2
	    	for (int k = 0; k < rank2; ++k) {
	    		FeatureVector dV2k = getdV2L(k, lfd, actDeps, actLabs, predLabs);
	        	l2norm += dV2k.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
	        	dV2[k] = dV2k;
	    	} 
	    	// update W2
	    	for (int k = 0; k < rank2; ++k) {
	    		FeatureVector dW2k = getdW2L(k, lfd, actDeps, actLabs, predLabs);
	        	l2norm += dW2k.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
	        	dW2[k] = dW2k;
	    	}
	    	// update X2L
	    	for (int k = 0; k < rank2; ++k) {
	    		FeatureVector dX2Lk = getdX2L(k, lfd, actDeps, actLabs, predLabs);
	        	l2norm += dX2Lk.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
	        	dX2L[k] = dX2Lk;
	    	}
	    	// update Y2L
	    	for (int k = 0; k < rank2; ++k) {
	    		FeatureVector dY2Lk = getdY2L(k, lfd, actDeps, actLabs, predLabs);
	        	l2norm += dY2Lk.squaredL2NormUnsafe() * (1-gammaL) * (1-gammaL);
	        	dY2L[k] = dY2Lk;
	    	}
    	}
        
        float alpha = loss/l2norm;
    	alpha = Math.min(C, alpha);
    	if (alpha > 0) {
    		float coeff;
            float coeff2;
    		
    		coeff = alpha * gammaL;
    		coeff2 = coeff * (1-updCnt);
    		addTheta(paramsL, totalL, dtl, coeff, coeff2);
    		
    		coeff = alpha * (1-gammaL);
			coeff2 = coeff * (1-updCnt);
			addTensor(U, totalU, dU, coeff, coeff2);
			addTensor(V, totalV, dV, coeff, coeff2);
			addTensor(WL, totalWL, dWL, coeff, coeff2);
			if (useGP) {
				addTensor(U2, totalU2, dU2, coeff, coeff2);
				addTensor(V2, totalV2, dV2, coeff, coeff2);
				addTensor(W2, totalW2, dW2, coeff, coeff2);
				addTensor(X2L, totalX2L, dX2L, coeff, coeff2);
				addTensor(Y2L, totalY2L, dY2L, coeff, coeff2);
			}
    	}
    	return loss;
	}
    
    private FeatureVector getdUL(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs,
			int[] predDeps, int[] predLabs) {
    	float[][] wpV = lfd.wpV;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	FeatureVector dU = new FeatureVector();
    	for (int mod = 1; mod < L; ++mod) {
    		assert(actDeps[mod] == predDeps[mod]);
    		int head  = actDeps[mod];
    		int dir = head > mod ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		if (lab == lab2){
    		    continue;
            }
    		float dotv = wpV[mod][k];
    		dU.addEntries(wordFvs[head], dotv * (WL[lab][k] + WL[dir*T+lab][k])
    									 - dotv * (WL[lab2][k] + WL[dir*T+lab2][k]));
    	}
    	return dU;
    }
    
    private FeatureVector getdVL(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs,
			int[] predDeps, int[] predLabs) {
    	float[][] wpU = lfd.wpU;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	FeatureVector dV = new FeatureVector();
    	for (int mod = 1; mod < L; ++mod) {
    		assert(actDeps[mod] == predDeps[mod]);
    		int head  = actDeps[mod];
    		int dir = head > mod ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		if (lab == lab2) continue;
    		float dotu = wpU[head][k];   //wordFvs[head].dotProduct(U[k]);
    		dV.addEntries(wordFvs[mod], dotu  * (WL[lab][k] + WL[dir*T+lab][k])
					- dotu * (WL[lab2][k] + WL[dir*T+lab2][k]));    
    	}
    	return dV;
    }
    
    private FeatureVector getdWL(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs,
			int[] predDeps, int[] predLabs) {
    	float[][] wpU = lfd.wpU, wpV = lfd.wpV;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	float[] dWL = new float[DL];
    	for (int mod = 1; mod < L; ++mod) {
    		assert(actDeps[mod] == predDeps[mod]);
    		int head = actDeps[mod];
    		int dir = head > mod ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		if (lab == lab2) continue;
    		float dotu = wpU[head][k];   //wordFvs[head].dotProduct(U[k]);
    		float dotv = wpV[mod][k];  //wordFvs[mod].dotProduct(V[k]);
    		dWL[lab] += dotu * dotv;
    		dWL[dir*T+lab] += dotu * dotv;
    		dWL[lab2] -= dotu * dotv;
    		dWL[dir*T+lab2] -= dotu * dotv;
    	}
    	
    	FeatureVector dWLfv = new FeatureVector();
    	for (int i = 0; i < DL; ++i)
    		dWLfv.addEntry(i, dWL[i]);
    	return dWLfv;
    }
    
    private FeatureVector getdU2L(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs, int[] predLabs) {
    	float[][] wpV2 = lfd.wpV2;
    	float[][] wpW2 = lfd.wpW2;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	FeatureVector dU2 = new FeatureVector();
    	for (int mod = 1; mod < L; ++mod) {
    		int head  = actDeps[mod];
    		int gp = actDeps[head];
    		if (gp == -1)
    			continue;
    		int dir = head > mod ? 1 : 2;
    		int pdir = gp > head ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		int plab = actLabs[head];
    		int plab2 = predLabs[head];
    		if (lab == lab2 && plab == plab2) continue;
    		float dotv2 = wpV2[head][k];
    		float dotw2 = wpW2[mod][k];
    		dU2.addEntries(wordFvs[gp], dotv2 * dotw2 * (X2L[plab][k] + X2L[pdir*T+plab][k]) * (Y2L[lab][k] + Y2L[dir*T+lab][k])
					  - dotv2 * dotw2 * (X2L[plab2][k] + X2L[pdir*T+plab2][k]) * (Y2L[lab2][k] + Y2L[dir*T+lab2][k]));
    	}
    	return dU2;
    }
    
    private FeatureVector getdV2L(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs, int[] predLabs) {
    	float[][] wpU2 = lfd.wpU2;
    	float[][] wpW2 = lfd.wpW2;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	FeatureVector dV2 = new FeatureVector();
    	for (int mod = 1; mod < L; ++mod) {
    		int head  = actDeps[mod];
    		int gp = actDeps[head];
    		if (gp == -1)
    			continue;
    		int dir = head > mod ? 1 : 2;
    		int pdir = gp > head ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		int plab = actLabs[head];
    		int plab2 = predLabs[head];
    		if (lab == lab2 && plab == plab2) continue;
    		float dotu2 = wpU2[gp][k];
    		float dotw2 = wpW2[mod][k];
    		dV2.addEntries(wordFvs[head], dotu2 * dotw2 * (X2L[plab][k] + X2L[pdir*T+plab][k]) * (Y2L[lab][k] + Y2L[dir*T+lab][k])
				    - dotu2 * dotw2 * (X2L[plab2][k] + X2L[pdir*T+plab2][k]) * (Y2L[lab2][k] + Y2L[dir*T+lab2][k]));
    	}
    	return dV2;
    }
    
    private FeatureVector getdW2L(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs, int[] predLabs) {
    	float[][] wpU2 = lfd.wpU2;
    	float[][] wpV2 = lfd.wpV2;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	FeatureVector dW2 = new FeatureVector();
    	for (int mod = 1; mod < L; ++mod) {
    		int head  = actDeps[mod];
    		int gp = actDeps[head];
    		if (gp == -1)
    			continue;
    		int dir = head > mod ? 1 : 2;
    		int pdir = gp > head ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		int plab = actLabs[head];
    		int plab2 = predLabs[head];
    		if (lab == lab2 && plab == plab2) continue;
    		float dotu2 = wpU2[gp][k];
    		float dotv2 = wpV2[head][k];
    		dW2.addEntries(wordFvs[mod], dotu2 * dotv2 * (X2L[plab][k] + X2L[pdir*T+plab][k]) * (Y2L[lab][k] + Y2L[dir*T+lab][k])
					  - dotu2 * dotv2 * (X2L[plab2][k] + X2L[pdir*T+plab2][k]) * (Y2L[lab2][k] + Y2L[dir*T+lab2][k]));
    	}
    	return dW2;
    }
    
    private FeatureVector getdX2L(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs, int[] predLabs) {
    	float[][] wpU2 = lfd.wpU2, wpV2 = lfd.wpV2, wpW2 = lfd.wpW2;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	float[] dX2L = new float[DL];
    	for (int mod = 1; mod < L; ++mod) {
    		int head  = actDeps[mod];
    		int gp = actDeps[head];
    		if (gp == -1)
    			continue;
    		int dir = head > mod ? 1 : 2;
    		int pdir = gp > head ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		int plab = actLabs[head];
    		int plab2 = predLabs[head];
    		if (lab == lab2 && plab == plab2) continue;
    		float dotu2 = wpU2[gp][k];
    		float dotv2 = wpV2[head][k];
    		float dotw2 = wpW2[mod][k];
    		float val = dotu2 * dotv2 * dotw2 * (Y2L[lab][k] + Y2L[dir*T+lab][k]);
    		float val2 = dotu2 * dotv2 * dotw2 * (Y2L[lab2][k] + Y2L[dir*T+lab2][k]);
    		dX2L[plab] += val;
    		dX2L[pdir*T+plab] += val;
    		dX2L[plab2] -= val2;
    		dX2L[pdir*T+plab2] -= val2;
    	}
    	
    	FeatureVector dX2Lfv = new FeatureVector();
    	for (int i = 0; i < DL; ++i)
    		dX2Lfv.addEntry(i, dX2L[i]);
    	return dX2Lfv;
    }
    
    private FeatureVector getdY2L(int k, LocalFeatureData lfd, int[] actDeps, int[] actLabs, int[] predLabs) {
    	float[][] wpU2 = lfd.wpU2, wpV2 = lfd.wpV2, wpW2 = lfd.wpW2;
    	FeatureVector[] wordFvs = lfd.wordFvs;
    	int L = wordFvs.length;
    	float[] dY2L = new float[DL];
    	for (int mod = 1; mod < L; ++mod) {
    		int head  = actDeps[mod];
    		int gp = actDeps[head];
    		if (gp == -1)
    			continue;
    		int dir = head > mod ? 1 : 2;
    		int pdir = gp > head ? 1 : 2;
    		int lab  = actLabs[mod];
    		int lab2 = predLabs[mod];
    		int plab = actLabs[head];
    		int plab2 = predLabs[head];
    		if (lab == lab2 && plab == plab2) continue;
    		float dotu2 = wpU2[gp][k];
    		float dotv2 = wpV2[head][k];
    		float dotw2 = wpW2[mod][k];
    		float val = dotu2 * dotv2 * dotw2 * (X2L[plab][k] + X2L[pdir*T+plab][k]);
    		float val2 = dotu2 * dotv2 * dotw2 * (X2L[plab2][k] + X2L[pdir*T+plab2][k]);
    		dY2L[lab] += val;
    		dY2L[dir*T+lab] += val;
    		dY2L[lab2] -= val2;
    		dY2L[dir*T+lab2] -= val2;
    	}
    	
    	FeatureVector dY2Lfv = new FeatureVector();
    	for (int i = 0; i < DL; ++i)
    		dY2Lfv.addEntry(i, dY2L[i]);
    	return dY2Lfv;
    }
	
	private float getLabelDis(int[] actLabs, int[] predLabs)
	{
		float dis = 0;
		for (int i = 1; i < actLabs.length; ++i) {
			if (actLabs[i] != predLabs[i]) dis += 1;
		}
		return dis;
    }
}
