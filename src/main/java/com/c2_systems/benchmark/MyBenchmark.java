/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

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
import org.openjdk.jmh.annotations.Benchmark;

public class MyBenchmark {

    @Benchmark
    public void testMethod() {

		// discrete time interval
		double dt = 0.1d;
		// position measurement noise (meter)
		double measurementNoise = 10d;
		// acceleration noise (meter/sec^2)
		double accelNoise = 0.2d;

		// A = [ 1 dt ]
//		     [ 0  1 ]
		RealMatrix A = new Array2DRowRealMatrix(new double[][] { { 1, dt }, { 0, 1 } });
		// B = [ dt^2/2 ]
//		     [ dt     ]
		RealMatrix B = new Array2DRowRealMatrix(new double[][] { { Math.pow(dt, 2d) / 2d }, { dt } });
		// H = [ 1 0 ]
		RealMatrix H = new Array2DRowRealMatrix(new double[][] { { 1d, 0d } });
		// x = [ 0 0 ]
		RealVector x = new ArrayRealVector(new double[] { 0, 0 });

		RealMatrix tmp = new Array2DRowRealMatrix(new double[][] {
		    { Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d },
		    { Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d) } });
		// Q = [ dt^4/4 dt^3/2 ]
//		     [ dt^3/2 dt^2   ]
		RealMatrix Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));
		// P0 = [ 1 1 ]
//		      [ 1 1 ]
		RealMatrix P0 = new Array2DRowRealMatrix(new double[][] { { 1, 1 }, { 1, 1 } });
		// R = [ measurementNoise^2 ]
		RealMatrix R = new Array2DRowRealMatrix(new double[] { Math.pow(measurementNoise, 2) });

		// constant control input, increase velocity by 0.1 m/s per cycle
		RealVector u = new ArrayRealVector(new double[] { 0.1d });

		ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
		MeasurementModel mm = new DefaultMeasurementModel(H, R);
		KalmanFilter filter = new KalmanFilter(pm, mm);

		RandomGenerator rand = new JDKRandomGenerator();

		RealVector tmpPNoise = new ArrayRealVector(new double[] { Math.pow(dt, 2d) / 2d, dt });
		RealVector mNoise = new ArrayRealVector(1);

		// iterate 60 steps
		for (int i = 0; i < 60; i++) {
		    filter.predict(u);

		    // simulate the process
		    RealVector pNoise = tmpPNoise.mapMultiply(accelNoise * rand.nextGaussian());

		    // x = A * x + B * u + pNoise
		    x = A.operate(x).add(B.operate(u)).add(pNoise);

		    // simulate the measurement
		    mNoise.setEntry(0, measurementNoise * rand.nextGaussian());

		    // z = H * x + m_noise
		    RealVector z = H.operate(x).add(mNoise);

		    filter.correct(z);

		    double position = filter.getStateEstimation()[0];
		    double velocity = filter.getStateEstimation()[1];

		}

    }

}
