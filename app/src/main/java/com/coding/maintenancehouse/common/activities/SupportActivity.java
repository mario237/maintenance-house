package com.coding.maintenancehouse.common.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.common.adapter.SelectFromOrdersAdapter;
import com.coding.maintenancehouse.helpers.JavaMailAPI;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.models.OrderModel;
import com.coding.maintenancehouse.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SupportActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView backIcon;
    EditText orderDetailsEdt, supportEdt;
    Button sendMessageBtn;

    String userId, username, email, phone, message,
            practitionerName = "", orderId, orderDescription, orderCategory,
            orderDate, orderTime;


    List<OrderModel> orderModelList;

    SelectFromOrdersAdapter selectFromOrdersAdapter;

    AlertDialog selectOrderDialog;

    boolean isOtherProblem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        init();

    }


    private void init() {
        backIcon = findViewById(R.id.backIcon);
        orderDetailsEdt = findViewById(R.id.orderDetailsEdt);
        supportEdt = findViewById(R.id.supportEdt);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);

        //add Views To OnClick
        backIcon.setOnClickListener(this);
        orderDetailsEdt.setOnClickListener(this);
        sendMessageBtn.setOnClickListener(this);

        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backIcon)
            onBackPressed();
        if (v.getId() == R.id.orderDetailsEdt)
            openSelectOrderDialog();
        if (v.getId() == R.id.sendMessageBtn)
            sendMessage();
    }

    private void onOtherCardClick() {
        orderDetailsEdt.setText(getResources().getString(R.string.other_not_order));
        isOtherProblem = true;
        selectOrderDialog.dismiss();
    }


    private void sendMessage() {

        if (supportEdt.getText().toString().isEmpty()) {
            supportEdt.setError("Field is required");
        } else {
            supportEdt.setError(null);

            if (isOtherProblem) {
                message =
                        "Sender information:" +
                                "\n\nusername: " + username + "\n\nemail: " + email + "\n\nphone: " + phone
                                + "\n\nmessage: " + supportEdt.getText().toString() +
                                "\n---------------------------------------------------------------";


                JavaMailAPI javaMailAPI = new JavaMailAPI(this,
                        "maintenancehouse.sup@gmail.com",
                        "Report from " + username, message , SupportActivity.this);

                javaMailAPI.execute();

                supportEdt.setText("");

                isOtherProblem = false;

            } else {
                boolean isValid = checkFormValidation(
                        orderId, orderCategory, orderDescription, orderDate, orderTime, practitionerName);

                if (isValid) {
                    message =
                            "Sender information:" +
                                    "\n\nusername: " + username + "\n\nemail: " + email + "\n\nphone: " + phone
                                    + "\n\nmessage: " + supportEdt.getText().toString() +
                                    "\n---------------------------------------------------------------" +
                                    "\n\nOrder information:" +
                                    "\n\nOrder Id: " + orderId +
                                    "\n\nOrder Category: " + orderCategory +
                                    "\n\nOrder Description: " + orderDescription +
                                    "\n\nOrder Date: " + orderDate +
                                    "\n\nOrder Time: " + orderTime +
                                    "\n\nPractitioner name: " + practitionerName +
                                    "\n---------------------------------------------------------------";

                    JavaMailAPI javaMailAPI = new JavaMailAPI(this,
                            "maintenancehouse.sup@gmail.com",
                            "Report from " + username, message,SupportActivity.this);

                    javaMailAPI.execute();

                    supportEdt.setText("");

                } else {
                    Toast.makeText(this, "Please check all fields", Toast.LENGTH_SHORT).show();
                }

            }


        }


    }


    private void openSelectOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.select_from_orders, null);

        builder.setView(dialogView);

        RecyclerView ordersRecycleView = dialogView.findViewById(R.id.ordersRecycleView);
        ordersRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecycleView.setHasFixedSize(true);


        ProgressBar loadOrderProgress = dialogView.findViewById(R.id.loadOrderProgress);


        CardView otherCard = dialogView.findViewById(R.id.otherCard);

        otherCard.setOnClickListener(v -> onOtherCardClick());

        selectOrderDialog = builder.create();
        selectOrderDialog.show();


        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("Services");

        orderModelList = new ArrayList<>();

        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);

                    assert orderModel != null;
                    if (orderModel.getUserId().equals(userId) || orderModel.getPractitionerId().equals(userId)) {
                        orderModelList.add(orderModel);
                    }
                }

                selectFromOrdersAdapter = new SelectFromOrdersAdapter(SupportActivity.this, orderModelList);
                ordersRecycleView.setAdapter(selectFromOrdersAdapter);

                selectFromOrdersAdapter.setOnItemClickListener(position -> selectOrderData(position));

                loadOrderProgress.setVisibility(View.GONE);
                ordersRecycleView.setVisibility(View.VISIBLE);
                otherCard.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void selectOrderData(int position) {
        orderDetailsEdt.setText(orderModelList.get(position).getCategory());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    assert userModel != null;
                    if (!orderModelList.get(position).getPractitionerId().equals("")) {
                        if (userModel.getUserId().equals(orderModelList.get(position).getPractitionerId())) {
                            practitionerName = userModel.getUsername();
                        }
                    } else {
                        practitionerName = "Not accepted by any practitioner";
                    }
                }


                orderId = orderModelList.get(position).getOrderId();
                orderCategory = orderModelList.get(position).getCategory();
                orderDescription = orderModelList.get(position).getDescription();
                orderDate = orderModelList.get(position).getDate();
                orderTime = "From" + " " + orderModelList.get(position).getFrom() +
                        " " + "To" + " " + orderModelList.get(position).getTo();

                checkFormValidation(
                        orderId,
                        orderCategory,
                        orderDescription,
                        orderDate,
                        orderTime,
                        practitionerName
                );


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        selectOrderDialog.dismiss();
    }

    private boolean checkFormValidation(String orderId, String orderCategory, String orderDescription, String orderDate, String orderTime, String practitionerName) {

        return orderId != null && orderCategory != null
                && orderDescription != null && orderDate != null && orderTime != null
                && !supportEdt.getText().toString().isEmpty() && !practitionerName.equals("");
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