package com.coding.maintenancehouse.common.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.authentication.LoginActivity;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;

public class ChangePasswordActivity extends AppCompatActivity {

    String userId;

    ImageButton backIcon;
    EditText passwordEdt,rePasswordEdt;
    Button changePassBtn;

    AlertDialog loadingDialog;

    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sharedPref = new SharedPref(this);

        userId = getIntent().getStringExtra("userId");

        init();
    }

    private void init() {
        backIcon = findViewById(R.id.backIcon);
        passwordEdt = findViewById(R.id.passwordEdt);
        rePasswordEdt = findViewById(R.id.rePasswordEdt);
        changePassBtn = findViewById(R.id.changePassBtn);

        backIcon.setOnClickListener(v -> onBackPressed());

        changePassBtn.setOnClickListener(v -> {
            if (validatePasswords()){
                loadingDialogMethod();
                checkInternetConnection();
            }

        });
    }

    private void checkInternetConnection() {
        new NetworkUtil(internet -> {
            if (internet){
                updateUserPassword();
            }
            else {
               loadingDialog.dismiss();
            }

        });
    }




    private Boolean validatePasswords() {
        String passwordTxt = passwordEdt.getText().toString();
        String rePasswordTxt = rePasswordEdt.getText().toString();

        boolean state = false;

        if (passwordTxt.isEmpty()) {
            passwordEdt.setError("Field cannot be empty");
            state = false;
        } else if (passwordTxt.length() < 8) {
            passwordEdt.setError("Password is too short");
            state = false;
        }if (rePasswordTxt.isEmpty()) {
            rePasswordEdt.setError("Field cannot be empty");
            state = false;
        } else if (rePasswordTxt.length() < 8) {
            rePasswordEdt.setError("Password is too short");
            state = false;
        }else if (!passwordTxt.equals(rePasswordTxt)){
            passwordEdt.setError("not match");
            rePasswordEdt.setError("not match");
        } else {
            passwordEdt.setError(null);
            rePasswordEdt.setError(null);
            state = true;
        }

        return state;
    }

    private void updateUserPassword() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId);
        HashMap<String , Object> map = new HashMap<>();
        map.put("password", passwordEdt.getText().toString());

        userRef.updateChildren(map).addOnSuccessListener(command -> {
            loadingDialog.dismiss();
            logout();
        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ChangePasswordActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

    private void logout(){
        sharedPref.putBoolean("isLogin" , false);
        sharedPref.putString("role","");
        sharedPref.putString("user_id","");
        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finishAffinity();
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