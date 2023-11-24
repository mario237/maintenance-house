package com.coding.maintenancehouse.practitioner.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.common.fragments.ProfileFragment;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.practitioner.fragments.PractitionerHomeFragment;
import com.coding.maintenancehouse.practitioner.fragments.PractitionerJobsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class PractitionerHomeActivity extends AppCompatActivity {

    BottomNavigationView practitioner_bottom_nav;

    private long lastPressedTime;
    private static final int PERIOD = 2000;


    @SuppressWarnings("deprecation")
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practitioner_home);

        practitioner_bottom_nav = findViewById(R.id.practitioner_bottom_nav);

        setFragment(new PractitionerHomeFragment());



        practitioner_bottom_nav.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {

                case R.id.practitionerHomeFragment:
                    fragment = new PractitionerHomeFragment();
                    break;

                case R.id.practitionerJobsFragment:
                    fragment = new PractitionerJobsFragment();
                    break;

                case R.id.practitionerProfileFragment:
                    fragment = new ProfileFragment();
                    break;


            }
            assert fragment != null;
            setFragment(fragment);
            return true;
        });

        practitioner_bottom_nav.setOnNavigationItemReselectedListener(item -> {
        });
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.practitionerHomeContainer, fragment);
        transaction.commit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!practitioner_bottom_nav.getMenu().findItem(R.id.practitionerHomeFragment).isChecked()) {
            setFragment(new PractitionerHomeFragment());
            practitioner_bottom_nav.setSelectedItemId(R.id.practitionerHomeFragment);
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