package game;

import display.MyGLSurfaceView;
import display.MyRenderer;
import input.InputManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Force game to run in landscape
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		GLSurfaceView gameSurfaceView = createGameSurfaceView();
		registerAccelerometer();
		
		setContentView(gameSurfaceView);
	}
	
	private void registerAccelerometer() {
		SensorManager sensorManager;
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		InputManager.initSensors(sensorManager);
	}
	
	private GLSurfaceView createGameSurfaceView() {
		GLSurfaceView mGLView = new MyGLSurfaceView(this);

		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager
				.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			mGLView.setEGLContextClientVersion(2);

			// Set the renderer to our demo renderer, defined below.
			MyRenderer.initialise(getApplicationContext());
			mGLView.setRenderer(MyRenderer.getInstance());
			
			return mGLView;
		} else {
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return null;
		}
	}
}