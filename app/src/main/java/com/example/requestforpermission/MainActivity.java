package com.example.requestforpermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Button camera_preview_button;
    private static Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static MainActivity getInstance;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1001; //권한설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 카메라 프리뷰를  전체화면으로 보여주기 위해 셋팅한다.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // 안드로이드 6.0 이상 버전에서는 CAMERA 권한 허가를 요청한다.
        requestPermissionCamera();
    }
    public static Camera getCamera(){
        return mCamera;
    }
    private void setInit(){
        getInstance = this;

        // 카메라 객체를 R.layout.activity_main의 레이아웃에 선언한 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
        mCamera = Camera.open();

        setContentView(R.layout.activity_main);

        // SurfaceView를 상속받은 레이아웃을 정의한다.
        surfaceView = (CameraPreview) findViewById(R.id.preview);


        // SurfaceView 정의 - holder와 Callback을 정의한다.
        holder = surfaceView.getHolder();
        holder.addCallback(surfaceView);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //권한 확인
    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {

            int permssionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
            if (permssionCheck!= PackageManager.PERMISSION_GRANTED) {
                final Toast unlockMessage = Toast.makeText(this,"카메라 권한 승인이 필요합니다.",Toast.LENGTH_LONG);
                unlockMessage.show();
                new CountDownTimer(2500, 1000)
                {
                    public void onTick(long millisUntilFinished) {unlockMessage.show();}
                    public void onFinish() {
                        unlockMessage.show();
                        //권한 거부시 앱재실행할때 다시 요청 페이지 띄움
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                }.start();
            }else {//권한 승인 되어 있을시
                setInit();
            }

        }else{  // version 6 이하일때
            setInit();
            return true;
        }

        return true;
    }


    //권한 요청 클릭시
    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // 승인허가
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"승인이 허가 되었습니다.",Toast.LENGTH_LONG).show();
                    restart();
                }
                //승인거부
                else if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                    Toast.makeText(this,"승인이 거부 되었습니다.\n"+"사용하실수 없습니다.",Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable()
                    {
                        //여기에 딜레이 후 시작할 작업들을 입력
                        @Override
                        public void run()
                        {
                            //거부시 다시 요청 페이지 나옴
                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
                    }, 6000);// 0.5초 정도 딜레이를 준 후 시작
                }
                //다시묻지 않음 승인거부
                else{
                    Toast.makeText(this,"다시묻지 않음으로 승인이 거부 되었습니다.\n"+"사용하실수 없습니다.",Toast.LENGTH_LONG).show();
                    final Toast unlockMessage1 = Toast.makeText(this, "앱을 지우고 다시설치하셔야 권한 페이지가 나옵니다.", Toast.LENGTH_LONG);
                    final Toast unlockMessage2 = Toast.makeText(this, "아니면 설정페이지 -> 애플리케이션에서 권한을 설정해주세요.\n"+
                            "그래야 사용하실수 있습니다.", Toast.LENGTH_LONG);
                    unlockMessage1.show();
                    unlockMessage2.show();
                    new CountDownTimer(7500, 1000)
                    {
                        public void onTick(long millisUntilFinished) {unlockMessage1.show();}
                        public void onFinish() {
                            unlockMessage1.show();

                            new CountDownTimer(7500, 1000)
                            {
                                public void onTick(long millisUntilFinished) {unlockMessage2.show();}
                                public void onFinish() {
                                    unlockMessage2.show();

                                    new Handler().postDelayed(new Runnable()
                                    {
                                        //여기에 딜레이 후 시작할 작업들을 입력
                                        @Override
                                        public void run(){goPermissionSetting();}//애플리케이션 정보로 이동
                                    }, 1500);// 0.5초 정도 딜레이를 준 후 시작

                                }
                            }.start();
                        }
                    }.start();

                }
                return;
            }
        }
    }

    //앱 다시 시작
    public void restart(){
        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    //애플리케이션 정보로 이동
    private void goPermissionSetting(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

}