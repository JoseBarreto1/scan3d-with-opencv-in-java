package mainOpenCv;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class MainCvinGL {	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile); //
		
		// The canvas
		final GLCanvas glcanvas = new GLCanvas(capabilities);

		OpenCVImageInGL opencvGL = new OpenCVImageInGL();
		
		glcanvas.addGLEventListener(opencvGL);
		glcanvas.setSize(800, 800);
		
		//Creating Frame
		//Creating Frame
		final Frame frame =  new Frame("head pose");
				
		frame.add(glcanvas);
		frame.setSize(800,800);
		frame.setVisible(true);
		frame.setLocation (40,40);
		
		
		final FPSAnimator animator = new FPSAnimator(glcanvas, 10,true);
		
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
}

