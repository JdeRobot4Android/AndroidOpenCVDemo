package org.jderobot.androidopencvdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.Video;

import android.content.Context;
import android.util.Log;

public class Filters {

	private Context context;
	
	public Filters(Context current){
        this.context = current;
    }
	
	public Mat performfiltering(int filternumber, Mat frame, Mat frame2) throws IOException{
		if(filternumber==0){
      	  /* Gray Filter*/
            Imgproc.cvtColor(frame, frame2, Imgproc.COLOR_RGB2GRAY);
        }
		if(filternumber==1){
            /*Canny Filter*/
            Imgproc.cvtColor(frame, frame2, Imgproc.COLOR_RGB2GRAY);
            Imgproc.Canny(frame2, frame2, MainActivity.cannybar_value1, MainActivity.cannybar_value2);

        }
		if(filternumber==2){
            /*Sobel Filter*/
            Imgproc.cvtColor(frame, frame2, Imgproc.COLOR_RGB2GRAY);
            Mat gradient_x=new Mat(frame2.height(), frame2.width(), CvType.CV_8UC4); 
            Mat gradient_y= new Mat(frame2.height(), frame2.width(), CvType.CV_8UC4);
            Mat abs_grad_x=new Mat(frame2.height(), frame2.width(), CvType.CV_8UC4); 
            Mat abs_grad_y= new Mat(frame2.height(), frame2.width(), CvType.CV_8UC4);
            //Gradient x
            //Imgproc.Scharr(src_gray, grad_x, depth, 1, 0, skala, delta, Imgproc.BORDER_DEFAULT);
            Imgproc.Sobel(frame2, gradient_x, frame2.depth(), 1, 0, 3, 1, 0, Imgproc.BORDER_DEFAULT);
            Core.convertScaleAbs(gradient_x, abs_grad_x);
            //Gradient Y
            //Imgproc.Scharr(src_gray, grad_y, depth, 0, 1, skala, delta, Imgproc.BORDER_DEFAULT);
            Imgproc.Sobel(frame2, gradient_y, frame2.depth(), 0, 1, 3, 1, 0, Imgproc.BORDER_DEFAULT);
            Core.convertScaleAbs(gradient_y, abs_grad_y);
            Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, frame2);
        }
		if(filternumber==3){
            /*Laplacian Filter*/
            /* Convert working copy to grey scale */
            Imgproc.cvtColor(frame2, frame2, Imgproc.COLOR_RGB2GRAY);
            /* Apply Laplace operator:*/
            Imgproc.Laplacian(frame2, frame2, frame2.depth(), 3, 1, 0);
            /* Prescale values, get absolute value and apply alpha 1 and beta 0 */
            Core.convertScaleAbs(frame2, frame2);
            /* Convert result image back to RGB8 */
            Imgproc.cvtColor(frame2, frame2, Imgproc.COLOR_GRAY2RGB);
        }
		if(filternumber==4){
            /*Pyramid Image Filter*/
            Mat div2 = new Mat(frame2.height()/2, frame2.width()/2, CvType.CV_8UC4);
            Mat div4 = new Mat(frame2.height()/4, frame2.width()/4, CvType.CV_8UC4);
            Mat div8 = new Mat(frame2.height()/8, frame2.width()/8, CvType.CV_8UC4);
            Mat div16 = new Mat(frame2.height()/16, frame2.width()/16, CvType.CV_8UC4);
            Mat dst = Mat.zeros(frame2.size(), CvType.CV_8UC4);
            
            
            Imgproc.pyrDown(frame2, div2);
            Imgproc.pyrDown(div2, div4);
            Imgproc.pyrDown(div4, div8);
            Imgproc.pyrDown(div8, div16);
            
            Rect roi2 = new Rect(0,0,div2.cols(),div2.rows());
            Rect roi4 = new Rect(div2.cols(),0,div4.cols(),div4.rows());
            Rect roi8 = new Rect(div2.cols()+div4.cols(),0,div8.cols(),div8.rows());
            Rect roi16 = new Rect(div2.cols()+div4.cols()+div8.cols(),0,div16.cols(),div16.rows());

            div2.copyTo(dst.submat(roi2));
            div4.copyTo(dst.submat(roi4));
            div8.copyTo(dst.submat(roi8));
            div16.copyTo(dst.submat(roi16));
        	
        	  dst.copyTo(frame2);
        }
		if(filternumber==5){
            /* HSV Filter*/
            Mat HSV_mask = new Mat(frame2.size(), CvType.CV_8UC4);
            Mat HSV = new Mat(frame2.size(), CvType.CV_8UC4);
            /*Convert frame to BGR and then to HSV*/
            Imgproc.cvtColor(frame, HSV, Imgproc.COLOR_RGB2BGR);
            Imgproc.cvtColor(HSV, HSV, Imgproc.COLOR_BGR2HSV);
            /*Filter the HSV as per requirements and get a mask*/
            /*For skin detection set (3,50,50) and (33,255,255)*/
            Core.inRange(HSV, new Scalar(MainActivity.Hmin, MainActivity.Smin, MainActivity.Vmin), new Scalar(MainActivity.Hmax, MainActivity.Smax, MainActivity.Vmax), HSV_mask);
            /*Do a bitwise AND as per the mask set destination to HSV*/
            Core.bitwise_and(frame2, frame2, HSV, HSV_mask);
            /* Erode and dialate to reduce noise*/
            Imgproc.erode(HSV, HSV, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1)));
            Imgproc.dilate(HSV, HSV, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            /*Copy the result from HSV to frame2*/
            HSV.copyTo(frame2);
        }
		if(filternumber==6){
            /* Harris Corners*/
        	Mat gray = new Mat(frame2.size(), CvType.CV_8UC1);
        	Imgproc.cvtColor( frame2, gray, Imgproc.COLOR_RGB2GRAY);
            Mat dst, dst_norm = new Mat(frame2.size(), CvType.CV_8UC1);
            dst = Mat.zeros(frame2.size(), CvType.CV_32FC1);
         
            /* Detecting corners*/
            Imgproc.cornerHarris( gray, dst, 7, 5, 0.05, Imgproc.BORDER_DEFAULT);
         
            /* Normalizing */
            Core.normalize( dst, dst_norm, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1);
            Core.convertScaleAbs( dst_norm, dst );
         
            int thresh = 200;
            /* Drawing a circle around corners */
            for( int j = 0; j < dst_norm.rows() ; j++ )
            { 
            	for( int i = 0; i < dst_norm.cols(); i++ )
            	{
            		if( (int) dst_norm.get(j, i)[0] > thresh )
            		{	
            			Core.circle(frame2, new Point( i, j ), 5, new Scalar(255), 2, 8, 0 );
            		}
            	}
            }
          }
		if(filternumber==7){
        	/*Hough Transform*/
        	Mat gray = new Mat(frame2.size(), CvType.CV_8UC4);
        	
        	/*Convert to gray and apply canny*/
          	Imgproc.cvtColor(frame2, gray, Imgproc.COLOR_RGB2GRAY);
          	Imgproc.Canny(gray, frame2, 80, 100);
          	
          	int minLineSize = 20;
            int lineGap = 20;
          	Mat lines = new Mat();
          	
          	Imgproc.HoughLinesP(frame2, lines, 1, Math.PI/180, MainActivity.hough_threshold, minLineSize, lineGap);
          	for (int x = 0; x < lines.cols(); x++) 
            {
                  double[] vec = lines.get(0, x);
                  double x1 = vec[0], 
                         y1 = vec[1],
                         x2 = vec[2],
                         y2 = vec[3];
                  Point start = new Point(x1, y1);
                  Point end = new Point(x2, y2);

                  Core.line(frame2, start, end, new Scalar(255,0,0), 3);
            }
          }
		if(filternumber==8){
      	  /*Hough Circles*/
      	  Mat gray = new Mat(frame2.size(), CvType.CV_8UC4);
            Imgproc.cvtColor(frame2, gray, Imgproc.COLOR_RGB2GRAY);
            Size graysize = gray.size();
            Mat circles = new Mat();
            Imgproc.GaussianBlur(gray, gray, new Size(9,9), 0, 0);
            //Imgproc.Canny(gray, gray, 80, 100);
            //Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1d, (double)frame2.height()/70, 200d, 100d, 0, 1000);
            Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 2d, graysize.height/4);
            //Toast.makeText(getApplicationContext(), "  "+circles.cols(), Toast.LENGTH_LONG).show();
            Log.e("Hough"," "+ circles.cols());
            for (int x = 0; x < circles.cols(); x++) 
            {
                    double vCircle[]=circles.get(0,x);

                    Point center=new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                    int radius = (int)Math.round(vCircle[2]);
                    // draw the circle center
                    Core.circle(frame2, center, 3,new Scalar(0,255,0), -1, 8, 0 );
                    // draw the circle outline
                    Core.circle(frame2, center, radius, new Scalar(0,0,255), 3, 8, 0 );

            }
        }
		if(filternumber==9){
      	  /*Convolution*/
      	  Imgproc.cvtColor(frame2, frame2, Imgproc.COLOR_RGB2GRAY);
      	  Imgproc.GaussianBlur(frame2, frame2, new Size(15,15), 50);
      	  Imgproc.cvtColor(frame2, frame2, Imgproc.COLOR_GRAY2RGB);
                
        }
        if(filternumber == 10){
      	  /*Optical Flow*/
      	  Mat img1 = new Mat(frame2.size(),CvType.CV_8UC1);
      	  Mat img2 = new Mat(frame2.size(),CvType.CV_8UC1);
      	  Imgproc.cvtColor(MainActivity.previous_image, img1, Imgproc.COLOR_RGB2GRAY);
      	  Imgproc.cvtColor(frame2, img2, Imgproc.COLOR_RGB2GRAY);
      	  int i;
      	  int numpoints = 90;  // 300;
      	  TermCriteria criteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER,20,.03);
      	  MatOfPoint points0 = new MatOfPoint();
      	  Size pixWinSize = new Size(15,15);
      	  Size sizeWindow = new Size(31,31);
      	  Imgproc.goodFeaturesToTrack(img1, points0, numpoints, .01, .01);
      	  MatOfPoint2f points02f = new MatOfPoint2f();
      	  points02f.fromList(points0.toList());
      	  MatOfPoint2f points12f = new MatOfPoint2f();
      	  MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();
            Log.e("points0empty", ""+points0.empty());
      	  if(points0.empty()==false){
      		  Imgproc.cornerSubPix(img1, points02f, pixWinSize, new Size(-1,-1), criteria);
      		  Video.calcOpticalFlowPyrLK(img1, img2, points02f, points12f, status, err, sizeWindow, 5);  
      	  }
      	  Log.e("status", ""+status.toList().size());
      	  for (i = 0; i < numpoints; i++) {
      	      if (status.toList().get(i) == 0)
      	        continue;

      	      int line_thickness = 5;
      	      Scalar line_color = new Scalar(255, 0, 0);

      	      Point p = new Point((int) points02f.toArray()[i].x, (int) points02f.toArray()[i].y);
      	      Point q = new Point((int) points12f.toArray()[i].x, (int) points12f.toArray()[i].y);

      	      double angle = Math.atan2((double) p.y - q.y, (double) p.x - q.x);
      	      double hypotenuse = Math.sqrt((p.y - q.y)*(p.y - q.y) + (p.x - q.x)*(p.x - q.x));
      	      //Log.e("Hypotenuse", " "+hypotenuse+" "+i+ " andgle" + angle);
      	      if (hypotenuse < 10 || hypotenuse > 40)
      	        continue;

      	      /*Line*/
      	      q.x = (int) (p.x - 1 * hypotenuse * Math.cos(angle));
      	      q.y = (int) (p.y - 1 * hypotenuse * Math.sin(angle));
      	      Core.line(frame2, p, q, line_color, line_thickness, Core.LINE_AA, 0);
      	      Log.e("Hypotenuse", " "+hypotenuse+" "+i+ " andgle" + angle);
      	      Log.e("Line","   "+q.toString()+ " "+q.x+ "  "+q.y );
      	      /*Arrow*/
      	      p.x = (int) (q.x + 9 * Math.cos(angle + Math.PI / 4));
      	      p.y = (int) (q.y + 9 * Math.sin(angle + Math.PI / 4));
      	      Core.line(frame2, p, q, line_color, line_thickness, Core.LINE_AA, 0);
      	      p.x = (int) (q.x + 9 * Math.cos(angle - Math.PI / 4));
      	      p.y = (int) (q.y + 9 * Math.sin(angle - Math.PI / 4));
      	      Core.line(frame2, p, q, line_color, line_thickness, Core.LINE_AA, 0);
      	    }
        }
        if(filternumber==11){
        	
      	  InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
      	  InputStream is2 = context.getResources().openRawResource(R.raw.haarcascade_eye);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            File mCascadeFile2 = new File(cascadeDir, "haarcascade_eye.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            FileOutputStream os2 = new FileOutputStream(mCascadeFile2);
            byte[] buffer = new byte[4096];
            byte[] buffer2 = new byte[4096];
            int bytesRead, bytesRead2;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            while((bytesRead2 = is2.read(buffer2))!=-1){
          	  os2.write(buffer2, 0, bytesRead2);
            }
            is2.close();
            os2.close();
            is.close();
            os.close();
      	  CascadeClassifier faceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
      	  CascadeClassifier eyeDetector = new CascadeClassifier(mCascadeFile2.getAbsolutePath());
      	  
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(frame2, faceDetections);
            
            MatOfRect eyeDetections = new MatOfRect();
            eyeDetector.detectMultiScale(frame2, eyeDetections);
            for (Rect rect : faceDetections.toArray()) {
          	  Core.rectangle(frame2, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
            }
            for (Rect rect : eyeDetections.toArray()) {
          	  Core.rectangle(frame2, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
            }

        }
        if(filternumber==12){
      	  Imgproc.cvtColor(frame2, frame2, Imgproc.COLOR_RGB2GRAY);
      	  Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
      	  int minHessian = 400;
        }
		return frame2;
	}
}
