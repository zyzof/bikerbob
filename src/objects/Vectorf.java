package objects;

import android.opengl.Matrix;
import android.util.FloatMath;

public class Vectorf {
	public float x, y, z;
	private boolean isNormalised;
	
	public Vectorf(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vectorf(Pointf p1, Pointf p2) {
		this.x = p1.x - p2.x;
		this.y = p1.y - p2.y;
	}
	
	public Vectorf(Vectorf other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	/**
	 * In-place vector normalisation
	 */
	public void normalise() {
		float magnitude = magnitude();
		this.x = ((this.x == 0) ? 0 : this.x / magnitude);
		this.y = ((this.y == 0) ? 0 : this.y / magnitude);
		this.z = ((this.z == 0) ? 0 : this.z / magnitude);
		
		this.isNormalised = true;
	}
	
	public Vectorf getPerpendicular() {
		return new Vectorf(-this.y, -this.x, 0);
	}
	
	public float magnitude() {
		return FloatMath.sqrt((x*x) + (y*y) + (z*z));
	}

	public Vectorf() { }

	
	public Vectorf(Vectorf upDir, Vectorf groundNormal) {
		this.x = upDir.x - groundNormal.x;
		this.y = upDir.y - groundNormal.y;
	}

	/**
	 * cos(theta) = vec1.dot(vec2);
	 */
	public float angleTo(Vectorf other) {
		if (!this.isNormalised()) {
			this.normalise();
		}
		
		if (!other.isNormalised()) {
			other.normalise();
		}
		float dotProduct = dotProduct(other);
		float angleRadians = (float)Math.acos(dotProduct);
		float angleDegrees = (float)(angleRadians * 180 / Math.PI);
		
		return angleDegrees;
	}
	
	public float getShortestAngleTo(Vectorf other) {
		float angle = this.angleTo(other);;
		
		Vectorf rotateDir = new Vectorf(other, this);
		
		if (rotateDir.x < 0) {	//Angle to the left
			angle = -angle;
		}
		
		return angle;
	}
	
	public float dotProduct(Vectorf vec) {
		float x1 = this.x;
		float x2 = vec.x;
		float y1 = this.y;
		float y2 = vec.y;
		float z1 = this.z;
		float z2 = vec.z;
		
		return (x1 * x2) + (y1 * y2) + (z1 * z2);
	}
	
	public boolean isNormalised() {
		return isNormalised;
	}

	public void setNormalised(boolean isNormalised) {
		this.isNormalised = isNormalised;
	}
	
	public Vectorf add(Vectorf other) {
		return new Vectorf(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public Vectorf subtract(Vectorf other) {
		return new Vectorf(this.x - other.x, this.y - other.y, this.z - other.z);
	}
	
	public Vectorf multiply(float scalar) {
		return new Vectorf(this.x * scalar, this.y * scalar, this.z * scalar);
	}
	
	public Vectorf divide(float scalar) {
		return new Vectorf(this.x / scalar, this.y / scalar, this.z / scalar);
	}
	
	public Vectorf invert() {
		return new Vectorf(-this.x, -this.y, -this.z);
	}

	public void rotate(float angle, int x, int y, int z) {
		float[] rotationMat = new float[16];
		float[] vectorMatrix = new float[4];
		vectorMatrix[0] = this.x;
		vectorMatrix[1] = this.y;
		vectorMatrix[2] = this.z;
		vectorMatrix[3] = 1;
		
		Matrix.setIdentityM(rotationMat, 0);
		
		Matrix.setRotateM(rotationMat, 0, angle, x, y, z);
		Matrix.multiplyMV(vectorMatrix, 0, rotationMat, 0, vectorMatrix, 0);
		
		this.x = vectorMatrix[0];
		this.y = vectorMatrix[1];
		this.z = vectorMatrix[2];
	}
}
