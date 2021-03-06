package graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import util.Vec2f;

public class GraphicImage implements Graphic {
	
	private GLImage img;
	private float[] clipping = new float[] {
			0f,	// xStart
			0f,	// yStart
			1f,	// xEnd
			1f	// yEnd
	};
	private Vec2f offset = new Vec2f(0f, 0f);
	private boolean flipped = false;
	private Vec2f scale = new Vec2f(1f, 1f);
	
	public GraphicImage(GLImage _img, float[] _clipping, Vec2f _offset, boolean _flipped) {
		this.img = _img;
		this.clipping = _clipping;
		this.offset = _offset;
		this.flipped = _flipped;
	}
	
	public GraphicImage (String _filename) {
		this.img = new GLImage(_filename);
	}
	
	public GraphicImage (String _filename, float[] _clipping) {
		this.img = new GLImage(_filename);
		this.clipping = Arrays.copyOf(_clipping, 4);
	}
	
	public GraphicImage (String _filename, Vec2f _offset) {
		this.img = new GLImage(_filename);
		this.offset = _offset;
	}
	
	public GraphicImage (String _filename, Vec2f _offset, float[] _clipping) {
		this.img = new GLImage(_filename);
		this.offset = _offset;
		this.clipping = Arrays.copyOf(_clipping, 4);
	}
	
	public void setScale(Vec2f _scale) {
		this.scale = _scale.clone();
	}
	
	@Override
	public void flipX() {
		if (!this.flipped) {
			this.flipped = true;
			float xStart = this.clipping[0];
			float xEnd = this.clipping[2];
			this.clipping[0] = xEnd;
			this.clipping[2] = xStart;
		}
	}
	
	@Override
	public void unflipX() {
		if (this.flipped) {
			this.flipped = false;
			float xStart = this.clipping[0];
			float xEnd = this.clipping[2];
			this.clipping[0] = xEnd;
			this.clipping[2] = xStart;
		}
	}
	
	@Override
	public void reset() {
		// nothing to do here
	}
	
	@Override
	public boolean disposable() {
		return true;
	}
	
	@Override
	public void tick(int delta) {
		// nothing to do here
	}

	@Override
	public void render() {
		GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1); // transparent color for overlay
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.img.getID());
			
			GL11.glScalef(this.scale.x, this.scale.y, 1f);
			GL11.glTranslatef(this.offset.x, this.offset.y, 0f);
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(clipping[0], clipping[1]);
				GL11.glVertex2f(0, 0);
				
				GL11.glTexCoord2f(clipping[2], clipping[1]);
				GL11.glVertex2f(1, 0);
				
				GL11.glTexCoord2f(clipping[2], clipping[3]);
				GL11.glVertex2f(1, 1);

				GL11.glTexCoord2f(clipping[0], clipping[3]);
				GL11.glVertex2f(0, 1);
			GL11.glEnd();
		
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}
	
	@Override
	public Graphic clone() {
		return new GraphicImage(this.img, this.clipping.clone(), this.offset.clone(), this.flipped);
	}
	
	@Override
	public void destroy() {
		this.img.destroy();
		this.img = null;
		this.offset = null;
		this.clipping = null;
	}
}



class GLImage {
	private int id;
	private int width;
	private int height;
	
	public GLImage (String _filename) {
		try {
			BufferedImage image = ImageIO.read(this.getClass().getResource("/textures/" + _filename));
			int[] pixels = new int[image.getWidth() * image.getHeight()];
		    image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		    ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB
		    for(int y = 0; y < image.getHeight(); y++){
		        for(int x = 0; x < image.getWidth(); x++){
		            int pixel = pixels[y * image.getWidth() + x];
		            buffer.put((byte) ((pixel >> 16) & 0xFF));	// red
		            buffer.put((byte) ((pixel >> 8) & 0xFF));	// green
		            buffer.put((byte) (pixel & 0xFF));			// blue
		            buffer.put((byte) ((pixel >> 24) & 0xFF));	// alpha
		        }
		    }
		    buffer.flip();
		    
		    this.id = GL11.glGenTextures(); //Generate texture ID
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id); //Bind texture ID
		    //Setup wrap mode
		    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		    //Setup texture scaling filtering
		    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);	        
		    //Send texel data to OpenGL
		    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
			
		    this.width = image.getWidth();
		    this.height = image.getHeight();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getID () {
		return this.id;
	}
	
	public int getWidth () {
		return this.width;
	}
	
	public int getHeight () {
		return this.height;
	}
	
	public void destroy() {
		//TODO clear image in OpenGL
	}
	
}