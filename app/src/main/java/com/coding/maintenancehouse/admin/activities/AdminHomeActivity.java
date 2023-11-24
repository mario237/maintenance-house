package com.coding.maintenancehouse.admin.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.admin.fragments.NewPractitionerFragment;
import com.coding.maintenancehouse.admin.fragments.SearchFragment;
import com.coding.maintenancehouse.authentication.LoginActivity;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class AdminHomeActivity extends AppCompatActivity {

    BottomNavigationView adminBottomNav;

    private long lastPressedTime;
    private static final int PERIOD = 2000;

    Button logoutBtn;

    SharedPref sharedPref;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        sharedPref = new SharedPref(this);


        adminBottomNav = findViewById(R.id.admin_bottom_nav);
        logoutBtn = findViewById(R.id.logoutBtn);


        setFragment(new NewPractitionerFragment());



        adminBottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {

                case R.id.NewPractitionerFragment:
                    fragment = new NewPractitionerFragment();
                    break;

                case R.id.SearchFragment:
                    fragment = new SearchFragment();
                    break;
            }
            assert fragment != null;
            setFragment(fragment);
            return true;
        });

        adminBottomNav.setOnNavigationItemReselectedListener(item -> {
        });

        logoutBtn.setOnClickListener(v -> logout());
    }
    private void logout(){
        sharedPref.putBoolean("isLogin" , false);
        sharedPref.putString("role","");
        sharedPref.putString("user_id","");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finishAffinity();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }


    public void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.adminHomeContainer, fragment);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!adminBottomNav.getMenu().findItem(R.id.NewPractitionerFragment).isChecked()) {
            setFragment(new NewPractitionerFragment());
            adminBottomNav.setSelectedItemId(R.id.NewPractitionerFragment);
        }
        else{  if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getDownTime() - lastPressedTime < PERIOD) {
                    finishAffinity();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.press_back_again),
                            Toast.LENGTH_SHORT).show();
                    lastPressedTime = event.getEventTime();
                }
                return true;
            }
        }
        }
        return false;
    }
}