package mainOpenCv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

public class OpenCVImageInGL implements GLEventListener {

	//public static DisplayMode dm, dm_old;
		private GLU glu = new GLU();
		private float rquad = 0.0f;
		private int texture;
	
	//---------------FUNÇOES OBRIGATORIAS JOGL----------------------------------
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
	
	
	
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
	}
		
}
