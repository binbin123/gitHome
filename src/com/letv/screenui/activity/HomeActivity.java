package com.letv.screenui.activity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.cybergarage.upnp.device.ConnectPhoneNumChangeListener;
import org.cybergarage.util.Debug;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.letv.airplay.AirplayService;
import com.letv.dmr.upnp.DMRService;
import com.letv.dmr.utils.IfcUtil;
import com.letv.screenui.activity.adapter.LocalNamesDialog;
import com.letv.screenui.activity.adapter.LocalNamesDialog.OnListItemClick;
import com.letv.smartControl.R;
import com.letv.statistics.DBHelper;
import com.letv.statistics.LogPostService;
import com.letv.upnpControl.service.BackgroundService;
import com.letv.upnpControl.service.ListenNetWorkService;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.LetvUtils;
import com.umeng.analytics.MobclickAgent;

public class HomeActivity extends Activity implements OnClickListener, ConnectPhoneNumChangeListener
{
    TextView connect_tips, ip_str;
    
    Button local_name, about, connect_phone, version_show;
    
    String items[] = null;
    
    SharedPreferences prefer;
    
    int mPosition = 0;
    
    public static final String TAG = "HomeActivity";
    
    public static final String ACTION_CHANGE_NAME = "com.smartControl.action.changeName";
    
    public static final int DISPLAY_CONNECT_NUMBER = 0;
    
    public static final int NOT_NET = 1;
    
    private Context context;
    
    Timer mTimer = null;
    
    TimerTask mTask = null;
    
    String hostip;
    
    private TextView app_version;
    
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DISPLAY_CONNECT_NUMBER:
                    int numsc = DMRService.getControlPointNumber();
                    if (numsc > 0)
                    {
                        dealTextView(numsc + "", R.string.main_connectnum_tip, connect_phone);
                    }
                    else
                    {
                        dealTextView(R.string.phone_connect);
                    }
                    break;
                case NOT_NET:
                    ip_str.setText(R.string.no_network);
                    dealTextView(R.string.phone_connect);
                    
                    break;
            }
        }
        
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_main);
        
        context = this;
        prefer = getSharedPreferences("DeviceNameId", MODE_PRIVATE);
        initView();
        MobclickAgent.updateOnlineConfig(context);
        startDmrAirplay();
        LetvUtils.checkUpdate(context);
        
        new Thread(new Runnable()
        {
            
            @Override
            public void run()
            {
                /* 上报应用启动信息 */
                ContentValues values = new ContentValues();
                values.put("at", Long.toString(System.currentTimeMillis()));
                DBHelper helper = new DBHelper(HomeActivity.this);
                helper.insert(values, "startupsTbl");
//                try{
//                new ObjectServer();
//                }catch(IOException e){
//                	e.printStackTrace();
//                }
            };
            
        }).start();
        
    }
    
    private void startDmrAirplay()
    {
        
        if (DMRService.dmrDev == null || (DMRService.dmrDev != null && DMRService.dmrDev.isDMRStart == false))
        {
            // Debug.d(TAG, "startDmrAirplay");
            startService(new Intent(HomeActivity.this, DMRService.class));
            startService(new Intent(HomeActivity.this, BackgroundService.class));
            Intent i = new Intent(HomeActivity.this, AirplayService.class);
            i.setAction("startService");
            startService(i);
            startService(new Intent(HomeActivity.this, LogPostService.class));
            Intent intent = new Intent(HomeActivity.this, ListenNetWorkService.class);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            startService(intent);
            
        }
        if (DMRService.dmrDev != null)
        {
            Debug.d(TAG, "addConnectedPhoneListener");
            DMRService.dmrDev.addConnectedPhoneListener(this);
        }
        else
        {
            
            mTimer = new Timer();
            mTask = new TimerTask()
            {
                
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    if (DMRService.dmrDev != null)
                    {
                        //  Debug.d(TAG, "addConnectedPhoneListener");
                        DMRService.dmrDev.addConnectedPhoneListener(HomeActivity.this);
                    }
                }
                
            };
            mTimer.schedule(mTask, 3000);
        }
    }
    
    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        mPosition = prefer.getInt("position", 0);
        String text = String.format(getResources().getString(R.string.main_local_name), items[mPosition]);
        local_name.setText(text);
        // dealTextView(items[mPosition], R.string.main_local_name, local_name);
        int numsc = DMRService.getControlPointNumber();
        if (numsc > 0)
        {
            
            dealTextView(numsc + "", R.string.main_connectnum_tip, connect_phone);
        }
        else
        {
            dealTextView(R.string.phone_connect);
        }
        
        SharedPreferences sp = getSharedPreferences("DeviceName", MODE_PRIVATE);
        
        SharedPreferences.Editor editor = sp.edit();
        
        editor.putString("device_name", items[mPosition]);
        editor.commit();
        super.onResume();
        MobclickAgent.onPageStart("HomeActivity");
        MobclickAgent.onResume(this);
    }
    
    public void onPause()
    {
        MobclickAgent.onPageEnd("HomeActivity");
        super.onPause();
        MobclickAgent.onPause(this);
    }
    
    protected void onDestroy()
    {
        
        if (DMRService.dmrDev != null)
        {
            DMRService.dmrDev.removeConnectedPhoneListener(this);
        }
        if (mTimer != null)
        {
            if (mTask != null)
            {
                mTask.cancel();
            }
            mTimer.cancel();
        }
        super.onDestroy();
    }
    
    /**
     * 
     */
    private void initView()
    {
        connect_tips = (TextView)findViewById(R.id.connect_yh_tips);
        ip_str = (TextView)findViewById(R.id.ip);
        connect_phone = (Button)findViewById(R.id.connect_phone);
        local_name = (Button)findViewById(R.id.modif_name);
        version_show = (Button)findViewById(R.id.version_show);
        if (LetvUtils.isHideNameFunc())
        {
            local_name.setVisibility(View.GONE);
        }
        about = (Button)findViewById(R.id.about);
        app_version = (TextView)findViewById(R.id.app_version);
        dealTextView(R.string.phone_connect);
        connect_phone.setText(R.string.phone_connect);
        items = getResources().getStringArray(R.array.item_names);
        mHandler.sendEmptyMessage(DISPLAY_CONNECT_NUMBER);
        
        String versionCode = LetvUtils.getAppVersion(context);
        version_show.setText(versionCode);
        String ip = IfcUtil.getIpAddress(context);
        
        if (ip.length() <= 0)
        {
            mHandler.sendEmptyMessage(NOT_NET);
            
        }
        else
        {
            String text = String.format(getString(R.string.ip), ip);
            ip_str.setText(text);
        }
//        String deviceName = LetvUtils.getTvProductName();
//        
//        if (deviceName.equals("letv"))
        {
            
            version_show.setVisibility(View.GONE);
            String strversionCode =String.format(context.getResources().getString(R.string.app_viersion_name), versionCode);
            app_version.setText(strversionCode);
            
        }
//        else
//        {
//            version_show.setVisibility(View.VISIBLE);
//            app_version.setText(context.getResources().getString(R.string.install_already));
//            
//        }
    }
    
    private void dealTextView(int id)
    {
        String text = getResources().getString(id);
        connect_phone.setText(text);
    }
    
    private void dealTextView(String name, int id, TextView v)
    {
        String text = String.format(getResources().getString(id), name);
        int position = text.indexOf(name);
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new ForegroundColorSpan(Color.rgb(38, 153, 255)),
        position,
        position + name.length(),
        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        v.setText(text);
    }
    
    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        switch (v.getId())
        {
            case R.id.modif_name:
                LocalNamesDialog dialog = new LocalNamesDialog(this, items, mPosition);
                
                dialog.setOnListClickListener(new OnListItemClick()
                {
                    
                    @Override
                    public void onListItemClick(LocalNamesDialog dialog, String item, int position)
                    {
                        // TODO Auto-generated method stub
                        int bp = mPosition;
                        mPosition = position;
                        prefer.edit().putInt("position", position).commit();
                        String text = String.format(getResources().getString(R.string.main_local_name), item);
                        local_name.setText(text);
                        SharedPreferences sp = getSharedPreferences("DeviceName", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("device_name", items[mPosition]);
                        editor.commit();
                        String name_mac = LetvUtils.getProductNameMac(HomeActivity.this);
                        
                        if (LetvUtils.isDmrOnly() == false)
                        {
                            BackgroundService.setDeviceNameAndMac(name_mac, items[mPosition]);
                         
                        }
                        if (LetvUtils.isLetvUI() == LetvUtils.C1S)
                        {
                            SystemProperties.set(Constants.DEVEICE_NAME, items[mPosition]);
                        }
                        
                        if (DMRService.dmrDev != null)
                            DMRService.dmrDev.setDmrDeviceName(items[mPosition], String.valueOf(mPosition));
                        
                        /**
                         * send ChangeName broadcast.
                         */
                        
                        if (LetvUtils.isHideNameFunc() == false)
                        {
                            if (bp != position)
                            {
                                Debug.d(TAG, "send " + ACTION_CHANGE_NAME);
                                Intent intent = new Intent();
                                intent.setAction(ACTION_CHANGE_NAME);
                                sendBroadcast(intent);
                                bp = position;
                            }
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.connect_phone:
                startActivity(new Intent(this, ConnectPhoneActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutDialog.class));
                break;
            default:
                break;
        }
    }
    
    public void NotifyControlPointNumberChanged()
    {
        
        mHandler.sendEmptyMessage(DISPLAY_CONNECT_NUMBER);
    }
}
