/**
 * Adapted from Android OpenGLES2.0 tutorial:
 * http://developer.android.com/training/graphics/opengl/index.html
 */

package display;

import game.Scene;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import objects.GameObject;
import objects.Ground;
import objects.Plane;
import objects.Pointf;
import objects.Vectorf;
import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class MyRenderer implements GLSurfaceView.Renderer {	
	private final String TAG = "MyRenderer";
	private final int COORDS_PER_VERTEX = 3;
    
    //Shader handles:
    private int spriteShaderProgram;
    private int groundShaderProgram;	//TODO this needs to be in a separate renderer class
    
    private int positionHandle;
    private int colorHandle;
    private int mvpMatrixHandle;
    private int textureUniformHandle;
    private int textureCoordHandle;

    //Matrices:
    private final float[] viewProjectionMat = new float[16];
    private final float[] projectionMat = new float[16];
    private final float[] viewMat = new float[16];
    
    private int screenHeightPx;
    private int screenWidthPx;
    
	private Context appContext;
	
    private Scene scene;
    
	private static MyRenderer instance;
	
    private MyRenderer(Context appContext) {
		this.appContext = appContext;
	}
	
	public static void initialise(Context context) {
		instance = new MyRenderer(context);
	}
	
	public static MyRenderer getInstance() {
		return instance;
	}
    
    public Scene getScene() {
    	return scene;
    }
    
    public float[] getViewProjectionMat() {
    	return viewProjectionMat;
    }
    
	public Context getAppContext() {
		return appContext;
	}
	
	public int getScreenWidthPx() {
		return screenWidthPx;
	}

	public int getScreenHeightPx() {
		return screenHeightPx;
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
     // create OpenGL program executables
        loadSpriteShaders();
        loadGroundShaders();
        
        //Create scene
        scene = new Scene();
    }

	private void loadSpriteShaders() {
		spriteShaderProgram = loadShaders("shader");
	}
	
	private void loadGroundShaders() {
		groundShaderProgram = loadShaders("line_shader");
	}
	
	/**
	 * Loads shaders
	 * @param name
	 * @return Shader program handle
	 */
	private int loadShaders(String name) {
		//Create shaders:        
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, "shaders/" + name + ".vert");
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, "shaders/" + name + ".frag");

        int program = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);
        
        return program;
	}

    public void onDrawFrame(GL10 unused) {

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(viewProjectionMat, 0, projectionMat, 0, viewMat, 0);
        
        scene.doCurrentFrame();
        moveCamera(scene);
    }

    private final float FAR_PLANE_AND_EYE_Z_POS = 70f;
    private void moveCamera(Scene scene) {
    	Matrix.setLookAtM(viewMat,
        		0, 
        		-scene.getCameraPosition().x, scene.getCameraPosition().y, -FAR_PLANE_AND_EYE_Z_POS,
        		-scene.getCameraPosition().x, scene.getCameraPosition().y, 0f,
        		0f, 1.0f, 0.0f);
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		this.screenWidthPx = width;
		this.screenHeightPx = height;
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 0, 7);
        float ORTHO_BOUNDS_FACTOR = 4;	//Controls the scale of the screen. Larger value here means everything smaller on screen.
        Matrix.orthoM(projectionMat, 0, 
        		-ORTHO_BOUNDS_FACTOR*ratio, ORTHO_BOUNDS_FACTOR*ratio, //left, right
        		-ORTHO_BOUNDS_FACTOR, ORTHO_BOUNDS_FACTOR,	//bottom, top
        		0, FAR_PLANE_AND_EYE_Z_POS);	//Near,far
    }

    public int loadShader(int type, String filename){        
        String shaderCode = getShaderString(filename);

        return loadShaderFromString(type, shaderCode);
    }
    
    private int loadShaderFromString(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        shaderCode.hashCode();
        Log.d("ShaderString", shaderCode);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    
    private String getShaderString(String filename) {
    	AssetManager am = appContext.getAssets();
    	InputStream inputStream;
        
    	try {
			inputStream = am.open(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        
		
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);
 
        String nextLine;
        final StringBuilder body = new StringBuilder();
 
        try
        {
            while ((nextLine = bufferedReader.readLine()) != null)
            {
                body.append(nextLine);
                body.append('\n');
            }
        }
        catch (IOException e)
        {
            return null;
        }
 
        return body.toString();
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
    
    public void draw(GameObject obj) {
    	float[] modelViewProjectionMat = new float[16];
		Matrix.multiplyMM(modelViewProjectionMat, 0, viewProjectionMat, 0, obj.getTransform(), 0);
		
		// Add program to OpenGL environment
        GLES20.glUseProgram(spriteShaderProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(spriteShaderProgram, "aPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     obj.getVertexStride(), obj.getVertexBuffer());

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetUniformLocation(spriteShaderProgram, "uColor");

        // Set color of plain untextured object
        GLES20.glUniform4fv(colorHandle, 1, obj.getColour(), 0);

        // get handle to shape's transformation matrix
        mvpMatrixHandle = GLES20.glGetUniformLocation(spriteShaderProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionMat, 0);
        checkGlError("glUniformMatrix4fv");
        
        
        /* Texture related bindings: */
        textureUniformHandle = GLES20.glGetUniformLocation(spriteShaderProgram, "uTexture");
        textureCoordHandle = GLES20.glGetAttribLocation(spriteShaderProgram, "aTexCoord");
        
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, obj.getTextureDataHandle());
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);
        
        
        obj.getTexCoordBuffer().position(0);
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false,
         0, obj.getTexCoordBuffer());
        
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        
        //Enable alpha channel
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND); 
        
        //Draw the vertices
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, obj.getVertexCount());
        
        //Disable blend that was enabled above for alpha texturing.
        GLES20.glDisable(GLES20.GL_BLEND);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    /**
     * Adapted from Rodney Lambert's answer on 
     * http://stackoverflow.com/questions/16027455/what-is-the-easiest-way-to-draw-line-using-opengl-es-android
     * 
     * TODO: merge common code between this and draw(GroundObject)
     * @param groundPlane
     */
	public void drawGroundPlane(Ground ground) {
		float[] modelViewProjectionMat = new float[16];
		Matrix.multiplyMM(modelViewProjectionMat, 0, viewProjectionMat, 0, ground.getTransform(), 0);
		
		 // Add program to OpenGL ES environment
	    GLES20.glUseProgram(groundShaderProgram);

	    // get handle to vertex shader's vPosition member
	    positionHandle = GLES20.glGetAttribLocation(groundShaderProgram, "vPosition");

	    // Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(positionHandle);

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
	                                 GLES20.GL_FLOAT, false,
	                                 ground.getVertexStride(), ground.getVertexBuffer());

	    // get handle to fragment shader's vColor member
	    colorHandle = GLES20.glGetUniformLocation(groundShaderProgram, "vColor");

	    // Set color for drawing the triangle
	    GLES20.glUniform4fv(colorHandle, 1, ground.getColor(), 0);

	    // get handle to shape's transformation matrix
	    mvpMatrixHandle = GLES20.glGetUniformLocation(groundShaderProgram, "uMvpMatrix");
	    checkGlError("glGetUniformLocation");

	    // Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionMat, 0);
	    checkGlError("glUniformMatrix4fv");


	    // Draw the triangle
	    GLES20.glDrawArrays(GLES20.GL_LINES, 0, ground.getVertexCount());

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(positionHandle);
	}

	/**
	 * TODO
	 * @param obj
	 * @return
	 */
	private boolean withinViewFrustum(GameObject obj) {
		
		//Get camera pos, top, bottom, left, right planes
		float camX = -scene.getCameraPosition().x;
		float camY = scene.getCameraPosition().y;
		float camZ = -FAR_PLANE_AND_EYE_Z_POS;
		
		Pointf cameraPos = new Pointf(camX, camY, camZ);
		
		float lookAtX = -scene.getCameraPosition().x;
		float lookAtY = scene.getCameraPosition().y;
		float lookAtZ = 0f;
		
		float upX = 0f; 
		float upY = 1f;
		float upZ = 0f;
		
		Plane leftFrustumPlane = new Plane(
				new Pointf(camX - 1, camY, camZ), new Vectorf(-1, 0, 0));
		
		Plane rightFrustumPlane = new Plane(
				new Pointf(camX + 1, camY, camZ), new Vectorf(1,0,0));
		
		if (leftFrustumPlane.distToPoint(cameraPos) < 0) {
			return false;
		}
		
		if (rightFrustumPlane.distToPoint(cameraPos) < 0) {
			return false;
		}
		
		//TODO: Get up/down planes too
		
		return true;
	}
}



