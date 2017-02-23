package objects;

public class Plane {
	public Pointf point;
	public Vectorf normal;
	
	public Plane(Pointf point, Vectorf normal) {
		this.point = point;
		this.normal = normal;
	}
	
	public float distToPoint(Pointf p) {
		return 0;		
	}
}
