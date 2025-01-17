package com.inhatc.mapsosa;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.kakao.auth.Session;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserMain extends AppCompatActivity implements SensorEventListener, View.OnClickListener{

    public static Context context_user; // context 변수 선언

    FirebaseDatabase myFirebase;                // Firebase object
    DatabaseReference myDB_Reference = null;    // Firebase DB reference

    // HashMap<String, Object> childNode = null;
    HashMap<String, Object> user = null;

    SensorManager objSMG;
    Sensor sensor_Accelerometer;

    Button login_option;
//    Button login_kakao;

    Animation anim;
    ImageView login_executing;

    TextView login_txtHeader;

    String strHeader = "USER";
    String login_strId = null;
    String login_strPwd = null;
    String login_strPhone = null;
    String login_strPhone2 = null;

    int flag = 0; // 0 : 버튼클릭 X, 1 : 회원 로그인, 2 : 카카오 로그인

    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        context_user = this;

        flag = ((homeActivity)homeActivity.context_main).btnFlag;

        //Object for access sensor device
        objSMG = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_Accelerometer = objSMG.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        login_option = (Button) findViewById(R.id.login_option);
        login_option.setOnClickListener(this);
//        login_kakao = (Button) findViewById(R.id.login_kakao);
//        login_kakao.setOnClickListener(this);

        login_txtHeader = (TextView) findViewById(R.id.login_txtHeader);

        // 회원 로그인
        if (flag == 1) {
            login_strId = ((homeActivity)homeActivity.context_main).main_strId;
            login_strPwd = ((homeActivity)homeActivity.context_main).main_strPwd;
        }

        // 카카오 로그인
        else if (flag == 2) {
            Log.d("KAKAO_ID", "nickname: " + login_strId);
            Log.d("homeActivity.KakaoId", "nickname: " + homeActivity.KakaoId);
            login_strId = homeActivity.KakaoId;
        }

        login_txtHeader.setText(login_strId + "님 환영합니다.");

        session = Session.getCurrentSession();

        myFirebase = FirebaseDatabase.getInstance();    // Get FirebaseDatabase instance
        myDB_Reference = myFirebase.getReference();     // Get Firebase reference

        user = new HashMap<>();               // Create HashMap


        mGet_FirebaseDatabase();
        imgRotation(); // executing 이미지 무한 회전
    }

    // executing 이미지 무한 회전
    private void imgRotation() {
        login_executing = (ImageView) findViewById(R.id.login_executing);
        anim = AnimationUtils.loadAnimation(this, R.anim.executing_anim);
        login_executing.setAnimation(anim);
    }

    private void getHashKey() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash123", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_option:
                Intent intent = new Intent(
                        getApplicationContext(), // 현재 화면의 제어권자
                        editPhone.class); // 다음 넘어갈 클래스 지정
                startActivity(intent);
                break;

//            case R.id.login_kakao:
//                sendKaKao();
//                break;

            default:
                break;
        }
    }

    private void sendKaKao() {
        String s = "테스트";
        Log.e("MainActivity :: ", "카카오 메세지 전송시작");
        String Token = session.getAccessToken();
        String reqURL = "https://kakao.com/v2/api/talk/memo/default/send";
        String result = null;

        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder("낙상감지위험",
                        "https://postfiles.pstatic.net/MjAyMTA2MTNfNDgg/MDAxNjIzNTU4Njg1ODE3.pSNW_THy-JH18h5tvhqllKIOeUvgfSD9o7QuIOQLrVkg.FupBHgPNNySfIvQQa-TDQuGAfUZfhRZyW1rIbSC-cUog.PNG.3555186/mapsosa_icon.png?type=w773",
                        LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com").build())
                        .setDescrption("낙상감지 위험 상태입니다")
                        .build())
                .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20)
                        .setSharedCount(30).setViewCount(40).build())
                .addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder().setWebUrl("'https://developers.kakao.com").setMobileWebUrl("'https://developers.kakao.com").build()))
                .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                        .setWebUrl("'https://developers.kakao.com")
                        .setMobileWebUrl("'https://developers.kakao.com")
                        .setAndroidExecutionParams("key1=value1")
                        .setIosExecutionParams("key1=value1")
                        .build()))
                .build();


        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");


        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                Log.e("sendKaKao result :: ", result.toString());
                Log.e("sendKaKao :: ", "카카오 메세지 onSuccess ");

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register Listener for changing sensor value
        objSMG.registerListener(this, sensor_Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        objSMG.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                int xValue = (int) sensorEvent.values[0];
                int yValue = (int) sensorEvent.values[1];
                int zValue = (int) sensorEvent.values[2];

                // default : 25, 25, 25
                if (xValue >= 25 || yValue >= 25 || zValue >= 25){
                    showDialog();
                }
                break;
        }
    }

    void showDialog() {
//        AlertDialog.Builder msgBulder = new AlertDialog.Builder(MainActivity.this)
//                .setTitle("낙상이 감지되었습니다!")
//                .setMessage("위험한 상황입니까?")
//                .setPositiveButton("아니오", new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i){
//                        Intent intent = new Intent(MainActivity.this, MapActivity.class);
//                        startActivity(intent);
//                    }
//                })
//                .setNegativeButton("예", new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i){
//                        Toast.makeText(MainActivity.this, "안 끔", Toast.LENGTH_SHORT).show();
//                    }
//                });
//        AlertDialog msgDlg = msgBulder.create();
//        msgDlg.show();
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setTitle("낙상이 감지되었습니다!")
                .setMessage("위험한 상황입니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(UserMain.this, MapActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("아니오", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            private static final int AUTO_DISMISS_MILLIS = 10000;
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final CharSequence negativeButtonText = defaultButton.getText();
                new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        defaultButton.setText(String.format(
                                Locale.getDefault(), "%s (%d)",
                                negativeButtonText,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                        ));
                    }
                    @Override
                    public void onFinish() {
                        if (((AlertDialog) dialog).isShowing()) {
                            dialog.dismiss();
                            Intent intent = new Intent(UserMain.this, MapActivity.class);
                            startActivity(intent);
                        }
                    }
                }.start();
            }
        });
        dialog.show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Call when sensor accuracy changed
    }

    // Data : Firebase DB -> App
    public void mGet_FirebaseDatabase() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String strKey = postSnapshot.getKey();
                    if (strKey.equals(login_strId)) {
                        login_strPhone = postSnapshot.child("Phone Number").getValue().toString();
                        login_strPhone2 = postSnapshot.child("Phone Number 2").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError dbError) {
                // Failed to read value
                Log.w("TAG : ", "Failed to read value.", dbError.toException());
            }
        };
        Query sortbyName = FirebaseDatabase.getInstance().getReference()
                .child(strHeader).orderByChild(login_strId);
        sortbyName.addListenerForSingleValueEvent(postListener);
    }
}
