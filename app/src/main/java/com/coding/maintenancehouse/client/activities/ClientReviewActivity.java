package com.coding.maintenancehouse.client.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ClientReviewActivity extends AppCompatActivity {

    String userId, orderId, emailMessage;
    ImageButton backIcon;
    RatingBar practitionerRateBar, serviceRateBar, applicationRateBar;
    EditText reviewMessageEdt;
    Button sendReviewBtn;

    AlertDialog networkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_review);

        userId = getIntent().getStringExtra("userId");
        orderId = getIntent().getStringExtra("orderId");

        init();

    }

    private void init() {
        backIcon = findViewById(R.id.backIcon);

        practitionerRateBar = findViewById(R.id.practitionerRateBar);
        serviceRateBar = findViewById(R.id.serviceRateBar);
        applicationRateBar = findViewById(R.id.applicationRateBar);

        reviewMessageEdt = findViewById(R.id.reviewMessageEdt);

        sendReviewBtn = findViewById(R.id.sendReviewBtn);

        backIcon.setOnClickListener(v -> onBackPressed());

        sendReviewBtn.setOnClickListener(v -> checkInternetConnection());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void checkInternetConnection() {
        new NetworkUtil(internet -> {
            if (internet) {
                sendMessage();
            } else {
                new Handler(getMainLooper()).postDelayed(this::networkErrorDialogMethod, 1000);
            }

        });
    }

    private void networkErrorDialogMethod() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ClientReviewActivity.this);


        LayoutInflater.from(ClientReviewActivity.this)
                .inflate(R.layout.no_internet, findViewById(R.id.no_internet));


        builder.setView(R.layout.no_internet);

        networkDialog = builder.create();


        networkDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        networkDialog.show();


    }


    private void sendMessage() {
        if (practitionerRateBar.getRating() != 0.0f && serviceRateBar.getRating() != 0.0f
                && applicationRateBar.getRating() != 0.0f){
            if (userId != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        emailMessage =
                                "Review information:" +
                                        "\nOrder ID: " + orderId +
                                        "\nPractitioner Rate: " + practitionerRateBar.getRating() + "/5.0" +
                                        "\nService Rate: " + serviceRateBar.getRating() + "/5.0" +
                                        "\nApplication Rate: " + applicationRateBar.getRating() + "/5.0" +
                                        "\nReview Message: " + reviewMessageEdt.getText().toString().trim() +
                                        "\n---------------------------------------------------------------";


                        assert userModel != null;
//                        JavaMailAPI javaMailAPI = new JavaMailAPI(ClientReviewActivity.this,
//                                "maintenancehouse.sup@gmail.com",
//                                "Review from " + userModel.getUsername(), emailMessage , ClientReviewActivity.this);
//
//                        javaMailAPI.execute();

                        reviewMessageEdt.setText("");
                        practitionerRateBar.setRating(0.0f);
                        serviceRateBar.setRating(0.0f);
                        applicationRateBar.setRating(0.0f);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }else {
            Toast.makeText(this, "make a review", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }
}