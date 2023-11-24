package com.coding.maintenancehouse.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.admin.activities.AdminHomeActivity;
import com.coding.maintenancehouse.client.activities.ClientHomeActivity;
import com.coding.maintenancehouse.common.activities.VerifyOTPActivity;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.coding.maintenancehouse.practitioner.activities.PractitionerHomeActivity;
import com.coding.maintenancehouse.splash.SplashActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    EditText emailEdt, passwordEdt;
    ImageButton practitionerImage, clientImage;
    TextView forgetPassTv;
    Button loginBtn;


    AlertDialog loadingDialog, networkDialog, emailCheckDialog;

    SharedPref sharedPref;

    boolean userExist = false , isVerified = false;

    String role, userId , userEmail;

    ImageButton arabicImage, englishImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        Locale current = getResources().getConfiguration().getLocales().get(0);


        if (current.toString().equals("en")) {
            englishImage.setVisibility(View.GONE);
            arabicImage.setVisibility(View.VISIBLE);
        }else if (current.toString().equals("ar")){
            arabicImage.setVisibility(View.GONE);
            englishImage.setVisibility(View.VISIBLE);
        }
    }

    private void init() {

        arabicImage = findViewById(R.id.arabicImage);
        englishImage = findViewById(R.id.englishImage);

        emailEdt = findViewById(R.id.emailEdt);
        passwordEdt = findViewById(R.id.passwordEdt);
        forgetPassTv = findViewById(R.id.forgetPassTv);
        loginBtn = findViewById(R.id.loginBtn);
        practitionerImage = findViewById(R.id.practitionerImage);
        clientImage = findViewById(R.id.clientImage);


        //add views to onClick method

        arabicImage.setOnClickListener(this);
        englishImage.setOnClickListener(this);

        forgetPassTv.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        practitionerImage.setOnClickListener(this);
        clientImage.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.arabicImage)
            changeLanguage("ar");

        if (v.getId() == R.id.englishImage)
            changeLanguage("en");

        if (v.getId() == R.id.forgetPassTv)
            showEmailCheckDialog();
        if (v.getId() == R.id.loginBtn)
            loginToMainPage();
        if (v.getId() == R.id.practitionerImage)
            goToRegisterPage("pending");
        if (v.getId() == R.id.clientImage)
            goToRegisterPage("client");

    }

    private void changeLanguage(String language) {
        LocaleHelper.setLocale(getApplicationContext(), language);
        restartApp();
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void showEmailCheckDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View view = LayoutInflater.from(LoginActivity.this).inflate(
                R.layout.email_check_dialog_style, findViewById(R.id.emailCheckLayout));
        builder.setView(view);

        emailCheckDialog = builder.create();

        EditText emailEdt = view.findViewById(R.id.emailEdt);

        Button sendOTPBtn = view.findViewById(R.id.sendOTPBtn);

        sendOTPBtn.setOnClickListener(v -> checkEmailExistence(emailEdt.getText().toString()));

        emailCheckDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        emailCheckDialog.show();
    }

    private void checkEmailExistence(String email) {

        if (email.isEmpty()) {
            Toast.makeText(this, "enter email", Toast.LENGTH_SHORT).show();
        } else {
            emailCheckDialog.dismiss();
            loadingDialogMethod();
            new NetworkUtil(internet -> {
                if (internet) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                assert userModel != null;
                                if (userModel.getEmailAddress().equals(email)) {
                                    userExist = true;
                                    userId = userModel.getUserId();
                                }
                            }

                            if (userExist) {
                                loadingDialog.dismiss();
                                Intent verifyOTPIntent = new Intent(LoginActivity.this, VerifyOTPActivity.class);
                                verifyOTPIntent.putExtra("userId", userId);
                                verifyOTPIntent.putExtra("email", email);
                                verifyOTPIntent.putExtra("from","login");
                                startActivity(verifyOTPIntent);
                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "email not found", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    new Handler(getMainLooper()).postDelayed(() -> {
                        loadingDialog.dismiss();
                        networkErrorDialogMethod();
                    }, 1000);
                }

            });
        }

    }

    private void loginToMainPage() {
        if (validateEmail() && validatePassword()) {
            checkInternetConnection();
        } else {
            Toast.makeText(this, getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInternetConnection() {
        loadingDialogMethod();
        new NetworkUtil(internet -> {
            if (internet) {
                checkUserExistence();
            } else {
                new Handler(getMainLooper()).postDelayed(() -> {
                    loadingDialog.dismiss();
                    networkErrorDialogMethod();
                }, 1000);
            }

        });
    }


    private void checkUserExistence() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    assert userModel != null;

                    if (userModel.getEmailAddress().equals(emailEdt.getText().toString())
                            && userModel.getPassword().equals(passwordEdt.getText().toString())) {

                        role = userModel.getRole();
                        userId = userModel.getUserId();

                        userExist = true;

                        isVerified = userModel.isVerified();

                        userId = userModel.getUserId();

                        userEmail = userModel.getEmailAddress();
                    }

                }



                if (userExist){
                    if (role.equals("client")){
                        if (isVerified){
                            loadingDialog.dismiss();
                            sharedPref.putBoolean("isLogin", true);
                            sharedPref.putString("role", "client");
                            sharedPref.putString("user_id", userId);
                            loadingDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, ClientHomeActivity.class));
                            finish();
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                        else {
                            loadingDialog.dismiss();
                            Intent verifyOTPIntent = new Intent(LoginActivity.this , VerifyOTPActivity.class);
                            verifyOTPIntent.putExtra("email" , userEmail);
                            verifyOTPIntent.putExtra("userId" , userId);
                            verifyOTPIntent.putExtra("from" , "login");
                            startActivity(verifyOTPIntent);
                        }
                    }
                    else {
                        checkUserRole(role);
                    }
                }else {
                    loadingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "wrong email or password", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserRole(String userRole) {

        switch (userRole) {
            case "practitioner":
                sharedPref.putBoolean("isLogin", true);
                sharedPref.putString("role", "practitioner");
                sharedPref.putString("user_id", userId);
                loadingDialog.dismiss();
                startActivity(new Intent(LoginActivity.this, PractitionerHomeActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case "admin":
                sharedPref.putBoolean("isLogin", true);
                sharedPref.putString("role", "admin");
                sharedPref.putString("adminId", userId);
                loadingDialog.dismiss();
                startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            default:
                loadingDialog.dismiss();
                Toast.makeText(this, "your account is under review", Toast.LENGTH_SHORT).show();
                break;
        }

    }


    private Boolean validateEmail() {
        String emailTxt = emailEdt.getText().toString();

        if (emailTxt.isEmpty()) {
            emailEdt.setError("Field cannot be empty");

            return false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            emailEdt.setError("Invalid email address");

            return false;
        } else {
            emailEdt.setError(null);
            return true;
        }

    }

    private Boolean validatePassword() {
        String passwordTxt = passwordEdt.getText().toString();


        if (passwordTxt.isEmpty()) {
            passwordEdt.setError("Field cannot be empty");

            return false;
        } else if (passwordTxt.length() < 8) {
            passwordEdt.setError("Password is too short");

            return false;
        } else {
            passwordEdt.setError(null);
            return true;
        }

    }


    private void goToRegisterPage(String userRole) {
        Intent registrationIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        registrationIntent.putExtra("userRole", userRole);
        startActivity(registrationIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

    private void networkErrorDialogMethod() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);


        LayoutInflater.from(LoginActivity.this)
                .inflate(R.layout.no_internet, findViewById(R.id.no_internet));


        builder.setView(R.layout.no_internet);

        networkDialog = builder.create();


        networkDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        networkDialog.show();


    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }

}