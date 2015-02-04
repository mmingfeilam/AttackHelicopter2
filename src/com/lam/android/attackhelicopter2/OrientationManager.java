package com.lam.android.attackhelicopter2;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Android Orientation Sensor Manager Archetype
 * 
 * @author antoine vianey under GPL v3 :
 *         http://www.gnu.org/licenses/gpl-3.0.html
 */
public class OrientationManager {

	private static Sensor sensor;
	private static SensorManager sensorManager;
	// you could use an OrientationListener array instead
	// if you plans to use more than one listener
	private static OrientationListener listener;

	/** indicates whether or not Orientation Sensor is supported */
	private static Boolean supported;
	/** indicates whether or not Orientation Sensor is running */
	private static boolean running = false;

	/** Sides of the phone */
	enum Side {
		TOP, BOTTOM, LEFT, RIGHT;
	}

	private static float oldPitch, oldRoll;

	public static final int DISTANCE_LIMIT = 25;

	/**
	 * Returns true if the manager is listening to orientation changes
	 */
	public static boolean isListening() {
		return running;
	}

	/**
	 * Unregisters listeners
	 */
	public static void stopListening() {
		running = false;
		try {
			if (sensorManager != null && sensorEventListener != null) {
				sensorManager.unregisterListener(sensorEventListener);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Returns true if at least one Orientation sensor is available
	 */
	public static boolean isSupported() {
		if (supported == null) {
			if (AttackHelicopter.CONTEXT != null) {
				sensorManager = (SensorManager) AttackHelicopter.CONTEXT
						.getSystemService(Context.SENSOR_SERVICE);
				List<Sensor> sensors = sensorManager
						.getSensorList(Sensor.TYPE_ORIENTATION);
				supported = new Boolean(sensors.size() > 0);
			} else {
				supported = Boolean.FALSE;
			}
		}
		return supported;
	}

	/**
	 * Registers a listener and start listening
	 */
	public static void startListening(OrientationListener orientationListener) {
		sensorManager = (SensorManager) AttackHelicopter.CONTEXT
				.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			running = sensorManager.registerListener(sensorEventListener,
					sensor, SensorManager.SENSOR_DELAY_NORMAL);
			listener = orientationListener;

			oldPitch = 0;
			oldRoll = 0;
		}
	}

	/**
	 * The listener that listen to events from the orientation listener
	 */
	private static SensorEventListener sensorEventListener = new SensorEventListener() {

		/** The side that is currently up */
		private Side currentSide = null;
		private Side oldSide = null;
		private float azimuth;
		private float pitch;
		private float roll;

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event) {
			azimuth = event.values[0]; // azimuth
			pitch = event.values[1]; // pitch
			roll = event.values[2]; // roll

//			if (pitch < -20 && pitch > -110) {
//				// top side up
//				currentSide = Side.TOP;
//			} else if (pitch > 20 && pitch < 110) {
//				// bottom side up
//				currentSide = Side.BOTTOM;
//			} else if (roll > 20) {
//				// right side up
//				currentSide = Side.RIGHT;
//			} else if (roll < -20) {
//				// left side up
//				currentSide = Side.LEFT;
//			}
			
			boolean bIsTooTilted = false;
//			
//			if (pitch < -55 && pitch > -145) {
//				bIsTooTilted = true;
//			} else if (pitch > 55 && pitch < 145) {
//				bIsTooTilted = true;
//			} else if (roll > 55) {
//				bIsTooTilted = true;
//			} else if (roll < -55) {
//				bIsTooTilted = true;
//			}
			
			// if the phone is too tilted
			if (pitch < -65 || pitch > 65) 
			{
				bIsTooTilted = true;
			} 
			if (roll < -55 || roll > 55) 
			{
				bIsTooTilted = true;
			}
			
			// if the user tilts too fast
			if (Math.abs((pitch - oldPitch)) > DISTANCE_LIMIT
					|| Math.abs((roll - oldRoll)) > DISTANCE_LIMIT) {
				bIsTooTilted = true;
			}

			oldRoll = roll;
			oldPitch = pitch;
			
			if (bIsTooTilted) {
				//listener.onOrientationChangedTooMuch(pitch, roll, azimuth);
			}
			
			listener.onOrientationChanged(pitch, roll, azimuth);

//			if (currentSide != null && !currentSide.equals(oldSide)) {
//				// forwards orientation to the OrientationListener
//				if (bIsTooTilted) {
//					listener.onOrientationChangedTooMuch();
//				}
//				oldSide = currentSide;
//			}

		}

	};

}