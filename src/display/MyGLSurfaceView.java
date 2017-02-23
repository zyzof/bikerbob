package display;

import input.TouchHandler;
import game.Scene;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
	private TouchHandler touchHandler;
	
    public MyGLSurfaceView(Context context) {
    	super(context);
    }
    
    public boolean onTouchEvent(MotionEvent event) {
    	if (touchHandler == null) {
    		MyRenderer renderer = MyRenderer.getInstance();
    		int width = renderer.getScreenWidthPx();
    		int height = renderer.getScreenHeightPx();
    		
    		touchHandler = new TouchHandler(width, height);
    	}
    	
    	touchHandler.handleTouchEvent(event);
    	
		return true;
    }
}
