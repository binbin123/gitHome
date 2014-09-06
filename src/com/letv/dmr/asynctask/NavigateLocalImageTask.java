package com.letv.dmr.asynctask;


import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import org.cybergarage.util.Debug;
import com.letv.dmr.upnp.DesUtils;
import android.view.View;
import com.letv.dmr.PictureShowActivity;
import android.widget.Toast;
import android.content.Context;
import com.letv.smartControl.R;


public class NavigateLocalImageTask extends AsyncTask<String, Void, Bitmap> {
  private String url;
  private ImageView iv;
  private RelativeLayout  image_loading ,pngBc= null;
  private static final String TAG = "NavigateLocalImageTask";
  private Context mContext = null;
  public NavigateLocalImageTask(String url,ImageView iv,RelativeLayout  image_loading,RelativeLayout  bc,Context context) {
    super();
    this.url = url;
    this.iv=iv;
    this.pngBc=bc;
    this.image_loading = image_loading;
    mContext = context;
  }

  @Override
  protected Bitmap doInBackground(String... params) {
    Bitmap bitmap = null;  

    Debug.e("DownloadImageTask", "NavigateLocalImageTask url  =" + url);	
    try  
    {  
      File file = new File(url);  
      if(file.exists())  
      {  
	      BitmapFactory.Options opt = new BitmapFactory.Options();
	      
	      opt.inJustDecodeBounds = true;
	      opt.inSampleSize = 1;
	      BitmapFactory.decodeFile(url,opt);
	      
	      Debug.d(TAG, "opt.outHeight = " + opt.outHeight + "outWidth" + opt.outWidth);
	      if (opt.outWidth == -1
	          || opt.outHeight == -1) {
	      
	      }else{
	        opt.inSampleSize = DesUtils.computeSampleSize(opt,-1,DownloadImageTask.MAX_REQUEST_WIDTH*DownloadImageTask.MAX_REQUEST_HEIGHT);
	      }
	      Debug.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
	      opt.inJustDecodeBounds = false;
	      opt.inDither = false;
	      
	      bitmap = BitmapFactory.decodeFile(url,opt);  
             file.delete();
      }  
    } catch (Exception e)  {  
      e.printStackTrace();
    }  catch(OutOfMemoryError e1){
              Debug.d(TAG, "OutOfMemoryError = " + e1);
              System.gc();
        }

   
    return bitmap;  

  }

  @Override
  protected void onPostExecute(Bitmap result) {
    // TODO Auto-generated method stub
	super.onPostExecute(result);
	if(image_loading != null)
		image_loading.setVisibility(View.INVISIBLE); 
	if(result != null){ 
	    pngBc.setBackgroundColor(Color.BLACK);
		iv.setImageBitmap(result);
	}else{
		Toast.makeText(mContext, mContext.getString(R.string.download_image_failed), Toast.LENGTH_SHORT).show();
		((PictureShowActivity)mContext).finish(); 
	}
  }

}
