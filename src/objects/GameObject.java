package objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import display.MyRenderer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

public class GameObject {

	private static final int COORDS_PER_VERTEX = 3;
	private static final int BYTES_PER_FLOAT = 4;

	private FloatBuffer vertexBuffer;
	private FloatBuffer texCoordBuffer;
	private int textureDataHandle;
	protected float[] textureCoords = { 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f };

	protected float[] _transformMat;
	private float[] colour = { 0, 0, 0 };
	float objectCoords[] = { -0.5f, 0.5f, 0, -0.5f, -0.5f, 0, 0.5f, 0.5f, 0,
			-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0 };

	private Vectorf velocity;
	private Vectorf acceleration;

	private boolean isBeingDragged;

	public Vectorf getUp() {
		return new Vectorf(_transformMat[4], _transformMat[5], _transformMat[6]);
	}

	public Vectorf getForward() {
		return new Vectorf(_transformMat[0], _transformMat[1], _transformMat[2]);
	}

	public void setForward(Vectorf forward) {
		// TODO: rotate so forward is pointing in $forward direction
	}

	public GameObject() {
		this.velocity = new Vectorf(0, 0, 0);
		this.acceleration = new Vectorf(0, 0, 0);

		_transformMat = new float[16];
		Matrix.setIdentityM(_transformMat, 0);

		initialiseBuffers();
	}

	public void initialiseBuffers() {
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(objectCoords.length
				* BYTES_PER_FLOAT);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(objectCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		texCoordBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		texCoordBuffer.put(textureCoords).position(0);
	}

	/**
	 * Adapted from
	 * http://www.learnopengles.com/android-lesson-four-introducing-
	 * basic-texturing/
	 * 
	 * @param context
	 * @param resourceId
	 * @return
	 */
	public int loadTexture(final String fileName) {
		final Context context = MyRenderer.getInstance().getAppContext();

		int resourceId = context.getResources().getIdentifier(fileName,
				"drawable", context.getPackageName());

		if (resourceId == 0) {
			// could not find resourceId for
		}
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture");
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false; // No prescaling

		// read in resource
		final Bitmap bitmap = BitmapFactory.decodeResource(
				context.getResources(), resourceId);

		// Bind to texture in OpenGL - does this mean binding to shader?
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

		// set filtering
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Recycle the bitmap, since its data has been loaded into OpenGL.
		bitmap.recycle();

		this.textureDataHandle = textureHandle[0];

		return textureHandle[0];
	}

	public void translate(float x, float y, float z) {
		float[] translationMat = new float[16];
		Matrix.setIdentityM(translationMat, 0);
		Matrix.translateM(translationMat, 0, x, y, z);

		Matrix.multiplyMM(_transformMat, 0, translationMat, 0, _transformMat, 0);
	}

	public void scale(float x, float y, float z) {
		float[] scaleMat = new float[16];
		Matrix.setIdentityM(scaleMat, 0);
		Matrix.scaleM(scaleMat, 0, x, y, z);

		Matrix.multiplyMM(_transformMat, 0, scaleMat, 0, _transformMat, 0);
	}

	private void glRotate(float angle, float x, float y, float z) {
		float[] rotationMat = new float[16];
		Matrix.setIdentityM(rotationMat, 0);
		Matrix.setRotateM(rotationMat, 0, angle, x, y, z);

		Matrix.multiplyMM(_transformMat, 0, rotationMat, 0, _transformMat, 0);
	}

	/**
	 * "Real rotation" - move object to origin before rotating then returning to
	 * prev. pos.
	 * 
	 * @param obj
	 * @param angle
	 */
	public void rotate(float angle) {
		float[] savedObjTransform = _transformMat.clone();
		float translationX = savedObjTransform[12];
		float translationY = savedObjTransform[13];

		translate(-translationX, -translationY, 0); // Move to the origin
		glRotate(angle, 0, 0, 1); // Apply the rotation

		translate(translationX, translationY, 0); // Move obj back to where it was before
	}

	public void setObjectCoords(float[] objectCoords) {
		this.objectCoords = objectCoords;
	}

	public void setColour(float[] colour) {
		this.colour = colour;
	}

	public void resetTransform() {
		Matrix.setIdentityM(_transformMat, 0);
	}

	public float[] getTransform() {
		return this._transformMat;
	}

	public FloatBuffer getVertexBuffer() {
		return this.vertexBuffer;
	}

	public FloatBuffer getTexCoordBuffer() {
		return this.texCoordBuffer;
	}

	public int getVertexCount() {
		return this.objectCoords.length / COORDS_PER_VERTEX;
	}

	public int getVertexStride() {
		return COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	}

	public int getTextureDataHandle() {
		return this.textureDataHandle;
	}

	public float[] getColour() {
		return this.colour;
	}

	public Pointf getPosition() {
		return new Pointf(-_transformMat[12], _transformMat[13]);
	}

	public void setPosition(Pointf pos) {
		_transformMat[12] = -pos.x;
		_transformMat[13] = pos.y;
	}

	public void move() {
		translate(-velocity.x * 0.05f, velocity.y * 0.05f, 0);
	}

	public float getMass() {
		return 1;
	}
	
	public Vectorf getAcceleration() {
		return this.acceleration;
	}
	
	public void setAcceleration(Vectorf acceleration) {
		this.acceleration = acceleration;
	}

	public Vectorf getVelocity() {
		return this.velocity;
	}

	public void setVelocity(Vectorf newVelocity) {
		this.velocity = newVelocity;
	}

	public boolean isBeingDragged() {
		return this.isBeingDragged;
	}

	public void startDragging() {
		setVelocity(new Vectorf(0, 0, 0));
		this.isBeingDragged = true;
	}

	public void endDragging() {
		this.isBeingDragged = false;
	}
}