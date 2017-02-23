package physics;

import android.util.FloatMath;
import android.util.Log;
import game.Scene;
import objects.GameObject;
import objects.Ground;
import objects.Pointf;
import objects.Vectorf;

public class PhysicsHandler {
	
	public static final float GRAVITY_EARTH = 9.81f;
	public static final float TERMINAL_Y_VELOCITY = 4;
	private static final float GROUND_FRICTION = 0.15f;
	private static final float MIN_ROTATION_DEGREES = 1f;
	
	private static final PhysicsHandler _instance = new PhysicsHandler();
	
	private PhysicsHandler() {}
	
	public static PhysicsHandler getInstance() {
		return _instance;
	}
	
	public void update(GameObject obj, Scene scene) {
		Log.d("PhysicsHandler", "Bob at position " + obj.getPosition().x  + ", " + obj.getPosition().y);
		
		if (objHasHitGround(obj, scene)) {
			resolveCollisions(obj, scene);
		}
		if (!obj.isBeingDragged()) {
			Vectorf accel = calculateAcceleration(obj, scene.getGroundPlane());
			incrementVelocity(obj, accel);
			obj.move();
			adjustRotation(obj, scene);
		}
		if (objHasHitGround(obj, scene)) {
			resolveCollisions(obj, scene);
		}
	}

	/**
	 * Impulse collision resolution based on 
	 * http://gamedevelopment.tutsplus.com/tutorials/how-to-create-a-custom-2d-physics-engine-the-basics-and-impulse-resolution--gamedev-6331
	 */
	private void resolveCollisions(GameObject obj, Scene scene) {
		Ground ground = scene.getGroundPlane();
		Pointf objPosition = obj.getPosition();
		float groundPlaneY = ground.getYPositionAt(objPosition.x);
		Vectorf groundNormal = ground.getNormalForAccelCalc(objPosition);
		float inverseObjMass = 1 / obj.getMass();
		
		Vectorf objVelocity = obj.getVelocity();
		float velocityAlongNormal = objVelocity.dotProduct(groundNormal);
		
		if (velocityAlongNormal <= 0) {		//Going into the ground	
			float restitution = 0f;
			
			float impulseScalar = -(1 + restitution) * velocityAlongNormal;
			impulseScalar /= inverseObjMass;
			
			//Apply impulse
			Vectorf impulse = groundNormal.multiply(impulseScalar);
			Vectorf velocityChange = impulse.multiply(inverseObjMass);
			obj.setVelocity(objVelocity.add(velocityChange));
		}
		
		//TODO: linear projection position correction (see tutorial). Should avoid the stuttering effect.
		obj.setPosition(new Pointf(objPosition.x, groundPlaneY));
	}
	
	private boolean objHasHitGround(GameObject obj, Scene scene) {
		return scene.getGroundPlane().collidesWith(obj);
	}

	private Vectorf calculateAcceleration(GameObject obj, Ground ground) {
		float mass = obj.getMass();
		Vectorf gravityDirection = new Vectorf(0, -1, 0);
		float forceGravity = GRAVITY_EARTH * mass;
		Vectorf forceGravityVec = gravityDirection.multiply(forceGravity);
		Vectorf accel;
		
		if (!ground.collidesWith(obj)) {
			accel = forceGravityVec;
		} else {
			Vectorf groundNormal = ground.getNormalForAccelCalc(obj.getPosition());
			groundNormal.normalise();
			
			Vectorf vecPerp = groundNormal.invert();
			vecPerp.normalise();
			
			Vectorf forwardDirection = groundNormal.getPerpendicular();
			forwardDirection.x = -forwardDirection.x;
			forwardDirection.normalise();
			
			Vectorf backwardDirection = forwardDirection.invert();
			forwardDirection.normalise();
			
			if (groundNormal.x < 0) {
				Vectorf temp = new Vectorf(backwardDirection);
				backwardDirection = forwardDirection;
				forwardDirection = temp;
			}
			
			float theta = gravityDirection.angleTo(vecPerp);
			
			float forceForward = forceGravity * FloatMath.sin(theta);
			Vectorf forceForwardVec = forwardDirection.multiply(forceForward);
			
			float groundNormalForce = forceGravity * FloatMath.cos(theta);
			Vectorf groundNormalForceVec = groundNormal.multiply(groundNormalForce);
			
			//TODO: use previous frame's accel to calc friction and then slow us down
			float forceOfFriction = groundNormalForce * GROUND_FRICTION;
			Vectorf forceOfFrictionVec = backwardDirection.multiply(forceOfFriction);

			Vectorf forceNet = forceGravityVec.add(forceForwardVec).add(groundNormalForceVec).add(forceOfFrictionVec);
			
			accel = forceNet.divide(mass);
		}
		return accel;
	}

	private void incrementVelocity(GameObject obj, Vectorf accel) {
		Vectorf currentObjVelocity = obj.getVelocity();
		//Scale down accel as it's currently added to obj velocity per frame, not per sec
		//TODO: calculate scale factor based on FPS
		Vectorf increment = accel.multiply(0.05f);
		if (accel.y == 0 && currentObjVelocity.y < 0) {
			currentObjVelocity.y = 0;
		}
		
		if (currentObjVelocity.x >= 0 && accel.y == 0 && currentObjVelocity.x + accel.x < 0) {
			obj.setVelocity(new Vectorf(0,0,0));
			obj.setAcceleration(new Vectorf(0,0,0));
		} else {
			obj.setVelocity(currentObjVelocity.add(increment));
			obj.setAcceleration(accel);
		}
	}

	/**
	 * Rotate obj according to normal of ground plane at point of collision
	 * @param obj
	 * @param scene
	 */
	public void adjustRotation(GameObject obj, Scene scene) {
		Ground g = scene.getGroundPlane();
		if (!objHasHitGround(obj, scene)) {
			return;	//TODO: continue previous level of rotation. inertia?
		} else {
			Vectorf groundNormal = g.getNormalAt(obj.getPosition());
			Vectorf upDir = obj.getUp();
			upDir.normalise();
			
			float rotationAngle = groundNormal.getShortestAngleTo(upDir);
			
			if (Math.abs(rotationAngle) > MIN_ROTATION_DEGREES) {
				obj.rotate(rotationAngle);
			}
		}
	}
}
