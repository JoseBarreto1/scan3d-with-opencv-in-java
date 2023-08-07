package mainOpenCv;

import org.opencv.core.Mat;


public class OpenCVGLTexture{
		private OpenCVImageInGL auxTex;
		private int tex_id;
		private double twr,thr,aspect_w2h;
		Mat image, tex_img;
			
		public OpenCVGLTexture(){
			setId(-1);
			setTwr(1.0);
			setThr(1.0);			
		}
		
		void setTexture(Mat image) { 
			image.clone(); 
			//auxTex.copyImgToTex(image, tex_id, twr, thr); 
			setW2h((double)image.cols()/(double)image.rows());
		}
		
		public void setId(int id){
			this.tex_id = id;
		}
		public void setTwr(double tw){
			this.twr = tw;
		}
		public void setThr(double th){
			this.thr = th;
		}
		public void setW2h(double aspect){
			this.aspect_w2h = aspect;
		}
		
		public int getId(){
			return tex_id;
		}
		public double getTwr(){
			return twr;
		}
		public double getThr(){
			return thr;
		}
		public double getW2h(){
			return aspect_w2h;
		}
}
