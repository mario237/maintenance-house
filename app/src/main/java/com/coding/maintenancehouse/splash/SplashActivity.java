package com.coding.maintenancehouse.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.admin.activities.AdminHomeActivity;
import com.coding.maintenancehouse.authentication.LoginActivity;
import com.coding.maintenancehouse.client.activities.ClientHomeActivity;
import com.coding.maintenancehouse.practitioner.activities.PractitionerHomeActivity;
import com.coding.maintenancehouse.helpers.SharedPref;

public class SplashActivity extends AppCompatActivity {

    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        sharedPref = new SharedPref(this);

       new Handler(getMainLooper()).postDelayed(() -> {
           if (sharedPref.getBoolean("isLogin")){
               switch (sharedPref.getString("role")) {
                   case "client": {
                       Intent loginIntent = new Intent(SplashActivity.this, ClientHomeActivity.class);
                       startActivity(loginIntent);
                       finish();
                       overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                       break;
                   }
                   case "practitioner": {
                       Intent loginIntent = new Intent(SplashActivity.this, PractitionerHomeActivity.class);
                       startActivity(loginIntent);
                       finish();
                       overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                       break;
                   }
                   case "admin": {
                       Intent loginIntent = new Intent(SplashActivity.this, AdminHomeActivity.class);
                       startActivity(loginIntent);
                       finish();
                       overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                       break;
                   }
               }
           }
           else {
               Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
               startActivity(loginIntent);
               finish();
               overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
           }

       },2000);

    }

}