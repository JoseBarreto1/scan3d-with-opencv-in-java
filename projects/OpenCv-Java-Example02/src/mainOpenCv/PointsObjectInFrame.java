package mainOpenCv;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import Jama.Matrix;

//Aqui Fica guardado as informações dos parametros da camera, tanto intrinseco, quanto extrinseco
public class PointsObjectInFrame  { //implements GLEventListener
    //Matriz de cada quadro analizado
	private Mat cameraMat, rvec, tvec, mask;
	private Matrix inversaMatrix;
	private double theta, phi;

    public PointsObjectInFrame(){       
    }
    
    public void cloudConstruction(){
    	pointCloudConstruction();
    }
    
    public void paramExtrinseco(Mat cameraMatrix){
    	this.cameraMat = cameraMatrix;
    }
    
    public void paramIntrinseco(Mat vetorRot, Mat vetorTrans){
    	this.rvec = vetorRot;
    	this.tvec = vetorTrans;
    }
    
    public void maskObject(Mat masked){
    	this.mask = masked;
    	calcMatrizInversa();
    	pointCloudConstruction();
    } 
    
    public void angleObject(double angle1, double angle2){
    	this.theta = angle1;
    	this.phi = angle2;
    }
    
    public void pointCloudConstruction(){
    	int cont = 0;
    	for(int j = 0; j < mask.rows(); j++){
        	for(int k = 0; k < mask.cols(); k++){
        		double dx = mask.get(j,k)[0];
        		if (dx > 150){	
        			cont++;
        		}
        	}
        }
    	Mat cloud_aux =  Mat.zeros(cont, 3, CvType.CV_32F); //Matriz onde a linha(row) é a mesma quantidade de pontos por frame e três colunas(cols)
        cont = 0;
    	for(int j = 0; j < mask.rows(); j++){
        	for(int k = 0; k < mask.cols(); k++){
        		double px = mask.get(j,k)[0];
        		if (px > 200){
        			//---------convertendo 2D em 3D-------------
        			double temp[][] = {{k},{j},{1}};
        			Matrix mB = new Matrix(temp);
        			Matrix cloudcontors = inversaMatrix.times(mB);
        			double rho = cloudcontors.get(0, 0)/cloudcontors.get(2, 0);
        			double raio = cloudcontors.get(1, 0)/cloudcontors.get(2, 0);
        			double x = raio*Math.sin(theta)*Math.sin(phi);
        			double y = raio*Math.cos(theta)*Math.sin(phi);
        			double z = raio*Math.cos(phi);
        			cloud_aux.put(cont,0,x);
        			cloud_aux.put(cont,1,y);
        			cloud_aux.put(cont,2,z);
        			//--------Verificando resultados------------
        		    //System.out.println("MATRIZ PONTOS CONTORNO:");
        			//System.out.println("X: " + k + " Y: " + j);
        		    //mB.print(2, 5); 
        			cont++;
        		}
        	}
    	}
    	System.out.println("MATRIZ 3D:\n" + cloud_aux.dump() + "\n");
        //System.out.println("Total_points: \n" + cont);
    }
    
    public void calcMatrizInversa(){
    	Mat cameraMatrix = cameraMat;
    	Mat rot_mat = Mat.zeros(3,3,CvType.CV_32F);
    	//Converte vetor de rotação em matriz de rotação
        Calib3d.Rodrigues(rvec, rot_mat);
        
        calcAngle(rot_mat);
        //System.out.println("Vetor de rotaçao: \n" + rot_mat.dump());
    	//System.out.println("Vetor de translação: \n" + tvec.dump());
        
        //Matriz double de rotação + translacao
        double a[][] = new double[3][3];
        double b[][] = new double[3][3];
        for(int i = 0; i < 3; i++){
        	for(int j = 0; j < 3; j++){
        		if(j==2){
        			double[] data = tvec.get(i,0);
        			b[i][j] = data[0];
        		}
        		else{
        			double[] data = rot_mat.get(i,j);
        			b[i][j] = data[0];
        		}
        	}
        }
        for(int i = 0; i < 3; i++){
        	for(int j = 0; j < 3; j++){
        		double[] data = cameraMatrix.get(i,j);
        		a[i][j]= data[0];
        	}
        }
        
        Matrix mB = new Matrix(b);
        Matrix mA = new Matrix(a);
        Matrix mC = mA.times(mB); //A*B
        Matrix inverseMatrix = mC.inverse(); //Inversa C
        		
        //-------Mostrando as Matrizes-------------
        /*System.out.println("MATRIZ A");
        mA.print(2, 5);      
        System.out.println("MATRIZ B");
        mB.print(2, 5);
        System.out.println("MATRIZ C");
        mC.print(2, 5);
        System.out.println("MATRIZ Inversa");
        inverseMatrix.print(2, 5);*/
        
        this.inversaMatrix = inverseMatrix;
    }
    
    // Convention used is Y-X-Z Tait-Bryan angles
    public void calcAngle(Mat rot_mat){
    	Mat euler = Mat.zeros(1,3,CvType.CV_64F);
  		double m00 = rot_mat.get(0,0)[0];
  		double m10 = rot_mat.get(1,0)[0];
  	    double m11 = rot_mat.get(1,1)[0];
  	    double m12 = rot_mat.get(1,2)[0];
  	    double m20 = rot_mat.get(2,0)[0];
  	    double m21 = rot_mat.get(2,1)[0];
  	    double m22 = rot_mat.get(2,2)[0];
  		double sy = Math.sqrt(m00*m00 + m10*m10);
  		double yaw, pitch, roll;
  		boolean singular = sy < 1e-6; // If  		 
  		
  		//Angulo de Euler para cada eixo(XYZ)
  	    if (!singular){
  	    	yaw = Math.atan2(m21 , m22);
  	    	pitch = Math.atan2(-m20, sy);
  	    	roll = Math.atan2(m10, m00);
  	    }
  	    else{
  	    	yaw = Math.atan2(-m12, m11);
  	    	pitch = Math.atan2(-m20, sy);
  	    	roll = 0;
  	    }
  	    euler.put(0, 0, 180 - (yaw * 180/Math.PI));
		euler.put(0, 1, pitch * 180/Math.PI);
		euler.put(0, 2, roll * 180/Math.PI); 
		
		System.out.println("Angulos Euler2: " + euler.dump() + "\n");
		
		double theta1 = roll * 180/Math.PI;
		double phi1 = pitch * 180/Math.PI;
		if(theta1 < 0)	theta1 = 360 - theta1;
		angleObject(theta1, Math.sqrt(phi1*phi1));  	    
		/*//Angulo de Rodrigues da solvePnP
		double m01 = rot_mat.get(0,1)[0];
  		double m02 = rot_mat.get(0,2)[0];
  	    double theta = Math.sqrt(m00*m00+m01*m01+m02*m02);
  	    System.out.println("\nAngulo Rodrigues: " + theta + "\n");*/
  	}
    
}

/*
    //calcular a diferença de proporção do contorno de um quadro para o outro
	List<Integer> proporcao = new ArrayList<Integer>();    
    public int calcprop(int prop) {
        proporcao.add(prop);
        int ajust = prop/proporcao.get(0);
        return ajust;
    }
    //((X*(sen(teta)),(X*(cos(teta))), Y); //3D RECONSTRUCTION
    
    //--------------------Calcula o angulo geral de cada frame----------------
    public void angleofObject(Mat mask){
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	Point yaux1 = new Point(0,0);
    	Point yaux2 = new Point(0,0);
    	Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
    	for (int i = 0; i < contours.size(); i++) {
        	MatOfPoint2f example = new MatOfPoint2f(contours.get(i).toArray());
    		//confirmar .cols
        	for (int j = 0; j < example.cols(); j++){
    			//---------extraindo o raio---------------
    			Point ymax = new Point(0,example.get(j, 0)[1]);
        		if (yaux1.y < ymax.y){
        			yaux1.y = ymax.y;
        		}
        		Point ymin = new Point(0,example.get(j, 0)[1]);
        		if (yaux2.y > ymin.y){
        			yaux2.y = ymin.y;
        		}
    		}
    	}
    } 
    
    // Convention used is Y-Z-X Tait-Bryan angles
  	void rot2euler(Mat rot_mat){
  		
  		Mat euler = Mat.zeros(1,3,CvType.CV_64F);
  		double m00 = rot_mat.get(0,0)[0];
  	    double m02 = rot_mat.get(0,2)[0];
  	    double m10 = rot_mat.get(1,0)[0];
  	    double m11 = rot_mat.get(1,1)[0];
  	    double m12 = rot_mat.get(1,2)[0];
  	    double m20 = rot_mat.get(2,0)[0];
  	    double m22 = rot_mat.get(2,2)[0];
  	    double x, y, z;
  	    // Assuming the angles are in radians.
  	    if (m10 > 0.998) { // singularity at north pole
  	        x = 0;
  	        y = Math.PI / 2;
  	        z = Math.atan2(m02, m22);
  	    }
  	    else if (m10 < -0.998) { // singularity at south pole
          x = 0;
          y = -Math.PI / 2;
          z = Math.atan2(m02, m22);
  	    }
  	    else{
          x = Math.atan2(-m12, m11);
          y = Math.asin(m10);
          z = Math.atan2(-m20, m00);
  	    }
  		euler.put(0, 0, 180- (x * 180/Math.PI));
  		euler.put(0, 1, y * 180/Math.PI);
  		euler.put(0, 2, z * 180/Math.PI);  		
  	    
  		//System.out.println("\nAngulos Euler1: " + euler.dump());
  	}
  	
   */

