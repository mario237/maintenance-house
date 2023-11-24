package com.coding.maintenancehouse.client.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.client.fragments.ClientHomeFragment;
import com.coding.maintenancehouse.client.fragments.ClientOrdersFragment;
import com.coding.maintenancehouse.common.fragments.ProfileFragment;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class ClientHomeActivity extends AppCompatActivity {

    BottomNavigationView client_bottom_nav;

    private long lastPressedTime;
    private static final int PERIOD = 2000;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);


        client_bottom_nav = findViewById(R.id.client_bottom_nav);



        setFragment(new ClientHomeFragment());



        client_bottom_nav.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {

                case R.id.clientHomeFragment:
                    fragment = new ClientHomeFragment();
                    break;

                case R.id.clientOrdersFragment:
                    fragment = new ClientOrdersFragment();
                    break;

                case R.id.clientProfileFragment:
                    fragment = new ProfileFragment();
                    break;


            }
            assert fragment != null;
            setFragment(fragment);
            return true;
        });

        client_bottom_nav.setOnNavigationItemReselectedListener(item -> {
        });

    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.clientHomeContainer, fragment);
        transaction.commit();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!client_bottom_nav.getMenu().findItem(R.id.clientHomeFragment).isChecked()) {
            setFragment(new ClientHomeFragment());
            client_bottom_nav.setSelectedItemId(R.id.clientHomeFragment);
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