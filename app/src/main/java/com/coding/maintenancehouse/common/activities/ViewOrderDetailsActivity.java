package com.coding.maintenancehouse.common.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.common.adapter.OrderImagesAdapter;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.OrderModel;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class ViewOrderDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPref sharedPref;

    String orderId, clientPhoneNumber, practitionerId;

    ConstraintLayout viewOrderLayout, noInternetLayout;

    ImageButton backIcon;

    RecyclerView orderImagesRecycler;

    TextView usernameTv, userPhoneTv, userAddressTv, orderTimeText, orderDateText, orderDescText;

    Button acceptBtn, cancelBtn, callBtn , finishBtn;

    ProgressBar loadOrderProgress;

    OrderImagesAdapter orderImagesAdapter;

    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order_details);

        orderId = getIntent().getStringExtra("orderId");

        sharedPref = new SharedPref(this);

        practitionerId = sharedPref.getString("user_id");

        init();

        showOrder();


    }


    private void init() {
        backIcon = findViewById(R.id.backIcon);

        noInternetLayout = findViewById(R.id.noInternetLayout);

        viewOrderLayout = findViewById(R.id.viewOrderLayout);

        orderImagesRecycler = findViewById(R.id.orderImagesRecycler);
        orderImagesRecycler.setHasFixedSize(true);
        orderImagesRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        usernameTv = findViewById(R.id.usernameTv);
        userPhoneTv = findViewById(R.id.userPhoneTv);
        userAddressTv = findViewById(R.id.userAddressTv);
        orderTimeText = findViewById(R.id.orderTimeText);
        orderDateText = findViewById(R.id.orderDateText);
        orderDescText = findViewById(R.id.orderDescText);

        acceptBtn = findViewById(R.id.acceptBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        finishBtn = findViewById(R.id.finishBtn);

        callBtn = findViewById(R.id.callBtn);

        loadOrderProgress = findViewById(R.id.loadOrderProgress);

        backIcon.setOnClickListener(this);
        acceptBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        finishBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backIcon)
            onBackPressed();
        if (v.getId() == R.id.acceptBtn)
            acceptClientOrder();
        if (v.getId() == R.id.cancelBtn)
            cancelClientOrder();
        if (v.getId() == R.id.callBtn)
            callClient();
        if (v.getId() == R.id.finishBtn)
            finishOrder();
    }

    private void finishOrder() {

        loadingDialogMethod();

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Services").child(orderId).child("state");

        orderRef.setValue("Done").addOnSuccessListener(unused -> loadingDialog.dismiss())
        .addOnFailureListener(e -> {
            Toast.makeText(ViewOrderDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
        });


        onBackPressed();
    }


    private void getOrderData() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Services").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OrderModel orderModel = snapshot.getValue(OrderModel.class);
                assert orderModel != null;

                if(!orderModel.getState().equals("Pending") && !orderModel.getState().equals("done")){
                    finishBtn.setVisibility(View.VISIBLE);
                }

                if (orderModel.getImages() != null) {
                    orderImagesAdapter = new OrderImagesAdapter(ViewOrderDetailsActivity.this
                            , orderModel.getImages());
                    orderImagesRecycler.setAdapter(orderImagesAdapter);

                }else {
                    orderImagesRecycler.setVisibility(View.GONE);
                }


                orderDateText.setText(orderModel.getDate());

                orderTimeText.setText(
                        getResources().getString(R.string.from) + " " + orderModel.getFrom()
                                + " " + getResources().getString(R.string.to)
                                + " " + orderModel.getTo()
                );

                orderDescText.setText(orderModel.getDescription());

                if (!orderModel.getPractitionerId().isEmpty()) {
                    acceptBtn.setVisibility(View.GONE);
                    cancelBtn.setVisibility(View.VISIBLE);
                } else {
                    acceptBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.GONE);
                }

                getUserData(orderModel.getUserId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);

                assert userModel != null;
                usernameTv.setText(userModel.getUsername());

                clientPhoneNumber = userModel.getPhoneNumber();
                userPhoneTv.setText(clientPhoneNumber);

                userAddressTv.setText(userModel.getAddress());

                loadOrderProgress.setVisibility(View.GONE);
                viewOrderLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showOrder() {
        new NetworkUtil(internet -> {
            if (internet) {
                getOrderData();
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadOrderProgress.setVisibility(View.GONE);
                    noInternetLayout.setVisibility(View.VISIBLE);
                }, 500);
                tryCheckAgain();
            }

        });
    }

    private void tryCheckAgain() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.no_internet_try_layout, findViewById(R.id.noInternetLayout));

        view.findViewById(R.id.tryAgainBtn).setOnClickListener(v -> {
            noInternetLayout.setVisibility(View.GONE);
            loadOrderProgress.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(this::showOrder, 1000);
        });
    }


    private void acceptClientOrder() {
        loadingDialogMethod();

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Services").child(orderId);

        HashMap<String, Object> updateOrderData = new HashMap<>();

        updateOrderData.put("practitionerId", practitionerId);
        updateOrderData.put("state", "Accepted");

        orderRef.updateChildren(updateOrderData).addOnSuccessListener(aVoid -> {
            acceptBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.VISIBLE);
            loadingDialog.dismiss();
        });
    }

    private void cancelClientOrder() {

        loadingDialogMethod();

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Services").child(orderId);

        HashMap<String, Object> updateOrderData = new HashMap<>();

        updateOrderData.put("practitionerId", "");
        updateOrderData.put("state", "Pending");

        orderRef.updateChildren(updateOrderData).addOnSuccessListener(aVoid -> {
            acceptBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.GONE);
            loadingDialog.dismiss();
        });

    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ViewOrderDetailsActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

    private void callClient() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + clientPhoneNumber));
        startActivity(intent);
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