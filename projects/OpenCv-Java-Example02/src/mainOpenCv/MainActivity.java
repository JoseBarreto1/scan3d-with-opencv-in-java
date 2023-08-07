package mainOpenCv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

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
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class MainActivity {
    List<MatOfPoint> listSquares = new ArrayList<MatOfPoint>();
    List<MatOfPoint> listTriangles = new ArrayList<MatOfPoint>();
    List<Circle> centerTriangle = new ArrayList<Circle>();
    List<Circle> centerSquare = new ArrayList<Circle>();
    
    private PointsObjectInFrame mObjectInFrame = new PointsObjectInFrame();
    //private OpenCVImageInGL cvImageInGL = new OpenCVImageInGL();
    
    private String path;
    private Mat cameraMat;
    
    public MainActivity(String path) {
    	this.path = path;
    	this.cameraMat = getCameraMatrix();
    	//mObjectInFrame.paramExtrinseco(cameraMat);
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
    			//Faz a leitura de todas as imagens JPGs de determinado diretorio e converte para o formato MAT
    			Mat mat = Highgui.imread(f.getPath(), Highgui.CV_LOAD_IMAGE_COLOR);    			
    			
    			//------- Inicia o Processamento das imagens ---
    			Mat matImgproc = imgproc1(mat.clone());
    			Mat matcontours = findObjects(matImgproc.clone());
    			//Filtro de processamento de imagens, só processa as >= x de quadrados e triangulos detectados
				if(listSquares.size() >= 0 && listTriangles.size() >= 0){
					totalsqr += listSquares.size();
					totaltri += listTriangles.size();
					total += 1;
					
					Mat matStore = storesCentroid(mat); // calcula o centro dos objetos de referencia
					calcSolvepnp(); //calcula os parametros Intrinsecos
					//cvImageInGL.imageBack(mat);
					//maskImage(matImgproc); //Passa a marcara
					
					//Mat mask = maskImage(matcontours.clone());
					//Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".G-erode(11-5)E(2x1)" + fileExtension, matImgproc1);
					//Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".G-erode(13-5)x(2x2)" + fileExtension, matcontours);
					//Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".G-erode(13-5)&(2x2)" + fileExtension, matStore);
					//Highgui.imwrite(outDir.getAbsolutePath() + "/" + fileName + ".matMask" + fileExtension, mask);
				}
			
    		}
    	}	
   		System.out.println("Total quadrados: " + totalsqr + "\nTotal triangulos: " + totaltri + "\nTotal:" + total);

    }
    
    //------------------Processamento de imagens/escala de cinza/retorna os contornos-------------
    public Mat imgproc1(Mat mat) {
    	Mat gray = new Mat( mat.height(), mat.width(), CvType.CV_8UC4);
        Mat canny = new Mat( mat.height(), mat.width(), CvType.CV_8UC4);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,1));
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(gray.clone(), canny, 250, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 5);
        Imgproc.Canny(canny.clone(), canny, 30, 120);
        Imgproc.dilate(canny.clone(), canny, element);
        //Imgproc.Canny(gray.clone(), canny, 30, 120); 
        //Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1));
        //Imgproc.erode(canny.clone(), canny, element1);        
        //Imgproc.threshold(gray.clone(), threshold, 30, 255, Imgproc.THRESH_BINARY);
            
        return canny;
    }
    
    //------------------Responsavel pela detecção dos objetos de referencia(quadrados e triangulos)------------
    public Mat findObjects(Mat mat){
    	listSquares.clear();
    	listTriangles.clear();
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	MatOfPoint approx = new MatOfPoint();
    	MatOfPoint2f approx_temp = new MatOfPoint2f();
    	
    	//lista todos os contornos na imagem analizada
    	Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {

            double peri = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            approx = approxPolyDP(contours.get(i), 0.04 * peri, true);
            //Salva as coordernadas das vertices poligonais
			approx_temp = approxPolyDP2f(contours.get(i), 0.04 * peri, true);
			double area_contours = Imgproc.contourArea(contours.get(i));
			
			//mostra no console a area absoluta de cada contorno/objeto fechado
			//System.out.println(Math.abs(Imgproc.contourArea(approx)));
			
			//mostra todas as vertices encontrados nos contornos
            /*for (int l = 0; l < approx_temp.toArray().length; l++) {
	            double[] temp_double;
	            temp_double = approx_temp.get(l, 0);
	            Point temp_point = new Point(temp_double[0],temp_double[1]);
	            Core.circle(mat, temp_point, 3, new Scalar(255, 255, 255), -1);
	    	}*/	 
			double areaApprox = Math.abs(Imgproc.contourArea(approx));
				
			//irá selecionar os contornos fechados com mais de 3 vertices e menores de 7 vertices, tbm é filtrado para que ruidos e pequenos contornos não sejam selecionados
            if (approx.toArray().length >= 3 && approx.toArray().length <= 8 && areaApprox > 200 && areaApprox < 8000) {
            	Point p0 = new Point(approx_temp.get(0, 0)[0],approx_temp.get(0, 0)[1]);
    	        Point p1 = new Point(approx_temp.get(1, 0)[0],approx_temp.get(1, 0)[1]);
    	        Point p2 = new Point(approx_temp.get(2, 0)[0],approx_temp.get(2, 0)[1]);
    	        
            	float[] raio_contours = new float[1];
            	Point centro_contours = new Point();
            	Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), centro_contours, raio_contours);
            	
            	//Retorna as vertices do menor quadrado que pode ser desenhado em torno do objeto
            	RotatedRect rRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
                Point[] vertices = new Point[4];
                rRect.points(vertices);
                
                //Desenha o contorno de acordo com as vertices detectadas pelo minAreaRect
                /*for(int j=0; j<4; ++j){
                    Core.line(mat, vertices[j], vertices[(j+1)%4], new Scalar(255,255,255),2);
                }*/
                
                //calcula a Area dos objetos, utilizando Relações Trigonométricas minAreaRect 
                double areaObj1 =  Math.sqrt(Math.pow((vertices[1].x - vertices[0].x),2)+ Math.pow((vertices[1].y - vertices[0].y),2)) * Math.sqrt((Math.pow((vertices[1].x - vertices[2].x),2)+ Math.pow((vertices[1].y - vertices[2].y),2)));
    	       
    	        //calcula a Area dos objetos, utilizando Relações Trigonométricas approxPolyDP2f 
    	        double areaObj0 =  Math.sqrt(Math.pow((p1.x - p0.x), 2)+ Math.pow((p1.y - p0.y), 2)) * Math.sqrt((Math.pow(p1.x - p2.x, 2))+ Math.pow((p1.y - p2.y), 2));
    	        
                
    	        double minArea_Square = areaObj0*0.80f;
    	        double maxArea_Square = areaObj0*1.1f;
    	        double minArea_Triangle = areaObj1*0.8f/2;
    	        double maxArea_Triangle = areaObj1*1.1f/2;
    	                      
    	        //Core.putText(mat, "Area Real:  " + area_contours, centro_contours, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);
    	         
                //Mostra no Console as areas dos objetos detectados para comparar suas "massas" (min, real, max)
                //System.out.println("MinQ:" + minArea_Square + " Real:" + area_contours + " MaxQ:" + maxArea_Square);
                //System.out.println("MinT:" + minArea_Triangle + " Real:" + area_contours + " MaxT:" + maxArea_Triangle);
                                
                //Verificando se o contorno detectado tem o formato triangular
                if(area_contours > minArea_Triangle && area_contours < maxArea_Triangle){
                	int id_tri = 0;
            		float minx, maxx, miny, maxy;
            		minx = ((float)centro_contours.x)*0.85f;
            		maxx = ((float)centro_contours.x)*1.25f;
            		miny = ((float)centro_contours.y)*0.85f;
            		maxy = ((float)centro_contours.y)*1.25f;
            		Point center = new Point();
            		//salvar um unico objeto para uma determina coordenada, levando em conta que existem o exteno e interno pra um unico objeto
                	//Não salvar repetida
            		for(int j = 0; listTriangles.size() > j; j++){
            			Imgproc.minEnclosingCircle(new MatOfPoint2f(listTriangles.get(j).toArray()), center, new float[1]);
            			if((float)center.x > minx && (float)center.x < maxx && (float)center.y > miny && (float)center.y < maxy )
            				id_tri = 1;
            		}
            		if(id_tri == 0){
                		listTriangles.add(approx);
                		
            		}
            	}
                //Verificando se o contorno detectado tem o formato quadrado
                else if(area_contours > minArea_Square && area_contours < maxArea_Square){
              		int id_squar = 0;
        			float minx, maxx, miny, maxy;
        			minx = ((float)centro_contours.x)*0.85f;
        			maxx = ((float)centro_contours.x)*1.25f;
        			miny = ((float)centro_contours.y)*0.85f;
        			maxy = ((float)centro_contours.y)*1.25f;
        			Point center = new Point();
        			//salvar um unico objeto para uma determina coordenada, levando em conta que existem o exteno e interno pra um unico objeto
                    for(int j = 0; listSquares.size() > j; j++){
        				Imgproc.minEnclosingCircle(new MatOfPoint2f(listSquares.get(j).toArray()), center, new float[1]);
        				if((float)center.x > minx && (float)center.x < maxx && (float)center.y > miny && (float)center.y < maxy )
        					id_squar = 1;
        			}
        			if(id_squar == 0){
            			listSquares.add(approx);
            		}
        		}
                              
            }
        }
        
        Imgproc.drawContours(mat, contours, -1, new Scalar(255), 1);
        return mat;
    }
    
    //--------------Armazena o centro dos objetos(quadrados e triangulos)-----
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
         //desenha os centros detectados, nos frames.
         //System.out.println("size triangle: " + listTriangle.size());
         for(int i = 0; i < centerTriangle.size(); i++){
        	 //Desenha um circulo vermelho no triangulo
              Core.circle(image, centerTriangle.get(i).center, 4, new Scalar(0, 0, 255), -1);
              Core.putText(image, "ID:   " + i, centerTriangle.get(i).center, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 1);
         }
         for(int i = 0; i < centerSquare.size(); i++){
        	  //Desenha um circulo azul no quadrado
              Core.circle(image, centerSquare.get(i).center, 4, new Scalar(255, 0, 0), -1);
              Core.putText(image, "ID:   " + i, centerSquare.get(i).center, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 1);
          }         
         return image;
    }
    
    //---------------- Cria uma mascara----------------
    //ou seja, retorna apenas o objeto desejado
    public void maskImage(Mat image){
        List<MatOfPoint> biggerContour = new ArrayList<MatOfPoint>();
      	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	MatOfPoint bcontours = new MatOfPoint();
    	MatOfPoint approx = new MatOfPoint();
    	
    	double area1 = 0;
    	
    	Imgproc.findContours( image.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    	for (int i = 0; i < contours.size(); i++) {
    		
    		double area2 = Math.abs(Imgproc.contourArea(contours.get(i)));
        	double peri = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
    		approx = approxPolyDP(contours.get(i), 0.04 * peri, true);
    		if(area1 < area2 && approx.toArray().length > 2 
    				// && approx.toArray().length >= 6
    				) {    		
    			area1 = area2;
    			bcontours = contours.get(i);
    		}    			
    	}
    	
    	biggerContour.add(bcontours);
    	Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);
    	Imgproc.drawContours(mask, biggerContour, -1, new Scalar(255), 4);   
        Imgproc.drawContours(mask, biggerContour, -1, new Scalar(0), 1);
        Mat masked = new Mat();
        Core.bitwise_and(image, mask, masked);
        mObjectInFrame.maskObject(masked);
    }
    
    //--------------------Calcula o solvepnp------------------
    public void calcSolvepnp(){    	
    	Mat cameraMatrix = cameraMat;
    	//System.out.println(cameraMatrix.dump());
    	MatOfPoint2f image_points = new MatOfPoint2f();
    	Mat rvec = new Mat();
    	Mat tvec = new Mat();
    	MatOfPoint3f object_points= new MatOfPoint3f();
    	MatOfDouble dist_coeffs = new MatOfDouble(Mat.zeros(4,1,CvType.CV_64FC1));

    	ArrayList<Point3> model_points = new ArrayList<Point3>();
    	model_points.add(new Point3(0.0f,   0.0f,   0f));
    	model_points.add(new Point3(187.0f, 0.0f,   0f));
    	model_points.add(new Point3(0.0f,   161.0f, 0f));
    	model_points.add(new Point3(187.0f, 161.0f, 0f));
    	object_points.fromList(model_points);
    	//System.out.println("Model_points: \n" + object_points.dump());

    	ArrayList<Point> image_model = new ArrayList<Point>();
    	image_model.add(new Point(centerTriangle.get(0).center.x, centerTriangle.get(0).center.y));
    	image_model.add(new Point(centerSquare.get(0).center.x, centerSquare.get(0).center.y));
    	image_model.add(new Point(centerTriangle.get(1).center.x, centerTriangle.get(1).center.y));
    	image_model.add(new Point(centerSquare.get(1).center.x, centerSquare.get(1).center.y));
    	image_points.fromList(image_model);
    	//System.out.println("M: \n" + image_points.dump());

    	Calib3d.solvePnP(object_points, image_points, cameraMatrix, dist_coeffs, rvec, tvec);
    	//mObjectInFrame.paramIntrinseco(rvec, tvec);
    	//cvImageInGL.paramIntrinseco(rvec, tvec);
    }
    
    //--------------------Realiza a leitura do txt com os dados extrinseco da camera----------------------------------  
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
    
    //-------------funções auxiliares-----------------------------------
    MatOfPoint2f approxPolyDP2f(MatOfPoint curve, double epsilon, boolean closed) {
        MatOfPoint2f tempMat=new MatOfPoint2f();

        Imgproc.approxPolyDP(new MatOfPoint2f(curve.toArray()), tempMat, epsilon, closed);

        return tempMat;
    }
    
    MatOfPoint approxPolyDP(MatOfPoint curve, double epsilon, boolean closed) {
        MatOfPoint2f tempMat=new MatOfPoint2f();

        Imgproc.approxPolyDP(new MatOfPoint2f(curve.toArray()), tempMat, epsilon, closed);

        return new MatOfPoint(tempMat.toArray());
    }
}
