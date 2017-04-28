package amap.com.amapandgoogle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLng;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends FragmentActivity implements View.OnClickListener,
        AMap.OnCameraChangeListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
        AMapLocationListener, CompoundButton.OnCheckedChangeListener{
    private ToggleButton mcheckbtn;
    private Button mapbtn;
    private LinearLayout mContainerLayout;
    private LayoutParams mParams;
    private TextureMapView mAmapView;
    private MapView mGoogleMapView;
    private float zoom = 10;
    private double latitude = 39.23242 ;
    private double longitude = 116.253654;
    private boolean mIsAmapDisplay = true;
    private boolean mIsAuto = true;
    private GoogleMap googlemap;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AlphaAnimation anappear;
    private AlphaAnimation andisappear;
    private IntentFilter mIntentFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initLocation();
        mContainerLayout = (LinearLayout) findViewById(R.id.map_container);
        mAmapView = new TextureMapView(this);
        mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mContainerLayout.addView(mAmapView, mParams);

        mAmapView.onCreate(savedInstanceState);

        mAmapView.getMap().setOnCameraChangeListener(this);

        anappear = new AlphaAnimation(0,1);
        andisappear = new AlphaAnimation(1,0);
        anappear.setDuration(5000);
        andisappear.setDuration(5000);

        // 注册广播，监听应用必须谷歌服务安装情况
        mIntentFilter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        mIntentFilter.addDataScheme("package");
        registerReceiver(mInstallReciver, mIntentFilter);
    }

    private void initLocation() {
        //初始化client
        mlocationClient = new AMapLocationClient(this.getApplicationContext());
        // 设置定位监听
        mlocationClient.setLocationListener(this);
        //定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    private void init() {
        mContainerLayout = (LinearLayout) findViewById(R.id.map_container);
        mapbtn = (Button)findViewById(R.id.button);
        mcheckbtn = (ToggleButton)findViewById(R.id.auto);
        mapbtn.setOnClickListener(this);
        mcheckbtn.setOnClickListener(this);
        mcheckbtn.setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.auto:
                mIsAuto = mcheckbtn.isChecked();
                break;
            case R.id.button:
                mcheckbtn.setChecked(false);
                mIsAuto = false;
                if (mIsAmapDisplay){

                    changeToGoogleMapView();
                } else {

                    changeToAmapView();
                }
                break;
        }
    }

    /**
     * 切换为高德地图显示
     */
    private void changeToAmapView() {
            if (googlemap != null && googlemap.getCameraPosition() != null){
                zoom = googlemap.getCameraPosition().zoom;
                latitude = googlemap.getCameraPosition().target.latitude;
                longitude = googlemap.getCameraPosition().target.longitude;
                mAmapView = new TextureMapView(this, new AMapOptions()
                        .camera(new com.amap.api.maps.model.CameraPosition(new LatLng(latitude,longitude),zoom,0,0)));
            }else {
                mlocationClient.startLocation();
                mAmapView = new TextureMapView(this);

            }
        mAmapView.onCreate(null);
        mAmapView.onResume();
        mContainerLayout.addView(mAmapView, mParams);

        mGoogleMapView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mGoogleMapView != null) {
                    mGoogleMapView.setVisibility(View.GONE);
                    mContainerLayout.removeView(mGoogleMapView);
                    mGoogleMapView.onDestroy();
                }
            }
        });
        mAmapView.getMap().setOnCameraChangeListener(this);
        mIsAmapDisplay = true;
        mapbtn.setText("To Google");
    }


    private Handler handler=new Handler(){
        public void handleMessage(Message message){
            mAmapView.setVisibility(View.GONE);
            mContainerLayout.removeView(mAmapView);
            if (mAmapView != null) {
                mAmapView.onDestroy();
            }
        }
    };

    /**
     * 切换为google地图显示
     */
    private void changeToGoogleMapView() {
        if (!checkGooglePlayServices()){
            return;
        }
        zoom = mAmapView.getMap().getCameraPosition().zoom;
        latitude = mAmapView.getMap().getCameraPosition().target.latitude;
        longitude = mAmapView.getMap().getCameraPosition().target.longitude;

        mapbtn.setText("To Amap");
        mIsAmapDisplay = false;
        mGoogleMapView = new com.google.android.gms.maps.MapView(this, new GoogleMapOptions()
                .camera(new com.google.android.gms.maps.model
                        .CameraPosition(new com.google.android.gms.maps.model.LatLng(latitude, longitude), zoom, 0, 0)));
        mGoogleMapView.onCreate(null);
        mGoogleMapView.onResume();
        mContainerLayout.addView(mGoogleMapView, mParams);
        mGoogleMapView.getMapAsync(this);
        handler.sendEmptyMessageDelayed(0,500);
    }

    @Override
    public void onCameraChange(com.amap.api.maps.model.CameraPosition cameraPosition) {

    }

    /**
     * 高德地图移动完成回调
     * @param cameraPosition 地图移动结束的中心点位置信息
     */
    @Override
    public void onCameraChangeFinish(com.amap.api.maps.model.CameraPosition cameraPosition) {
        longitude = cameraPosition.target.longitude;
        latitude = cameraPosition.target.latitude;
        zoom = cameraPosition.zoom;
        if (!isInArea(latitude, longitude) && mIsAmapDisplay && mIsAuto) {
            changeToGoogleMapView();
        }
    }

    /**
     * 粗略判断当前屏幕显示的地图中心点是否在国内
     * @param latitude 纬度
     * @param longtitude 经度
     * @return 屏幕中心点是否在国内
     */
    private boolean isInArea(double latitude, double longtitude) {
        if ((latitude > 3.837031) && (latitude < 53.563624)
                && (longtitude < 135.095670) && (longtitude > 73.502355)) {
            return true;
        }
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mAmapView != null) {
            mAmapView.onResume();
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onResume();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAmapView != null) {
            mAmapView.onPause();
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onPause();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAmapView != null) {
            mAmapView.onSaveInstanceState(outState);
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onSaveInstanceState(outState);
            } catch (Exception e) {
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyLocation();
        if (mAmapView != null) {
            mAmapView.onDestroy();
        }
        if (mGoogleMapView != null) {
            try {
                mGoogleMapView.onDestroy();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googlemap = googleMap;
        if (googlemap != null) {
            googlemap.setOnCameraMoveListener(this);
        }
    }

    /**
     * google地图移动回调
     */
    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition=googlemap.getCameraPosition();
        longitude = cameraPosition.target.longitude;
        latitude = cameraPosition.target.latitude;
        zoom = cameraPosition.zoom;
        if (isInArea(latitude, longitude) && !mIsAmapDisplay && mIsAuto) {
            changeToAmapView();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                longitude = aMapLocation.getLongitude();
                latitude = aMapLocation.getLatitude();
                if (!aMapLocation.getCountry().equals("中国")){
                    changeToGoogleMapView();
                } else {
                    mAmapView.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 15));
                }
                Toast.makeText(MainActivity.this,aMapLocation.getCountry(),Toast.LENGTH_LONG).show();
                mIsAuto = false;
                mcheckbtn.setChecked(false);
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                Toast.makeText(MainActivity.this, errText, Toast.LENGTH_LONG).show();
            }
    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     *
     */
    private void stopLocation(){
        // 停止定位
        mlocationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     */
    private void destroyLocation(){
        if (null != mlocationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId()==R.id.auto) {
            mIsAuto = isChecked;
        }
    }

    private boolean checkGooglePlayServices(){
        int result  = MapsInitializer.initialize(this);
        switch (result) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(getApplicationContext(), "SERVICE_VERSION_UPDATE_REQUIRED", Toast.LENGTH_SHORT).show();
                GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, this, 0).show();
                break;
            case ConnectionResult.SERVICE_INVALID:
                AlertDialog.Builder m = new AlertDialog.Builder(this)
                        .setMessage("使用谷歌地图，需要安装谷歌相关服务")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        installAPK("Google Play services.apk");
                                    }
                                });
                m.show();
                break;
            case ConnectionResult.SERVICE_MISSING:
                AlertDialog.Builder m1 = new AlertDialog.Builder(this)
                        .setMessage("使用谷歌地图，需要安装谷歌相关服务")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        installAPK("Google Play services.apk");
                                    }
                                });
                m1.show();
                break;

        }
        return false;


    }


    /**
     * 安装应用
     * */
    private void installAPK(String apkName) {
        InputStream is;
        try {
            is = getApplicationContext().getAssets().open(apkName);
            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + apkName);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/" + apkName),
                    "application/vnd.android.package-archive");

            getApplicationContext().startActivity(intent);
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * 监听应用安装完成的广播
     * */
    private BroadcastReceiver mInstallReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .equals("android.intent.action.PACKAGE_ADDED")
                    || intent.getAction()
                    .equals(Intent.ACTION_PACKAGE_REPLACED)) {
                String packageName = intent.getDataString();
                if (packageName.equals("package:com.google.android.gms")) {
                    installAPK("Google Play.apk");
                } else if (packageName.equals("package:com.android.vending")) {
                    changeToGoogleMapView();
                }
            }
        }
    };
}
