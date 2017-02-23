package input;

import objects.Vectorf;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;

/**
 * TODO: Lose all the static methods, make this into a singleton
 */
public class InputManager {

	public static final short VECTOR_SIZE = 3;
	public static final short MATRIX_SIZE_4x4 = 16;

	public static final float RADIANS_TO_DEGREES = 57.2957795f;
	// final short MATRIX_SIZE_3x3 = 9;

	private static float[] lastAccel;
	private static float[] lastMagneticField;

	public static void readInputs() {
		// TODO: controll movement etc from here
		// TODO: handle touch events to pause
	}
	
	public static Vectorf getGravityDirection() {
		Vectorf down = new Vectorf(0, -1, 0);
		
		if (!hasReading()) {
			return null;
		}
		float[] orientation = getOrientation();
				 //Separate yaw/pitch/roll components wrt to the physical device, not view window
		float yaw = orientation[0] * RADIANS_TO_DEGREES;
		float pitch = orientation[1] * RADIANS_TO_DEGREES;
		float roll = orientation[2] * RADIANS_TO_DEGREES;
		
		down.rotate(pitch, 0, 0, 1);
		return down;
	}

	private static float[] getOrientation() {

		float[] rotationMat = new float[MATRIX_SIZE_4x4];
		float[] inclinationMat = new float[MATRIX_SIZE_4x4]; // arg 2 of
																// getRotationMatrix
		// float[] gravity = new float[VECTOR_SIZE]; //m_lastAccels from example
		// at
		// http://stackoverflow.com/questions/4576493/how-can-i-use-sensormanager-getorientation-for-tilt-controls-like-my-paper-plan
		float[] geomagnetic = new float[VECTOR_SIZE]; // m_lastMagFields

		float[] orientation = new float[MATRIX_SIZE_4x4];

		// Initialise required matrices
		Matrix.setIdentityM(rotationMat, 0);
		Matrix.setIdentityM(inclinationMat, 0);

		if (SensorManager.getRotationMatrix(rotationMat, null, lastAccel,
				lastMagneticField)) {
			SensorManager.getOrientation(rotationMat, orientation);
		} else {
			Log.d("Input", "Could not get rotation matrix");
		}

		return orientation;
	}

	private static Vectorf getGravity() {
		float[] gravity = { 0f, 0f, 1f }; // Into table if sitting landscaped w
											// screen up (TODO: check this)
		return new Vectorf(gravity[0], gravity[1], gravity[2]);
	}

	private static boolean hasReading() {
		return hasAccelReading() && hasMagneticReading();
	}

	private static boolean hasMagneticReading() {
		if (lastMagneticField == null) { // No mag. field reading
			Log.d("InputManager", "No mag field readings yet!");
			return false;
		}

		return true;
	}

	private static boolean hasAccelReading() {
		if (lastAccel == null) { // No accel reading yet
			Log.d("InputManager", "No accelerometer readings yet!");
			return false;
		}

		return true;
	}

	public static void initSensors(SensorManager sm) {
		initAccelerometer(sm);
		initMagneticFieldSensor(sm);
	}
	
	private static void initAccelerometer(SensorManager sm) {
		Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		SensorEventListener accelListener = new SensorEventListener() {

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

			public void onSensorChanged(SensorEvent event) {
				lastAccel = event.values;
			}
		};
		sm.registerListener(accelListener, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private static void initMagneticFieldSensor(SensorManager sm) {
		Sensor magneticFieldSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		SensorEventListener magFieldListener = new SensorEventListener() {

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

			public void onSensorChanged(SensorEvent event) {
				lastMagneticField = event.values;
			}
		};
		sm.registerListener(magFieldListener, magneticFieldSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}
}
