package com.coding.maintenancehouse.common.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.client.activities.ClientHomeActivity;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class VerifyOTPActivity extends AppCompatActivity {

    ConstraintLayout otpLayout,noInternetLayout;


    ProgressBar loadOtpLayout;

    ImageButton backIcon;
    PinView codeView;
    Button verifyBtn;

    SharedPref sharedPref;

    String userEmail;

    static String savedOTP;

    String fromWhich , userId;

    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        sharedPref = new SharedPref(this);

        init();
    }

    private void init() {
        backIcon = findViewById(R.id.backIcon);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        loadOtpLayout = findViewById(R.id.loadOtpLayout);
        otpLayout = findViewById(R.id.otpLayout);
        codeView = findViewById(R.id.codeView);
        verifyBtn = findViewById(R.id.verifyBtn);

        userEmail = getIntent().getStringExtra("email");

        fromWhich = getIntent().getStringExtra("from");

       userId = getIntent().getStringExtra("userId");

       userRef = FirebaseDatabase.getInstance().getReference("Users");

        new Handler(getMainLooper()).postDelayed(this::checkInternetConnection,1000);

        backIcon.setOnClickListener(v -> onBackPressed());

        verifyBtn.setOnClickListener(v -> verifyCode(Objects.requireNonNull(codeView.getText()).toString()));



    }

    private void checkInternetConnection() {
        new NetworkUtil(internet -> {
            if (internet){
                loadOtpLayout.setVisibility(View.GONE);
                otpLayout.setVisibility(View.VISIBLE);
                sendCodeToUserEmail();
            }
            else {
                new Handler(Looper.getMainLooper()).postDelayed(()->{
                    otpLayout.setVisibility(View.GONE);
                    loadOtpLayout.setVisibility(View.GONE);
                    noInternetLayout.setVisibility(View.VISIBLE);
                } ,500);
                tryCheckAgain();
            }

        });
    }

    private void tryCheckAgain(){
        View view = LayoutInflater.from(VerifyOTPActivity.this)
                .inflate(R.layout.no_internet_try_layout,findViewById(R.id.noInternetLayout));

        view.findViewById(R.id.tryAgainBtn).setOnClickListener(v -> {
            noInternetLayout.setVisibility(View.GONE);
            loadOtpLayout.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(this::checkInternetConnection,1000);
        });
    }

    private void sendCodeToUserEmail() {
        String otpNumber = "123456";

        sharedPref.putString("otp" , otpNumber);


//        JavaMailAPI javaMailAPI = new JavaMailAPI(this,
//                userEmail,
//                "OTP" , sharedPref.getString("otp"));
//
//        javaMailAPI.execute();

        savedOTP = sharedPref.getString("otp");
    }



    @SuppressLint("DefaultLocale")
    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }


    private static String arabicToDecimal(String number) {
        char[] chars = new char[number.length()];
        for(int i=0;i<number.length();i++) {
            char ch = number.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }

    private void verifyCode(String code) {

        if (code.equals(savedOTP)){
            sharedPref.putString("otp" , "");

            if (fromWhich !=null){
                if (fromWhich.equals("login")){

                    userRef.child(userId).child("verified").setValue(true).addOnSuccessListener(unused -> {
                        sharedPref.putBoolean("isLogin", true);
                        sharedPref.putString("role", "client");
                        sharedPref.putString("user_id", userId);
                        startActivity(new Intent(VerifyOTPActivity.this, ClientHomeActivity.class));
                        finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                    });


                }else if (fromWhich.equals("profile")){
                    Intent changePassIntent = new Intent(VerifyOTPActivity.this , ChangePasswordActivity.class);
                    changePassIntent.putExtra("userId" , userId);
                    startActivity(changePassIntent);
                    finish();
                }
            }

        }else {
            Toast.makeText(this, "invalid OTP code", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }
}