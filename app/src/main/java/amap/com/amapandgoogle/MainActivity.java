package amap.com.amapandgoogle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
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
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.animation.Animation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;

public class MainActivity extends FragmentActivity implements View.OnClickListener, AMap.OnCameraChangeListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, AMapLocationListener, CompoundButton.OnCheckedChangeListener {
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
    private AMap amap;
    private GoogleMap googlemap;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AlphaAnimation anappear;
    private AlphaAnimation andisappear;
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
        if (amap == null)
            amap = mAmapView.getMap();

        amap.setOnCameraChangeListener(this);

        anappear = new AlphaAnimation(0,1);
        andisappear = new AlphaAnimation(1,0);
        anappear.setDuration(5000);
        andisappear.setDuration(5000);
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

    private void changeToAmapView() {
        zoom = googlemap.getCameraPosition().zoom;
        latitude = googlemap.getCameraPosition().target.latitude;
        longitude = googlemap.getCameraPosition().target.longitude;
        mapbtn.setText("To Google");
        mAmapView = new TextureMapView(this, new AMapOptions()
                .camera(new com.amap.api.maps.model.CameraPosition(new LatLng(latitude,longitude),zoom,0,0)));
        mAmapView.onCreate(null);
        mAmapView.onResume();
        mContainerLayout.addView(mAmapView, mParams);

        mGoogleMapView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mGoogleMapView.setVisibility(View.GONE);
                 mContainerLayout.removeView(mGoogleMapView);
                if (mGoogleMapView != null) {
                    mGoogleMapView.onDestroy();
                }
            }
        });
        mAmapView.getMap().setOnCameraChangeListener(this);
        mIsAmapDisplay = true;
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
    private void changeToGoogleMapView() {
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

    @Override
    public void onCameraChangeFinish(com.amap.api.maps.model.CameraPosition cameraPosition) {
        longitude = cameraPosition.target.longitude;
        latitude = cameraPosition.target.latitude;
        zoom = cameraPosition.zoom;
        if (!isInArea(latitude, longitude) && mIsAmapDisplay && mIsAuto) {
            changeToGoogleMapView();
        }
    }
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
                    amap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 15));
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
     * @author hongming.wang
     *
     */
    private void stopLocation(){
        // 停止定位
        mlocationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
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
            mlocationClient = null;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId()==R.id.auto) {
            mIsAuto = isChecked;
        }
    }
}
