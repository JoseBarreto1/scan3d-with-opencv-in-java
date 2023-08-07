package mainOpenCv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class MainActivity {
    List<MatOfPoint> listSquares = new ArrayList<MatOfPoint>();
    List<MatOfPoint> listTriangles = new ArrayList<MatOfPoint>();
    List<Circle> centerTriangle = new ArrayList<Circle>();
    List<Circle> centerSquare = new ArrayList<Circle>();
    private String path;
    private Mat cameraMat;
    
    public MainActivity(String path) {
    	this.path = path;
    	this.cameraMat = getCameraMatrix();
    }


 public static void main(String[] args) throws InterruptedException{
	 System.loadLibrary (Core.NATIVE_LIBRARY_NAME);	
	 test0();
    }

    static void test0() {
        MainActivity cb = new MainActivity("/home/jose/Documentos/Opencv");
        cb.processImages();
    }

    void processImages(){
    	File outDir = new File(path + "/out/");
    	outDir.mkdir();

    	FilenameFilter filter = new FilenameFilter() {
    		@Override
    		public boolean accept(File dir, String name) {
    			return name.toUpperCase().endsWith("JPG") || name.toUpperCase().endsWith("JPEG") ;
    		}
    	};

    	int totalsqr = 0, totaltri = 0, total = 0;
    	for (File f : new File(path).listFiles(filter)) {
    		if(f.isFile()){
    			int lastIndexOfDot = f.getName().lastIndexOf(".");
    			String fileName = f.getName().substring(0, lastIndexOfDot);
    			String fileExtension = f.getName().substring(lastIndexOfDot);
    			Mat mat = Highgui.imread(f.getPath(), Highgui.CV_LOAD_IMAGE_COLOR);

    			for(int cannyMaxValue = 1; cannyMaxValue < 2; cannyMaxValue++)
    				for(int cannyMinValue = 1; cannyMinValue < 2; cannyMinValue++){
    					Mat matCanny = imgproc(mat, cannyMinValue, cannyMaxValue);
    					//Mat matDilate = dilate(matCanny);
    					//        		Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".matDilate" + fileExtension, matDilate);
    					Mat matcontours = findObjects(matCanny);
    					if(listSquares.size() == 2 && listTriangles.size() == 2){
    						totalsqr += listSquares.size();
    						totaltri += listTriangles.size();
    						//Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".matCanny" + fileExtension, matCanny);
    						//Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".matDilate" + fileExtension, matDilate);	
    						//System.out.println("(" + cannyMinValue +"," +  cannyMaxValue + ") SQR:" + listSquares.size() + " TRI:" + listTriangles.size() + ":" + f.getName());
    						total += 1;
    						Mat matStore = storesCentroid(mat);
    						Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".G(11, 5)-D(2, 2)" + fileExtension, matStore);
    					}
    					/*if(listSquares.size() > 1 && listTriangles.size() > 1){
						Mat matStore = storesCentroid(mat);
						project3d(mat);
						Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".matproject3d" + fileExtension, mat);
						Mat mask = maskImage(matDilate.clone());
						Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".matMask" + fileExtension, mask);				
					    }*/
    				}
    		}
    	}	
   		System.out.println("Total quadrados: " + totalsqr + "\nTotal triangulos: " + totaltri + "\nTotal:" + total);

    }
    
    public Mat imgproc(Mat mat, int minValue, int maxValue) {
    	Mat gray = new Mat( mat.height(), mat.width(), CvType.CV_8UC4);
        Mat canny = new Mat( mat.height(), mat.width(), CvType.CV_8UC4);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,1));
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(gray.clone(), canny, 250, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 5);
        //Imgproc.Canny(gray, canny, 30, 120); 
        Imgproc.dilate(canny.clone(), canny, element);
        //Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1));
        //Imgproc.erode(canny.clone(), canny, element1);
        //Imgproc.threshold(gray.clone(), threshold, 30, 255, Imgproc.THRESH_BINARY);
                
        return canny;
    }
    
    public Mat findObjects(Mat mat){
    	mat = mat.clone();
    	listSquares.clear();
    	listTriangles.clear();
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	MatOfPoint approx = new MatOfPoint();
    	
    	Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {

            double peri = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            approx = approxPolyDP(contours.get(i), 0.02 * peri, true);
			double area2 = Imgproc.contourArea(contours.get(i));

            if (approx.toArray().length == 3 && Math.abs(Imgproc.contourArea(approx)) > 150 && Math.abs(Imgproc.contourArea(approx)) < 2000) {
            	Point center1 = new Point();
            	float[] radius1 = new float[1];
            	Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center1, radius1);
            	float diameter = (2*radius1[0])/((float)Math.sqrt(2));
                double minArea = ((diameter*0.92f)*(diameter*0.92f))/2;
                double maxArea = ((diameter*1.08f)*(diameter*1.08f))/2;
                //System.out.println("Area real " + area2 + " min Area " + minArea + " max area " + maxArea);
                if(area2 > minArea && area2 < maxArea){
                	int id = 0;
                	if(listTriangles.size() == 0){
                		listTriangles.add(approx);
                	}
                	if(listTriangles.size() > 0){
                		float minx, maxx, miny, maxy;
                		minx = ((float)center1.x)*0.88f;
                		maxx = ((float)center1.x)*1.22f;
                		miny = ((float)center1.y)*0.88f;
                		maxy = ((float)center1.y)*1.22f;
                		Point center = new Point();
                		for(int j = 0; listTriangles.size() > j; j++){
                			Imgproc.minEnclosingCircle(new MatOfPoint2f(listTriangles.get(j).toArray()), center, new float[1]);
                			if((float)center.x > minx && (float)center.x < maxx && (float)center.y > miny && (float)center.y < maxy )
                				id = 1;
                		}
                	}
                	if(listTriangles.size() > 0 && id == 0){
                		listTriangles.add(approx);
                		id = 0;
                	}
                }
            }
            if (approx.toArray().length == 4 && Math.abs(Imgproc.contourArea(approx)) > 150 && Math.abs(Imgproc.contourArea(approx)) < 3000) {
            	Point center1 = new Point();
            	float[] radius1 = new float[1];
            	Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center1, radius1);
                float diameter = (2*radius1[0])/((float)Math.sqrt(2));
                double minArea = (diameter*0.92f)*(diameter*0.92f);
                double maxArea = (diameter*1.08f)*(diameter*1.08f);
                //System.out.println("Area real " + area2 + " min Area " + minArea);
            	if(area2 > minArea && area2 < maxArea){
            		int id = 0;
            		if(listSquares.size() == 0){
            			listSquares.add(approx);
            		}
            		if(listSquares.size() > 0){
            			float minx, maxx, miny, maxy;
            			minx = ((float)center1.x)*0.88f;
            			maxx = ((float)center1.x)*1.22f;
            			miny = ((float)center1.y)*0.88f;
            			maxy = ((float)center1.y)*1.22f;
            			Point center = new Point();
            			for(int j = 0; listSquares.size() > j; j++){
            				Imgproc.minEnclosingCircle(new MatOfPoint2f(listSquares.get(j).toArray()), center, new float[1]);
            				if((float)center.x > minx && (float)center.x < maxx && (float)center.y > miny && (float)center.y < maxy )
            					id = 1;
            			}
            		}
            		if(listSquares.size() > 0 && id == 0){
            			listSquares.add(approx);
            			id = 0;
            		}
            	}
            }  
        }
        Imgproc.drawContours(mat, contours, -1, new Scalar(255, 0, 0), 1);
              
        return mat;
    }
    
    public void project3d(Mat image){
    	
    	Mat cameraMatrix = cameraMat;
    	//System.out.println(cameraMatrix.dump());
    	MatOfPoint2f image_points = new MatOfPoint2f();
    	Mat rvec = new Mat();
    	Mat tvec = new Mat();
    	MatOfPoint3f object_points= new MatOfPoint3f();
    	MatOfDouble dist_coeffs = new MatOfDouble(Mat.zeros(4,1,CvType.CV_64FC1));

    	ArrayList<Point3> model_points = new ArrayList<Point3>();
    	model_points.add(new Point3(0.0f,   0.0f,   10.0f));
    	model_points.add(new Point3(161.0f, 0.0f,   10.0f));
    	model_points.add(new Point3(0.0f,   187.0f, 10.f));
    	model_points.add(new Point3(161.0f, 187.0f, 10.0f));
    	object_points.fromList(model_points);

    	ArrayList<Point> image_model = new ArrayList<Point>();
    	image_model.add(new Point(centerTriangle.get(0).center.x, centerTriangle.get(0).center.y));
    	image_model.add(new Point(centerTriangle.get(1).center.x, centerTriangle.get(1).center.y));
    	image_model.add(new Point(centerSquare.get(0).center.x, centerSquare.get(0).center.y));
    	image_model.add(new Point(centerSquare.get(0).center.x, centerSquare.get(1).center.y));
    	image_points.fromList(image_model);

    	Calib3d.solvePnP(object_points, image_points, cameraMatrix, dist_coeffs, rvec, tvec);

    	ArrayList<Point3> pp = new ArrayList<Point3>();
    	pp.add(new Point3(0, 0, 0)); //pts.get(0)
    	pp.add(new Point3(0, 187, 0)); //pts.get(1)
    	pp.add(new Point3(161, 187, 0)); //pts.get(2)
    	pp.add(new Point3(161, 0, 0)); //pts.get(3)
    	pp.add(new Point3(0, 0, -70)); //pts.get(4)
    	pp.add(new Point3(0, 187, -70)); //pts.get(5)
    	pp.add(new Point3(161, 187, -70)); //pts.get(6)
    	pp.add(new Point3(161, 0, -70)); //pts.get(7)

    	MatOfPoint3f axis = new MatOfPoint3f();
    	axis.fromList(pp);
    	MatOfPoint2f imgpts = new MatOfPoint2f();

    	Calib3d.projectPoints(axis, rvec, tvec, cameraMatrix, dist_coeffs, imgpts);

    	List<Point> pts = new Vector<Point>();
    	Converters.Mat_to_vector_Point(imgpts, pts);
    	Scalar color = new Scalar(255, 0, 0);
    	Scalar color1 = new Scalar(0, 0, 255);
    	Scalar color2 = new Scalar(0, 255, 0);
    	//bottom
    	Core.line(image, pts.get(0), pts.get(3), color, 2);
    	Core.line(image, pts.get(0), pts.get(1), color, 2);
    	Core.line(image, pts.get(3), pts.get(2), color, 2);
    	Core.line(image, pts.get(1), pts.get(2), color, 2);
    	//eixo z
    	Core.line(image, pts.get(0), pts.get(4), color1, 2);
    	Core.line(image, pts.get(1), pts.get(5), color1, 2);
    	Core.line(image, pts.get(2), pts.get(6), color1, 2);
    	Core.line(image, pts.get(3), pts.get(7), color1, 2);
    	//top
    	Core.line(image, pts.get(4), pts.get(5), color2, 2);
    	Core.line(image, pts.get(4), pts.get(7), color2, 2);
    	Core.line(image, pts.get(5), pts.get(6), color2, 2);
    	Core.line(image, pts.get(7), pts.get(6), color2, 2);

    	//quadrados (vermelho, verde)
    	Core.circle(image, centerSquare.get(0).center, 4, new Scalar(255, 0, 0), -1);
    	Core.circle(image, centerSquare.get(1).center, 4, new Scalar(0, 255, 0), -1);
    	//triangulo (azul, amarelo)
    	Core.circle(image, centerTriangle.get(0).center, 4, new Scalar(0, 0, 255), -1);
    	Core.circle(image, centerTriangle.get(1).center, 4, new Scalar(255, 255, 0), -1);

    }
    
    public Mat storesCentroid(Mat image){
    	image = image.clone();
    	centerTriangle.clear(); //triangulos
    	centerSquare.clear(); //quadrados
    	for (int i = 0; i < listTriangles.size(); i++) {
            Point center1 = new Point();
            float[] radius1 = new float[1];
            Imgproc.minEnclosingCircle(new MatOfPoint2f(listTriangles.get(i).toArray()), center1, radius1);
            centerTriangle.add(new Circle(center1, radius1[0]));
        }

         for (int i = 0; i < listSquares.size(); i++) {
             Point center1 = new Point();
             float[] radius1 = new float[1];
             Imgproc.minEnclosingCircle(new MatOfPoint2f(listSquares.get(i).toArray()), center1, radius1);
             centerSquare.add(new Circle(center1, radius1[0]));
         }
         //System.out.println("size triangle: " + listTriangle.size());
         for(int i = 0; i < centerTriangle.size(); i++){
             Core.circle(image, centerTriangle.get(i).center, 4, new Scalar(0, 0, 255), -1);
             Core.putText(image, "ID:   " + i, centerTriangle.get(i).center, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 1);
         }
         for(int i = 0; i < centerSquare.size(); i++){
              Core.circle(image, centerSquare.get(i).center, 4, new Scalar(255, 0, 0), -1);
              Core.putText(image, "ID:   " + i, centerSquare.get(i).center, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 1);
          }
         
         return image;

    }
    
    public Mat maskImage(Mat image){
        List<MatOfPoint> biggerContour = new ArrayList<MatOfPoint>();
      	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	MatOfPoint bcontours = new MatOfPoint();
    	double area1 = 0;
    	
    	Imgproc.findContours( image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    	for (int i = 0; i < contours.size(); i++) {
    		double area2 = Imgproc.contourArea(contours.get(i));

    		if (area1 < area2){
    			area1 = area2;
    			bcontours = contours.get(i);
    		} 
    	}
    	biggerContour.add(bcontours);
    	
    	Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);
        Imgproc.drawContours(mask, biggerContour, -1, new Scalar(255), -1);
        Imgproc.drawContours(mask, biggerContour, -1, new Scalar(0), 2);
        Mat masked = new Mat();
        Core.bitwise_and(image, mask, masked);
        
    	return masked;
    }
    
    public String[] readMatCamTxt(){
    	File file = new File(path + "/TextMatrix.txt");

    	try {
    		BufferedReader br = new BufferedReader(new FileReader(file));
    		String st = null;
    		String array[] = null;
    		while((st = br.readLine()) != null){
    			//System.out.println(st);
    			array = st.split(",");
    			
    		}
    		br.close();

    		if(array.length != 9)
    			throw new Exception("O formato da linha do arquivo deve ser: fx,0,cx,0,fy,cy,0,0,1");

    		return array;	

    	} catch (Exception e1) {
    		throw new RuntimeException(e1);
    	}
    }
    
    public Mat getCameraMatrix(){
    	String array[] = readMatCamTxt();
		double fx = Double.parseDouble(array[0]);
		double fy = Double.parseDouble(array[4]);
		double cx = Double.parseDouble(array[2]);
		double cy = Double.parseDouble(array[5]);
		
		Mat camera_matrix =  Mat.eye(3, 3, CvType.CV_32F);
		camera_matrix.put(0,0,fx);
	    camera_matrix.put(0,1,0.0);
	    camera_matrix.put(0,2,cx);

	    camera_matrix.put(1,0,0.0);
	    camera_matrix.put(1,1,fy);
	    camera_matrix.put(1,2,cy);

	    camera_matrix.put(2,0,0.0);
	    camera_matrix.put(2,1,0.0);
	    camera_matrix.put(2,2,1.0);
	    
	    return camera_matrix;
    }
    
    MatOfPoint approxPolyDP(MatOfPoint curve, double epsilon, boolean closed) {
        MatOfPoint2f tempMat=new MatOfPoint2f();

        Imgproc.approxPolyDP(new MatOfPoint2f(curve.toArray()), tempMat, epsilon, closed);

        return new MatOfPoint(tempMat.toArray());
    }
}
