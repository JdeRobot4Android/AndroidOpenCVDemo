package org.jderobot.androidopencvdemo;

import java.util.ArrayList;
import java.util.List;

import org.jderobot.androidopencvdemo.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


import jderobot.CameraPrx;
import jderobot.DataNotExistException;
import jderobot.HardwareFailedException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

  /* Image View for original image declared */
  private ImageView imageOriginal;
  
  /* Image View for image after filter declared */
  private ImageView imageFiltered;

  /* Text View for FPS */
  private TextView fps_view;

  /* Value of fps */
  private double fps = 0;

  /* Text View for Bandwidth */
  private TextView bandwidth_view;

  /* Value of Bandwidth */
  private double bandwidth = 0;

  /* Button declared */
  private RelativeLayout rel_layout;

  /* Declare pointer to camera interface */
  private CameraPrx cprx = null;

  /* String for port */
  private String port = "9999";

  /*String for proxy name*/
  private String proxyName = "cameraA";
  /* String for ip address */
  private String ipaddress = "";

  /* String for protocol */
  private String protocol = "tcp";

  /* Set the flag to 1 */
  private String NullFlag = "1";

  /* Declare the task */
  DownloadFilesTask runner = new DownloadFilesTask();

  /* Declare the default width and height of ImageView */
  private int imagwidth = 240;
  private int imagheight = 160;

  private int executed = 1;
  
  /*filternumber for selection of filters*/
  private int filternumber = 0;

  /* Aspect Ratio */
  private double aspect_ratio = 0;
  
  /*List items for drawer layout*/
  ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
  
  /*Main drawer layout*/
  private DrawerLayout mDrawerLayout;
  ListView mDrawerList;
  RelativeLayout mDrawerPane;
  
  
  private SeekBar cannybar1;
  private SeekBar cannybar2;
  public static int cannybar_value1=0;
  public static int cannybar_value2=0;
  private TextView cannybar_text;
  
  private SeekBar houghbar;
  private TextView houghbar_text;
  public static int hough_threshold = 50;
  
  private SeekBar H_barmin;
  private SeekBar H_barmax;
  private SeekBar S_barmin;
  private SeekBar S_barmax;
  private SeekBar V_barmin;
  private SeekBar V_barmax;
  private TextView H_text;
  private TextView S_text;
  private TextView V_text;
  public static int Hmin;
  public static int Hmax;
  public static int Smin;
  public static int Smax;
  public static int Vmin;
  public static int Vmax;
  
  public static Mat previous_image;
  
  Filters filters;


  private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
      @Override
      public void onManagerConnected(int status) {
          switch (status) {
              case LoaderCallbackInterface.SUCCESS:
              {
                  Log.i("APP", "OpenCV loaded successfully");
              } break;
              default:
              {
                  super.onManagerConnected(status);
              } break;
          }
      }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    filters = new Filters(this);
    /*Add the filters to the menu of filters*/
    mNavItems.add(new NavItem("Gray", "Grayscale filter", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Canny", "Canny edge detector", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Sobel", "Sobel Filter", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Laplacian", "Apply the laplacian filter", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Pyramid", "Pyramid Image filter", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("HSV filter", "Performs HSV filter", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Harris", "Detect Harris corners", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Hough Transform", "Apply Hough Transform", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Hough Circles", "Find circles using Hough Transform", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Convolution", "Guassian blur",R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Optical Flow","Detects change in movement", R.drawable.ic_launcher));
    mNavItems.add(new NavItem("Face Detection","Detects Face using Haar Cascade",R.drawable.ic_launcher));
    
    /* Find the drawer layout*/
    mDrawerLayout = (DrawerLayout) findViewById(R.id.layout);

    /*Populate the drawer layout with the filters*/
    mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
    mDrawerList = (ListView) findViewById(R.id.navList);
    /*Set the Drawer List adapter*/
    DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
    mDrawerList.setAdapter(adapter);
 
    // Drawer Item click listeners
    mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItemFromDrawer(position);
        }
    });
    fps_view = (TextView) findViewById(R.id.textView2);

    bandwidth_view = (TextView) findViewById(R.id.textView4);

    /* Set the ImageView to imageFiltered */
    imageOriginal = (ImageView) findViewById(R.id.imageView1);
    
    /* Set the ImageView to imageFiltered */
    imageFiltered = (ImageView) findViewById(R.id.imageView2);

    /* Set click listener to layout */
    rel_layout = (RelativeLayout) findViewById(R.id.mainContent);
    rel_layout.setOnClickListener(this);

    cannybar1 = (SeekBar) findViewById(R.id.canny_bar_1);
    cannybar2 = (SeekBar) findViewById(R.id.canny_bar_2);
    cannybar_text = (TextView) findViewById(R.id.canny_bar_text);
    cannybar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			cannybar_value1 = progress;
			cannybar_text.setText("Canny Threshold1:"+cannybar_value1+"\nCanny Threshold2:"+cannybar_value2);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    cannybar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			cannybar_value2 = progress;
			cannybar_text.setText("Canny Threshold1:"+cannybar_value1+"\nCanny Threshold2:"+cannybar_value2);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});

    houghbar = (SeekBar) findViewById(R.id.hough_bar);
    houghbar_text = (TextView) findViewById(R.id.hough_bar_text);
    houghbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			hough_threshold = progress;
			houghbar_text.setText("Hough Threshold:"+hough_threshold);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    
    H_text = (TextView) findViewById(R.id.H_text);
    H_barmin = (SeekBar) findViewById(R.id.Hbarmin);
    H_barmax = (SeekBar) findViewById(R.id.Hbarmax);
    S_text = (TextView) findViewById(R.id.S_text);
    S_barmin = (SeekBar) findViewById(R.id.Sbarmin);
    S_barmax = (SeekBar) findViewById(R.id.Sbarmax);
    V_text = (TextView) findViewById(R.id.V_text);
    V_barmin = (SeekBar) findViewById(R.id.Vbarmin);
    V_barmax = (SeekBar) findViewById(R.id.Vbarmax);
    
    H_barmin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			Hmin = progress;
			H_text.setText("H Min:"+Hmin+"\nH Max:"+Hmax);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    H_barmax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			Hmax = progress;
			H_text.setText("H Min:"+Hmin+"\nH Max:"+Hmax);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    
    S_barmin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			Smin = progress;
			S_text.setText("S Min:"+Smin+"\nS Max:"+Smax);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    
    S_barmax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			Smax = progress;
			S_text.setText("S Min:"+Smin+"\nS Max:"+Smax);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    
    V_barmin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			Vmin = progress;
			V_text.setText("V Min:"+Vmin+"\nV Max:"+Vmax);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    V_barmax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			Vmax = progress;
			V_text.setText("V Min:"+Vmin+"\nV Max:"+Vmax);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	});
    
    
    /* Call the preferences and set them to the strings */
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    proxyName = prefs.getString("proxyname", "cameraA");
    port = prefs.getString("port", "9999");
    protocol = prefs.getString("protocol", "default");
    ipaddress = prefs.getString("ipaddress", "172.10.2.102");
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    // Toast.makeText(getApplicationContext(), port + ""+ipaddress+ "" +protocol,
    // Toast.LENGTH_LONG).show();
    // new CustomTask().execute((Void[])null);
    try {
      initializeCommunicator();
      // Toast.makeText(getApplicationContext(), "Communicator initialized",
      // Toast.LENGTH_LONG).show();
    } catch (DataNotExistException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (HardwareFailedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    setaspectratio();
  }

  /*
   Called when a particular item from the navigation drawer
   is selected.
   */
  private void selectItemFromDrawer(int position) {
      
	  /*Get the filter position*/
	  filternumber = position;
	  
      mDrawerList.setItemChecked(position, true);
      setTitle(mNavItems.get(position).mTitle);
//      // Close the drawer
//      mDrawerLayout.closeDrawer(mDrawerPane);
  }
  

public void onClick(View v) {

    if (NullFlag == "0") {
      if (executed == 1) {
        runner = new DownloadFilesTask();
        runner.execute(cprx);
        executed = 0;
      } else {
        /* As runner is already running we cancel it */
        runner.cancel(true);
        executed = 1;
      }
    } else {
      /* If NullFlag is not zero then there is no connection */
      Toast.makeText(getApplicationContext(), "Connection not established", Toast.LENGTH_SHORT)
          .show();
    }
  }

  private Void setaspectratio() {

    aspect_ratio = (double) imagwidth / (double) imagheight;
    imagheight = imageOriginal.getLayoutParams().height;
    imagwidth = (int) (aspect_ratio * imagheight);
    Log.e("baa", imagwidth + " " + imagheight + " " + aspect_ratio);

    /* Set the imageheight and imagewidth as per aspect ratio*/
    imageOriginal.getLayoutParams().height = imagheight;
    imageOriginal.getLayoutParams().width = imagwidth;
    imageFiltered.getLayoutParams().height = imagheight;
    imageFiltered.getLayoutParams().width = imagwidth;
    
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
    Mat frame = null, frame2 = null, helper = null;
    Bitmap mBitmap = null;
    Bitmap mBitmapfilter = null;

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

          /* Check if we have allocated enough space for RGBA output bitmap */
          if (mBitmap == null || mBitmap.getHeight() != realdata.description.height
              || mBitmap.getWidth() != realdata.description.width) {
            mBitmap =
                Bitmap.createBitmap(realdata.description.width, realdata.description.height,
                    Bitmap.Config.ARGB_8888);
          }
          
          if (mBitmapfilter == null || mBitmapfilter.getHeight() != realdata.description.height
                  || mBitmapfilter.getWidth() != realdata.description.width) {
                mBitmapfilter =
                    Bitmap.createBitmap(realdata.description.width, realdata.description.height,
                        Bitmap.Config.ARGB_8888);
              }
          
          if (frame == null || frame.rows() != realdata.description.height
              || frame.cols() != realdata.description.width) {
            frame = new Mat(realdata.description.height, realdata.description.width, CvType.CV_8UC3);
          }
          if (frame2 == null || frame2.rows() != realdata.description.height
              || frame2.cols() != realdata.description.width) {
            frame2 = new Mat(realdata.description.height, realdata.description.width, CvType.CV_8UC4);
          }

          /* Check supported image formats */
          /* For OpenCV test right now we will need a gray image */
          if (realdata.description.format.equals("NV21")) {
            if (helper == null || helper.rows() != (int)(realdata.description.height * 1.5)
                || helper.cols() != realdata.description.width) {
              helper = new Mat((int)(realdata.description.height * 1.5), realdata.description.width, CvType.CV_8UC1);
            }
            helper.put(0, 0, realdata.pixelData);
            Imgproc.cvtColor(helper, frame, Imgproc.COLOR_YUV2RGB_NV21);
          } else if (realdata.description.format.equals("RGB8")) {
            frame.put(0, 0, realdata.pixelData);
          } 
          //filternumber = 1; cannybar_value1=100;cannybar_value2=100;
          Imgproc.cvtColor(frame, frame2, Imgproc.COLOR_RGB2RGBA);
          Utils.matToBitmap(frame2, mBitmap); 
          
         frame2 = filters.performfiltering(filternumber, frame, frame2);

          Utils.matToBitmap(frame2, mBitmapfilter); 
          previous_image = new Mat(frame2.size(), CvType.CV_8UC4);
          frame.copyTo(previous_image);
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
          publishProgress(mBitmap, mBitmapfilter);
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
      imageOriginal.setImageBitmap(mBitmap[0]);
      imageFiltered.setImageBitmap(mBitmap[1]);
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

  class NavItem {
	    String mTitle;
	    String mSubtitle;
	    int mIcon;
	 
	    public NavItem(String title, String subtitle, int icon) {
	        mTitle = title;
	        mSubtitle = subtitle;
	        mIcon = icon;
	    }
	}
  
  class DrawerListAdapter extends BaseAdapter {
	  
	    Context mContext;
	    ArrayList<NavItem> mNavItems;
	 
	    public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
	        mContext = context;
	        mNavItems = navItems;
	    }
	 
	    @Override
	    public int getCount() {
	        return mNavItems.size();
	    }
	 
	    @Override
	    public Object getItem(int position) {
	        return mNavItems.get(position);
	    }
	 
	    @Override
	    public long getItemId(int position) {
	        return 0;
	    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			 
	        if (convertView == null) {
	            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.drawer_item, null);
	        }
	        else {
	            view = convertView;
	        }
	 
	        TextView titleView = (TextView) view.findViewById(R.id.title);
	        TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
	        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
	 
	        titleView.setText( mNavItems.get(position).mTitle );
	        subtitleView.setText( mNavItems.get(position).mSubtitle );
	        iconView.setImageResource(mNavItems.get(position).mIcon);
	 
	        return view;
		}
	}
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
   * Converts NV21 to OpenCV Gray Mat (8 bits per pixel)
   * 
   * @param source NV21 image
   * @param width image width
   * @param height image height
   */
  public static Mat convertNv21ToCvGray(byte[] source, int width, int height) {
    int length = width * height;
    byte[] destination = new byte[length];
    Mat image;

    // NV21 will have a Y component (brightness) on every pixel
    System.arraycopy(source, 0, destination, 0, length);
    /*image = Highgui.imdecode(new MatOfByte(destination), Highgui.IMREAD_GRAYSCALE);
    return image.reshape(1, height);*/
    image = new Mat(height + height/2,width, CvType.CV_8UC1);
    
    image.put(0, 0, source);
    return image;
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
      /* Initialize Ice communicator */
      communicator = Ice.Util.initialize();

      /* Get the object proxy */
      base =
          communicator.stringToProxy(proxyName+":" + protocol + " -h " + ipaddress + " -p " + port
              + " -t 1500");
      // Toast.makeText(getApplicationContext(), base.toString(), Toast.LENGTH_LONG).show();

      // Toast.makeText(getApplicationContext(), cprx.toString(), Toast.LENGTH_LONG).show();
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
    /* Initialize OpenCV library */
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    /* Test if preferences was modified */
    if (Preferences.modified) {
      /* Clear modified flag */
      Preferences.modified = false;
      /* Finish this Activity and reload it again */
      Toast.makeText(getApplicationContext(), R.string.application_reload, Toast.LENGTH_LONG)
          .show();
      this.finish();
      Intent i = new Intent(this, MainActivity.class);
      startActivity(i);
    }
    /* We call the Preferences and get the selected values */
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    /* Get the port, protocol and ip address */
    proxyName = prefs.getString("proxyname", "cameraA");
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

      // Toast.makeText(getApplicationContext(), "Communicator initialized",
      // Toast.LENGTH_LONG).show();
    } catch (DataNotExistException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (HardwareFailedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // imag.setLayoutParams(new LayoutParams(imagwidth, imagheight));
    setaspectratio();


  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      /* Move to Preferences when settings is clicked */
      Intent i = new Intent(this, Preferences.class);
      startActivity(i);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
