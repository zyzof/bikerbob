package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.FloatMath;
import objects.Ground;
import objects.Pointf;

public class GroundGenerator {
	
	private static final float GROUND_LENGTH = 5f;
	private static final float MAX_DIST_TO_NEXT_JUMP = 10f;
	private static final float MAX_JUMP_ANGLE = 45f;
	private static final float MAX_JUMP_DISTANCE = 3f;
	private static final float MAX_SLOPE_DISTANCE = 1f;
	private static final float PIT_BOTTOM = -1000f;
	
	public GroundGenerator() {
		
	}

	public Ground generateGround(Pointf startPoint) {
		List<Pointf> groundPoints = generateGroundPoints(startPoint);
		return new Ground(groundPoints);
	}
	
	public List<Pointf> generateGroundPoints(Pointf startPoint) {
		List<Pointf> groundPoints = new ArrayList<Pointf>();
		
		float distanceFromStart = 0;
		Random rand = new Random();
		
		Pointf prevPoint = startPoint;
		groundPoints.add(prevPoint);
		while (distanceFromStart < GROUND_LENGTH) {
			float distanceToNextJump = 5 + rand.nextFloat() * MAX_DIST_TO_NEXT_JUMP;
			float jumpAngle = rand.nextFloat() * MAX_JUMP_ANGLE;
			float jumpGapDistance = 1.5f + rand.nextFloat() * MAX_JUMP_DISTANCE;
			float jumpSlopeDistance = 1.5f + rand.nextFloat() * MAX_SLOPE_DISTANCE;
			
			Pointf startOfJump = new Pointf(prevPoint.x + distanceToNextJump, 0);
			float jumpHeight = (float) (jumpSlopeDistance * Math.tan(jumpAngle * Math.PI / 180));
			Pointf endOfJumpTop = new Pointf(startOfJump.x + jumpSlopeDistance, jumpHeight);
			Pointf endOfJumpBottom = new Pointf(endOfJumpTop.x, PIT_BOTTOM);
			Pointf startOfLandingBottom = new Pointf(endOfJumpBottom.x + jumpGapDistance, PIT_BOTTOM);
			Pointf startOfLandingTop = new Pointf(startOfLandingBottom.x, endOfJumpTop.y);
			Pointf endOfLanding = new Pointf(startOfLandingTop.x + jumpSlopeDistance, 0);
			
			groundPoints.add(startOfJump);
			groundPoints.add(endOfJumpTop);
			groundPoints.add(endOfJumpBottom);
			groundPoints.add(startOfLandingBottom);
			groundPoints.add(startOfLandingTop);
			groundPoints.add(endOfLanding);
			
			distanceFromStart = endOfLanding.x;
			prevPoint = endOfLanding;
		}
		
		return groundPoints;
	}

}
