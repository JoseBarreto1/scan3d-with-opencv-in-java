import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;


public class BasicFrame implements GLEventListener {

	//public static DisplayMode dm, dm_old;
	private GLU glu = new GLU();
	private float rquad = 0.0f;
	private int texture;
	
	@Override
	public void display(GLAutoDrawable drawable) {

	      // TODO Auto-generated method stub
	      final GL2 gl = drawable.getGL().getGL2();
	      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	      gl.glLoadIdentity(); // Reset The View
	      gl.glTranslatef(0f, 0f, -5.0f);
			
	   // Rotate The Cube On X, Y & Z
	      gl.glRotatef(rquad, 0.0f, 1.0f, 0.0f); 
	 
	      //giving different colors to different sides
	      gl.glBegin(GL2.GL_LINES); // Start Drawing The Cube
	      gl.glColor3f(1f,0f,0f); //red color
	      for(float i = -100; i < 100; i=i+0.1f){
	    	  float x1 = i*i;
	    	  float j = i+0.1f;
	    	  float x2 = j*j;
	    	  gl.glVertex3f(i, x1, 0.0f); // Top Right Of The Quad (Top)
		      gl.glVertex3f(j, x2, 0.0f); // Top Left Of The Quad (Top)
	      }
	      gl.glEnd();

	      gl.glBegin(GL2.GL_LINES); // Start Drawing The Cube
	      gl.glColor3f( 1f,1f,1f ); //purple (red + green)
	      float E1 = 100*100;
	      gl.glVertex3f( 0f, -10.0f, 0.0f ); // Top Right Of The Quad (Left)
	      gl.glVertex3f( 0f, E1, 0.0f ); // Top Left Of The Quad (Left)
	      
	      gl.glVertex3f(-100.0f, 0.0f, 0.0f ); // Top Right Of The Quad (Left)
	      gl.glVertex3f( 100.0f, 0.0f, 0.0f ); // Top Left Of The Quad (Left)
	          
	      gl.glEnd(); // Done Drawing The Quad
	      gl.glFlush();
			
	      rquad -= 0.5f;
	   }

	@Override
	public void init(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		final GL2 gl = arg0.getGL().getGL2();
		
	      gl.glShadeModel( GL2.GL_SMOOTH );
	      gl.glClearColor( 0f, 0f, 0f, 0f );
	      gl.glClearDepth( 1.0f );
	      gl.glEnable( GL2.GL_DEPTH_TEST );
	      gl.glDepthFunc( GL2.GL_LEQUAL );
	      gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
	      
	      /*gl.glEnable(GL2.GL_TEXTURE_2D);
	      try{
			
	         File im = new File("E:\\office\\boy.jpg ");
	         Texture t = TextureIO.newTexture(im, true);
	         texture= t.getTextureObject(gl);
	          
	      }catch(IOException e){
	         e.printStackTrace();
	      }*/

	}

	@Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		  final GL2 gl = arg0.getGL().getGL2();
	      if(height <= 0)
	         height = 1;
				
	      final float h = (float) width / (float) height;
	      gl.glViewport(0, 0, width, height);
	      gl.glMatrixMode(GL2.GL_PROJECTION);
	      gl.glLoadIdentity();
			
	      glu.gluPerspective(45.0f, h, 1.0, 20.0);
	      gl.glMatrixMode(GL2.GL_MODELVIEW);
	      gl.glLoadIdentity();

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile); //
		
		// The canvas
		final GLCanvas glcanvas = new GLCanvas(capabilities); //
		BasicFrame basicFrame = new BasicFrame();	
		
		glcanvas.addGLEventListener(basicFrame);
		glcanvas.setSize(800, 800);
		
		//Creating Frame
		final Frame frame =  new Frame("head pose");
		
		frame.add(glcanvas);
		frame.setSize(800,800);
		frame.setVisible(true);
		frame.setLocation (40,40);
		
		
		final FPSAnimator animator = new FPSAnimator(glcanvas, 24,true);
		
		animator.start();
		//Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		//frame.setSize(dimension);
	    frame.addWindowListener(new WindowAdapter() {			
	    	
	    	@Override
	    	public void windowClosing(WindowEvent e) {
	    		// TODO Auto-generated method stub
	    		frame.dispose();
	    	}
	    	
	    	@Override
	    	public void windowDeactivated(WindowEvent e) {
	    		animator.pause();
	    	}
	    	
	    	@Override
	    	public void windowActivated(WindowEvent e) {
	    		animator.resume();
	    	}
	    });
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
	//texture = textures[currTextureFilter].getTextureObject(gl);
    //------------Criando Textura---------------------------
    /*// Create a OpenGL Texture object
    for(int i=0;i<image.length;i++)
    {     
        textures[i] = AWTTextureIO.newTexture(GLProfile.getDefault(), image[i], false); 

        textureCoords = textures[i].getImageTexCoords();
        textureTop[i] = textureCoords.top();
        textureBottom[i] = textureCoords.bottom();
        textureLeft[i] = textureCoords.left();
        textureRight[i] = textureCoords.right();
        https://stackoverflow.com/questions/28230714/issue-in-rotating-3d-view-of-dicom-images-using-jogl
    }*/
	

	//gl.glBegin(GL2.GL_QUADS); // of the color cube  
   /* gl.glNormal3f(0.0f, 0.0f, (0.1f));
    gl.glTexCoord2f(textureLeft, textureBottom);
    gl.glVertex3f(-1.0f, -1.0f, (0.1f)); // bottom-left of the texture and quad
    gl.glTexCoord2f(textureRight, textureBottom);
    gl.glVertex3f(1.0f, -1.0f, (0.1f));  // bottom-right of the texture and quad
    gl.glTexCoord2f(textureRight, textureTop);
    gl.glVertex3f(1.0f, 1.0f, (0.1f));   // top-right of the texture and quad
    gl.glTexCoord2f(textureLeft, textureTop);
    gl.glVertex3f(-1.0f, 1.0f, (0.1f));  // top-left of the texture and quad
    */

}
