package objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import android.annotation.TargetApi;
import android.opengl.Matrix;
import android.util.Log;

@TargetApi(9)
public class Ground {
	
	private static final int COORDS_PER_VERTEX = 3;
	private static final int BYTES_PER_FLOAT = 4;
	
	private FloatBuffer vertexBuffer;
	
    protected float[] _transformMat;
    private float[] colour = {0, 0, 0}; 
	
    private Deque<Pointf> _groundPoints;
	private float[] _objectCoords;

	public Ground(List<Pointf> groundPoints) {
		_transformMat = new float[16];
		Matrix.setIdentityM(_transformMat, 0);
		
		_groundPoints = new LinkedList<Pointf>();
		_groundPoints.addAll(groundPoints);
		
		_objectCoords = getGlCoordsFromPoints(groundPoints);
    	
		initialiseBuffers();
		translate(0, -0.5f, 0);
	}
	
	public Pointf getLastPoint() {
		return _groundPoints.getLast();
	}

	private float[] getGlCoordsFromPoints(Collection<Pointf> groundPoints) {
		//First and last points only used once, everything else twice
		float[] glCoords = new float[((groundPoints.size() - 2) * 6) + 6];	
		
		int i = 0;
		for (Pointf groundPoint : groundPoints) {
			glCoords[i] = -groundPoint.x;
			glCoords[i+1] = groundPoint.y;
			glCoords[i+2] = groundPoint.z;
			if (i > 0 && i < glCoords.length - 4) {
				glCoords[i+3] = -groundPoint.x;
				glCoords[i+4] = groundPoint.y;
				glCoords[i+5] = groundPoint.z;
				i += 6;
			} else {
				i += 3;
			}
		}
		
		return glCoords;
	}

	/**
	 * Solution taken from http://answers.unity3d.com/questions/33223/finding-point-on-the-vector.html
	 * @param x
	 * @return
	 * @throws Exception 
	 */
	public float getYPositionAt(float x) {
		Pointf[] surroundingGroundPoints = getGroundPointsSurrounding(x);
		
		if (surroundingGroundPoints == null) {
			return Float.NEGATIVE_INFINITY;
		}
		Pointf prev = surroundingGroundPoints[0];
		Pointf next = surroundingGroundPoints[1];
			
		if (prev.x <= x && next.x >= x) {
			Vectorf vec1 = new Vectorf(next, prev);
			
			float proportionOfDistanceAlongPrev = Math.abs((x - prev.x) / (next.x - prev.x));
			
			Vectorf vecScaledByPropAlongVec1 = 
					new Vectorf(vec1.x * proportionOfDistanceAlongPrev, 
								vec1.y * proportionOfDistanceAlongPrev, 
								vec1.z * proportionOfDistanceAlongPrev);
			
			Vectorf v2 = new Vectorf(prev.x + vecScaledByPropAlongVec1.x, 
									 prev.y + vecScaledByPropAlongVec1.y,
									 prev.z + vecScaledByPropAlongVec1.z);
			
			return v2.y;
		}
		
		return Float.MAX_VALUE;//Shouldn't get here. Scene not defined
	}
	
	public boolean collidesWith(GameObject player) {
		return player.getPosition().y <= getYPositionAt(player.getPosition().x);
	}
	
	public void translate(float x, float y, float z) {
		float[] translationMat = new float[16];
		Matrix.setIdentityM(translationMat, 0);
		Matrix.translateM(translationMat, 0, x, y, z);
		
		Matrix.multiplyMM(_transformMat, 0, translationMat, 0, _transformMat, 0);
	}
	
	private Pointf[] getGroundPointsSurrounding(float x) {
		for (int i = 0; i < _objectCoords.length - 8; i += 6) {
			
			//Note that x has been negated!!! Higher x values to the right.
			Pointf prev = new Pointf(-_objectCoords[i], _objectCoords[i+1], _objectCoords[i+2]);
			Pointf next = new Pointf(-_objectCoords[i+6], _objectCoords[i+7], _objectCoords[i+8]);	//Start of next line segment
			
			if (prev.x <= x && next.x >= x) {
				return new Pointf[] { prev, next };
			}
		}
		
		return null;
	}
	
	/**
	 * <Shudder>....
	 * @param position
	 * @return
	 */
	public Vectorf getNormalForAccelCalc(Pointf position) {
		Vectorf temp = getNormalAt(position);
		return new Vectorf(-temp.x, temp.y, 0);
	}

	public Vectorf getNormalAt(Pointf position) {
		
		Pointf[] surroundingGroundPoints = getGroundPointsSurrounding(position.x);
		Log.d("Ground.java: ", String.format("%s: (%s,%s), (%s,%s)", "surroundingGroundPoints" ,
																		surroundingGroundPoints[0].x,
																		surroundingGroundPoints[0].y,
																		surroundingGroundPoints[1].x,
																		surroundingGroundPoints[1].y));
		
		float x1 = surroundingGroundPoints[0].x;
		float x2 = surroundingGroundPoints[1].x;
		
		float y1 = surroundingGroundPoints[0].y;
		float y2 = surroundingGroundPoints[1].y;
		
		Vectorf groundLineSegment = new Vectorf(new Pointf(x1, y1), new Pointf(x2, y2));
		Vectorf normal = groundLineSegment.getPerpendicular();
		normal.normalise();
		
		Log.d("Ground.java: ", "(" + normal.x + "," + normal.y + ")");
		
		return normal;
	}
	
	public int getVertexCount() {
		return this._objectCoords.length / COORDS_PER_VERTEX;
	}
	
	public int getVertexStride() {
		return COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	}

	public FloatBuffer getVertexBuffer() {
		return vertexBuffer;
	}

	public float[] getColor() {
		return colour;
	}
	
	private void initialiseBuffers() {
		// initialize vertex byte buffer for shape coordinates
	    ByteBuffer bb = ByteBuffer.allocateDirect(_objectCoords.length * BYTES_PER_FLOAT);
	    // use the device hardware's native byte order
	    bb.order(ByteOrder.nativeOrder());
	
	    // create a floating point buffer from the ByteBuffer
	    vertexBuffer = bb.asFloatBuffer();
	    // add the coordinates to the FloatBuffer
	    vertexBuffer.put(_objectCoords);
	    // set the buffer to read the first coordinate
	    vertexBuffer.position(0);
	}

	public float[] getTransform() {
		return _transformMat;
	}

	public void addPoints(List<Pointf> points, Pointf currentObjPos) {
		_groundPoints.addAll(points);
		while(_groundPoints.getFirst().x < currentObjPos.x - 15) {	//TODO: this is currently just some dumb number to make sure we don't drop line segments while they're still on screen.
			_groundPoints.removeFirst();
		}
		
		_objectCoords = getGlCoordsFromPoints(_groundPoints);
		initialiseBuffers();
	}
}
