import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;


public interface Entity {
	public Vec2 getPosition();
	public BodyDef getBody();
	public Graphic getGraphic();
	
	public void tick(int delta);
	public void render();
}
