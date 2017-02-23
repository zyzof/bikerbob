package objects;

public class BoundingBox {
	public Pointf topLeft;
	public Pointf topRight;
	public Pointf bottomLeft;
	public Pointf bottomRight;
	
	/**
	 * 
	 * @param obj
	 * @param size (Should really come directly from the object. TODO)
	 */
	BoundingBox(GameObject obj, float size) {
		Pointf pos = obj.getPosition();
		Vectorf upDir = obj.getUp();
		Vectorf forwardDir = obj.getForward();
		upDir.normalise();
		forwardDir.normalise();
		Pointf topCenter = new Pointf(pos.x + (upDir.x * size),
									  pos.y + (upDir.y * size),
									  0);
		Pointf bottomCenter = new Pointf(pos.x + (-upDir.x * size),
										 pos.y + (-upDir.y * size),
										 0);
		topRight = new Pointf(topCenter.x + (forwardDir.x * size),
									 topCenter.y + (forwardDir.y * size),
									 0);
		topLeft = new Pointf(topCenter.x + (-forwardDir.x * size),
									topCenter.y + (-forwardDir.y * size), 
									0);
		bottomRight = new Pointf(bottomCenter.x + (forwardDir.x * size),
										bottomCenter.y + (forwardDir.y * size),
										0);
		bottomLeft = new Pointf(bottomCenter.x + (-forwardDir.x * size),
									   bottomCenter.y + (-forwardDir.y * size),
									   0);
	}
}
