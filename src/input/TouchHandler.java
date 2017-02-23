package input;

import objects.GameObject;
import objects.Pointf;
import objects.Vectorf;
import game.Scene;
import display.MyRenderer;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;


public class TouchHandler {
	private final float ACCEL_INCREMENT = 1f;
	
	private Pointf previousTouchPointInScreenCoords;
	private int screenWidth;
	private int screenHeight;
	private MyRenderer renderer;
	
	public TouchHandler(int screenWidth, int screenHeight) {
		renderer = MyRenderer.getInstance();
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}
	
	public void handleTouchEvent(MotionEvent event) {
		Scene scene = renderer.getScene();
    	GameObject player = scene.getPlayer();
    	
    	float x = event.getX();
    	float y = event.getY();
    	
    	Pointf touchPointInScreenCoords = new Pointf(x, y);
    	Pointf touchPointInWorldCoords = getPointInScreenCoordsInWorldCoords(touchPointInScreenCoords);
    	
    	Log.d("TouchHandler.java", 
    			"Touch at (" + 
    			touchPointInWorldCoords.x + ", " + 
    			touchPointInWorldCoords.y + ")"); 
    	
    	switch (event.getAction()) {
	    	case MotionEvent.ACTION_DOWN: {
	    		//Check if touch is on Bob, if so, start dragging him around
	    		if (pointIsWithinObject(player, touchPointInWorldCoords)) {
	    			player.startDragging();
	    			previousTouchPointInScreenCoords = touchPointInScreenCoords;
	    		} else {
		    		//If the left side of screen was held, decelerate, right side accel.
					Vectorf velocity = player.getVelocity();
					Vectorf newVelocity;
					if (touchPointInScreenCoords.x > screenWidth / 2) {
						newVelocity = new Vectorf(velocity.x + ACCEL_INCREMENT, velocity.y, velocity.z);
					} else {
						newVelocity = new Vectorf(velocity.x - ACCEL_INCREMENT, velocity.y, velocity.z);
					}
					player.setVelocity(newVelocity);
					
					//TODO: keep accelerating when finger held down (not an InputEvent :( )
	    		}
	    		break;
	    	}
	    	case MotionEvent.ACTION_CANCEL:
	    	case MotionEvent.ACTION_UP: {
	    		//Drop Bob
	    		if (player.isBeingDragged()) {
	    			player.endDragging();
	    			previousTouchPointInScreenCoords = null;
	    		}	    		
	    		break;
	    	}
	    	case MotionEvent.ACTION_MOVE: {
	    		//Drag Bob to new location
	    		if (player.isBeingDragged()) {
	    			Pointf prevTouchPointInWorldCoords = getPointInScreenCoordsInWorldCoords(previousTouchPointInScreenCoords);	//Needs to be recalculated as the Camera has moved, changing the vpMat.
	    			
	    			float xMovement = touchPointInWorldCoords.x - prevTouchPointInWorldCoords.x;
	    			float yMovement = touchPointInWorldCoords.y - prevTouchPointInWorldCoords.y;
	    			
	    			player.translate(-xMovement, yMovement, 0);	//Negate x so it increases to the right
	    			
	    			previousTouchPointInScreenCoords = touchPointInScreenCoords;
	    		}
	    		break;
	    	}
    	}
	}
	
	/**
	 * Adapted from Erol's answer on http://stackoverflow.com/questions/10985487/android-opengl-es-2-0-screen-coordinates-to-world-coordinates
	 */
	private Pointf getPointInScreenCoordsInWorldCoords(Pointf touchPoint) {
		float[] invertedMat = new float[16];
		float[] transformMat = new float[16];
		float[] normalisedTouchPoint = new float[4];
		float[] outPoint = new float[4];
		
		Pointf worldPoint = new Pointf(0, 0);
		
		/*
		 * Invert y coordinate, as android uses
		 * top-left, and ogl bottom-left.
		 */
		int oglTouchY = (int) (screenHeight - touchPoint.y);
		
		//Transform screen point to clip coord space in OpenGL
		normalisedTouchPoint[0] = ((touchPoint.x * 2.0f) / screenWidth) - 1.0f;
		normalisedTouchPoint[1] = ((oglTouchY * 2.0f) / screenHeight) - 1.0f;
		normalisedTouchPoint[2] = -1.0f;
		normalisedTouchPoint[3] = 1.0f;
		
		transformMat = renderer.getViewProjectionMat();
		
		Matrix.invertM(invertedMat, 0, transformMat, 0);
		
		//Apply the inverse to the point in clip space
		Matrix.multiplyMV(outPoint, 0, invertedMat, 0, normalisedTouchPoint, 0);
		
		if (outPoint[3] != 0.0) {
			worldPoint.x = -outPoint[0] / outPoint[3];	//Negate x so it increases to the right
			worldPoint.y = outPoint[1] / outPoint[3];
		}
		
		return worldPoint;
	}
	
	private boolean pointIsWithinObject(GameObject obj, Pointf point) {
		Pointf objPos = obj.getPosition();
		
		return point.x < objPos.x + 0.5
				&& point.x > objPos.x - 0.5
				&& point.y < objPos.y + 0.5
				&& point.y > objPos.y - 0.5;
	}
}
