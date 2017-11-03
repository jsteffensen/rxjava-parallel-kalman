package com.c2_systems.benchmark;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

public class FilterObject {

	private double dt, measurementNoise, accelNoise;
	private RealMatrix A, B, H, tmp, Q, P0, R;
	private RealVector x, u, tmpPNoise, mNoise;
	private ProcessModel pm;
	private MeasurementModel mm;
	private KalmanFilter filter;

	FilterObject() {

	    dt = 0.1d;
	    measurementNoise = 10d;
	    accelNoise = 0.2d;

	    A = new Array2DRowRealMatrix(new double[][] { { 1, dt }, { 0, 1 } });
	    B = new Array2DRowRealMatrix(new double[][] { { Math.pow(dt, 2d) / 2d }, { dt } });
	    H = new Array2DRowRealMatrix(new double[][] { { 1d, 0d } });
	    x = new ArrayRealVector(new double[] { 0, 0 });

	    tmp = new Array2DRowRealMatrix(new double[][] {
	        { Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d },
	        { Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d) } });

	    Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));
	    P0 = new Array2DRowRealMatrix(new double[][] { { 1, 1 }, { 1, 1 } });
	    R = new Array2DRowRealMatrix(new double[] { Math.pow(measurementNoise, 2) });
	    u = new ArrayRealVector(new double[] { 0.1d });

	    pm = new DefaultProcessModel(A, B, Q, x, P0);
	    mm = new DefaultMeasurementModel(H, R);
	    filter = new KalmanFilter(pm, mm);

	}

	public void predict() {
		filter.predict(u);
	}

	public void correct() {

		RandomGenerator rand = new JDKRandomGenerator();
	    tmpPNoise = new ArrayRealVector(new double[] { Math.pow(dt, 2d) / 2d, dt });
	    mNoise = new ArrayRealVector(1);

	    RealVector pNoise = tmpPNoise.mapMultiply(accelNoise * rand.nextGaussian());
	    x = A.operate(x).add(B.operate(u)).add(pNoise);
	    mNoise.setEntry(0, measurementNoise * rand.nextGaussian());

	    RealVector z = H.operate(x).add(mNoise);

	    filter.correct(z);
	}

	public Double getPosition() {
		return filter.getStateEstimation()[0];
	}

	public Double getVelocity() {
		return filter.getStateEstimation()[1];
	}

}
