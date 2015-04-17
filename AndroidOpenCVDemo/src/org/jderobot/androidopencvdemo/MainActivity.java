package org.jderobot.androidopencvdemo;

import java.util.ArrayList;
import java.util.List;

import org.jderobot.androidopencvdemo.R;

import jderobot.CameraPrx;
import jderobot.DataNotExistException;
import jderobot.HardwareFailedException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {

	/*Image View declared*/
	private ImageView imag;	

	/*Text View for FPS*/
	private TextView fps_view;
	
	/*Value of fps*/
	private double fps = 0;
	
	/*Text View for Bandwidth*/
	private TextView bandwidth_view;
	
	/*Value of Bandwidth*/
	private double bandwidth = 0;
	
	/*Button declared*/
	private RelativeLayout rel_layout;	
	
	/*Declare pointer to camera interface*/
	private CameraPrx cprx = null;
	
	/*String for port*/
	private String port = "9999";
	
	/*String for ip address*/
	private String ipaddress = "";
	
	/*String for protocol*/
	private String protocol = "tcp";
	
	/*Set the flag to 1*/
	private String NullFlag = "1";
	
	/*Declare the  task*/ 
	DownloadFilesTask runner = new DownloadFilesTask();
	
	/*Declare the default width and height of ImageView*/
	private int imagwidth = 240;
	private int imagheight = 160;
	
	private int executed = 1;
	
	/*Aspect Ratio*/
	private double aspect_ratio = 0;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        fps_view = (TextView) findViewById(R.id.textView2);
        
        bandwidth_view =(TextView) findViewById(R.id.textView4);
        
        /*Set the ImageView to imag*/
        imag = (ImageView) findViewById(R.id.imageView1);
        
        /*Set click listener to layout*/
        rel_layout = (RelativeLayout)findViewById(R.id.layout); 
        rel_layout.setOnClickListener(this);
        
        /*Call the preferences and set them to the strings*/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        port = prefs.getString("port", "9999");
        protocol = prefs.getString("protocol", "default");
        ipaddress = prefs.getString("ipaddress", "172.10.2.102");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //Toast.makeText(getApplicationContext(), port + ""+ipaddress+ "" +protocol, Toast.LENGTH_LONG).show();
        //new CustomTask().execute((Void[])null);
        try {
			initializeCommunicator();
			//Toast.makeText(getApplicationContext(), "Communicator initialized", Toast.LENGTH_LONG).show();
		} catch (DataNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HardwareFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        setaspectratio();
    }
    

    public void onClick(View v) {  
    	
    	if(NullFlag == "0"){
    		if(executed == 1){
    			runner = new DownloadFilesTask();
    			runner.execute(cprx);	
    			executed = 0;
    		}
    		else{
    			/*As runner is already running we cancel it*/
    			runner.cancel(true);
    			executed = 1;
    		}	
    	}else{
    		/* If NullFlag is not zero then there is no connection*/
    		Toast.makeText(getApplicationContext(), "Connection not established", Toast.LENGTH_SHORT).show();	
    	}
  	}

    private Void setaspectratio(){
    	
    	aspect_ratio = (double) imagwidth/ (double) imagheight ;
    	imagheight = imag.getLayoutParams().height;
    	imagwidth = (int) (aspect_ratio*imagheight);
    	Log.e("baa", imagwidth+" " + imagheight + " "+ aspect_ratio);
    	//Toast.makeText(getApplicationContext(), "imagheight" + imagheight + "imagwidth" + imagwidth + aspect_ratio + imag.getLayoutParams().height + imag.getLayoutParams().width, Toast.LENGTH_LONG).show();
    	imag.getLayoutParams().height = imagheight;
    	imag.getLayoutParams().width = imagwidth;
    	
		return null;	
    }
    

   
  private class DownloadFilesTask extends AsyncTask<CameraPrx, Bitmap, Long> {
    private class FrameData {
      public double timestamp;
      public int bytes;

      FrameData(double timestamp, int bytes) {
        this.timestamp = timestamp;
        this.bytes = bytes;
      }
    }

    List<FrameData> statistics = new ArrayList<FrameData>();
    double last_update = 0, currentframetime = 0;

    protected Long doInBackground(jderobot.CameraPrx... urls) {
      jderobot.ImageData realdata;

      /* Execute this loop until button is clicked */
      while (true) {
        try {
          /* If proxy helper is not connected try to create it */
          if (cprx == null) {
            cprx = jderobot.CameraPrxHelper.uncheckedCast(base);
            synchronized (this) {
              _communicator = communicator;
              if (_cb != null) {
                _cb.onCreate(_communicator);
              }
            }
          }

          /* Get the image data */
          realdata = cprx.getImageData();

          /* Check if we have allocated enough space for RGBA output image */
          int length = realdata.description.width * realdata.description.height;
          if (imageRgba == null || imageRgba.length != length) {
            imageRgba = new int[length];
          }
          /* Check supported image formats */
          if (realdata.description.format.equals("NV21")) {
            convertNv21ToRgba(realdata.pixelData, imageRgba, realdata.description.width,
                realdata.description.height);
          } else if (realdata.description.format.equals("RGB8")) {
            convertRgbToRgba(realdata.pixelData, imageRgba, realdata.description.width,
                realdata.description.height);
          }

          /* Create bitmap with RGBA image */
          Bitmap mBitmap =
              Bitmap.createBitmap(imageRgba, realdata.description.width,
                  realdata.description.height, Bitmap.Config.ARGB_8888);
          imagwidth = mBitmap.getWidth();

          imagheight = mBitmap.getHeight();
          /* Add new frame to statistics */
          currentframetime = System.currentTimeMillis();
          FrameData frame_data = new FrameData(currentframetime, realdata.pixelData.length);
          statistics.add(frame_data);
          /* Remove old values */
          currentframetime -= 3000;
          while (statistics.size() > 2 && statistics.get(0).timestamp < currentframetime) {
            statistics.remove(0);
          }
          /* Calculate data only when 2 or more frames have been received */
          if (statistics.size() > 1) {
            int bytes_total = 0, num_frames = statistics.size() - 1;
            double elapsed_time =
                (statistics.get(num_frames).timestamp - statistics.get(0).timestamp) / 1000;
            for (FrameData current : statistics) {
              bytes_total += current.bytes;
            }
            fps = num_frames / elapsed_time;
            bandwidth = bytes_total / elapsed_time;
          }
          /* Show updates */
          publishProgress(mBitmap);
          if (isCancelled())
            break;

        } catch (Ice.TimeoutException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (DataNotExistException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (HardwareFailedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (Exception e) {
          e.printStackTrace();
          try {
            Thread.sleep(500);
          } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }

      return (long) 1;
    }


    protected void onProgressUpdate(Bitmap... mBitmap) {
      /* Set the ImageView to Bitmap on ProgressUpdate */
      imag.setImageBitmap(mBitmap[0]);
      /* Update displayed fps and bandwidth data at most 2 times per second */
      if ((currentframetime - last_update) > 500) {
        fps_view.setText(" " + String.format("%.1f", fps) + " fps");
        bandwidth_view.setText(" " + String.format("%.1f", bandwidth / 1024) + " KB/s");
        last_update = currentframetime;
      }
    }

    protected void onPostExecute(Long result) {
      /* Do nothing */
    }
  }

  // int Nv21ToRgba[][][] = new int[256][256][256];
  int[] imageRgba = null;

  /**
   * Converts NV21 to RGBA (R + G + B + alpha)
   * 
   * @param source NV21 image
   * @param destination RGBA image
   * @param width image width
   * @param height image height
   */
  public static void convertNv21ToRgba(byte[] source, int[] destination, int width, int height) {
    int length = destination.length;
    int u, v, y1, y2, y3, y4;
    
    int i1 = 0, i2 = width, j1 = 0, j2 = width, k = length;
    for (int y = 0; y < height; y += 2) {
      Log.w("DEBUG LOOP", "i1=" + i1 + ", i2=" + i2 + ", j1=" + j1 + ", j2=" + j2 + ", k=" + k);
      for (int x = 0; x < width; x += 2) {
        y1 = source[i1++] & 0xff;
        y2 = source[i1++] & 0xff;
        y3 = source[i2++] & 0xff;
        y4 = source[i2++] & 0xff;
        u = source[k++] & 0xff;
        v = source[k++] & 0xff;
        u = u - 128;
        v = v - 128;

        destination[j1++] = convertYuvToRgb(y1, u, v);
        destination[j1++] = convertYuvToRgb(y2, u, v);
        destination[j2++] = convertYuvToRgb(y3, u, v);
        destination[j2++] = convertYuvToRgb(y4, u, v);
      }
      i1 += width;
      i2 += width;
      j1 += width;
      j2 += width;

    }
  }

  /**
   * Converts YUV pixel to RGBA with alpha opaque
   * 
   * @param y Y component
   * @param u U component
   * @param v V component
   * @return equivalent RGBA pixel opaque
   */
  private static int convertYuvToRgb(int y, int u, int v) {
    int r, g, b;

    r = y + (int) 1.402f * v;
    g = y - (int) (0.344f * u + 0.714f * v);
    b = y + (int) 1.772f * u;
    r = (r > 255) ? 255 : (r < 0) ? 0 : r;
    g = (g > 255) ? 255 : (g < 0) ? 0 : g;
    b = (b > 255) ? 255 : (b < 0) ? 0 : b;
    return 0xff000000 | (b << 16) | (g << 8) | r;
  }

  /**
   * Converts RGB (R + G + B) to RGBA (R + G + B + alpha)
   * 
   * @param source RGB image
   * @param destination RGBA image
   * @param width image width
   * @param height image height
   */
  public static void convertRgbToRgba(byte[] source, int[] destination, int width, int height) {
    int length = destination.length;

    for (int i = 0, j = 0; i < length; i++, j += 3) {
      destination[i] = 0xff000000 + source[j + 2] + (source[j + 1] << 8) + (source[j] << 16);
    }
  }

    /* Implementation of ICE */
    interface CommunicatorCallback {
      void onWait();

      void onCreate(Ice.Communicator communicator);

      void onError(Ice.LocalException ex);
    }
    
    private Ice.Communicator _communicator;
    private CommunicatorCallback _cb;
    Ice.ObjectPrx base;
    Ice.Communicator communicator;

    
    private void initializeCommunicator() throws DataNotExistException, HardwareFailedException {
      try {
    	
    	NullFlag = "1";
    	/*Initialize Ice communicator*/
                communicator = Ice.Util.initialize();
        
        /*Get the object proxy*/
        base = communicator.stringToProxy("cameraA:"+protocol+ " -h "+ipaddress+" -p " + port + " -t 1500");
      	//Toast.makeText(getApplicationContext(), base.toString(), Toast.LENGTH_LONG).show();
        
      	//Toast.makeText(getApplicationContext(), cprx.toString(), Toast.LENGTH_LONG).show();
        NullFlag = "0";
      } catch (Ice.LocalException ex) {
        Log.e("ICE", "Error ICE");
        synchronized (this) {
          if (_cb != null) {
            _cb.onError(ex);
          }
        }
      }
    }
    
    public void onStop() {
        super.onStop();
      }

      public void onPause() {
        super.onPause();
        runner.cancel(true);
      }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    protected void onResume() {
      super.onResume();
      /* Test if preferences was modified */
      if (Preferences.modified) {
        /* Clear modified flag */
        Preferences.modified = false;
        /* Finish this Activity and reload it again */
        Toast.makeText(getApplicationContext(), R.string.application_reload, Toast.LENGTH_LONG).show();
        this.finish();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
      }
      /* We call the Preferences and get the selected values */
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

      /* Get the port, protocol and ip address */
      port = prefs.getString("port", "9999");
      protocol = prefs.getString("protocol", "default");
      ipaddress = prefs.getString("ipaddress", "172.10.2.102");
      
      // Check wakelock and lockscreen
      if (prefs.getBoolean("lockscreen", false) == true) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      } else {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      }
      try {
  			initializeCommunicator();
  			
  			//Toast.makeText(getApplicationContext(), "Communicator initialized", Toast.LENGTH_LONG).show();
  		} catch (DataNotExistException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		} catch (HardwareFailedException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
    	//imag.setLayoutParams(new LayoutParams(imagwidth, imagheight));
      	setaspectratio();
//      final float scale = this.getResources().getDisplayMetrics().density;
          
//  		imag.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imagwidth, getResources().getDisplayMetrics());
//  		abcd =(int) (imagwidth * scale + 0.5f);
//  		Log.e(NullFlag, abcd +"  "+ imagwidth + "  " + scale);
//  		//Toast.makeText(getApplicationContext(), (int) (imagwidth * scale + 0.5f) , Toast.LENGTH_LONG);
//  		imag.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imagheight, getResources().getDisplayMetrics());
//  		abcd =(int) (imagheight * scale + 0.5f);
//  		Log.e(NullFlag, abcd +"  " + imagheight);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	/* Move to Preferences when settings is clicked*/
        	Intent i = new Intent(this, Preferences.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}