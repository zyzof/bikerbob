package game;

import java.util.ArrayList;
import java.util.List;

import display.MyRenderer;
import objects.GameObject;
import objects.Ground;
import objects.Pointf;
import physics.PhysicsHandler;

public class Scene {
	private MyRenderer renderer;
	private List<GameObject> gameObjects;
	public static final int FLOOR_VAL_Y_IN_GRID_COORDS = 5;
	
	private GameObject player;
	private GroundGenerator groundGen;
	private Ground ground;
	private GameObject background;
	private GameObject foreground;
	
	private PhysicsHandler physicsHandler;
	
	public Scene() {
		renderer = MyRenderer.getInstance();
		gameObjects = new ArrayList<GameObject>();
		physicsHandler = PhysicsHandler.getInstance();
		groundGen = new GroundGenerator();
		
		createBackground();
		createForeground();
		createGround();
		createPlayer();
	}
	
	private void createPlayer() {
		player = new GameObject();
		player.loadTexture("biker");
		player.translate(0f, 1f, 0);
		gameObjects.add(player);
	}
	
	private void createBackground() {
		background = new GameObject();
		background.loadTexture("background1");
		background.scale(10, 10, 10);	//TODO: scale based on screen height/width so it fills screen
		background.translate(0, 4.5f, 0);	//1/2 bg image height - 0.5 (due to ground being 0.5 below player center)
		gameObjects.add(background);
	}
	
	private void createForeground() {
		foreground = new GameObject();
		foreground.loadTexture("foreground1");
		foreground.scale(100, 10, 10);	//TODO: scale based on screen height/width so it fills screen
		foreground.translate(0, -5.5f, 0);	//1/2 bg image height + 0.5 (due to ground being 0.5 below player center)
		gameObjects.add(foreground);
	}
	
	private void createGround() {
		ground = groundGen.generateGround(new Pointf(0,0,0));
	}
	
	public void draw() {		
		for (int i = 0; i < gameObjects.size(); i++) {
			GameObject obj = gameObjects.get(i);
			
			renderer.draw(obj);
		}
		renderer.drawGroundPlane(ground);
	}
	
	public void doCurrentFrame() {
		try{
			Thread.sleep(100);
		} catch (Exception e){}
        physicsHandler.update(player, this);
		draw();
		
		Pointf lastGroundPoint = ground.getLastPoint();
		if (player.getPosition().x > lastGroundPoint.x - 10) {
			ground.addPoints(groundGen.generateGroundPoints(ground.getLastPoint()), player.getPosition());
		}
	}

	public Pointf getCameraPosition() {
		float DIST_FROM_OBJ_CENTRE_TO_LEFT_SCREEN_EDGE = 1.5f;
		return new Pointf(player.getPosition().x + DIST_FROM_OBJ_CENTRE_TO_LEFT_SCREEN_EDGE, player.getPosition().y);
	}

	public Ground getGroundPlane() {
		return ground;
	}
	
	public GameObject getPlayer() {
		return this.player;
	}
}
