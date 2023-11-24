package com.coding.maintenancehouse.admin.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.models.OrderModel;
import com.coding.maintenancehouse.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;


public class SearchFragment extends Fragment {

    Context context;

    ProgressBar loadOrdersInfoProgress;
    EditText orderIdEdt;
    ImageButton searchBtn;

    CardView orderCard, userCard, practitionerCard;

    TextView orderIdTv, orderCategoryTv, orderDateTv, orderTimeTv, orderDescTv,
            usernameTv, userEmailTv, userPhoneTv, userAddressTv,
            practitionerNameTv,practitionerEmailTv,practitionerPhoneTv,practitionerAddressTv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        context = requireContext();

        init(view);

        return view;
    }

    private void init(View view) {
        loadOrdersInfoProgress = view.findViewById(R.id.loadOrdersInfoProgress);
        orderIdEdt = view.findViewById(R.id.orderIdEdt);
        searchBtn = view.findViewById(R.id.searchBtn);

        //Cards
        orderCard = view.findViewById(R.id.orderCard);
        userCard = view.findViewById(R.id.userCard);
        practitionerCard = view.findViewById(R.id.practitionerCard);


        //Order Card
        orderIdTv = view.findViewById(R.id.orderIdTv);
        orderCategoryTv = view.findViewById(R.id.orderCategoryTv);
        orderDateTv = view.findViewById(R.id.orderDateTv);
        orderTimeTv = view.findViewById(R.id.orderTimeTv);
        orderDescTv = view.findViewById(R.id.orderDescTv);


        //User Card
        usernameTv = view.findViewById(R.id.usernameTv);
        userEmailTv = view.findViewById(R.id.userEmailTv);
        userPhoneTv = view.findViewById(R.id.userPhoneTv);
        userAddressTv = view.findViewById(R.id.userAddressTv);


        //Practitioner Card
        practitionerNameTv = view.findViewById(R.id.practitionerNameTv);
        practitionerEmailTv = view.findViewById(R.id.practitionerEmailTv);
        practitionerPhoneTv = view.findViewById(R.id.practitionerPhoneTv);
        practitionerAddressTv = view.findViewById(R.id.practitionerAddressTv);

        searchBtn.setOnClickListener(v -> getOrderDetails(orderIdEdt.getText().toString()));
    }

    private void getOrderDetails(String orderID) {
        if (orderID.isEmpty()) {
            orderIdEdt.setError("Field is required");
        } else {

            loadOrdersInfoProgress.setVisibility(View.VISIBLE);

            DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Services").child(orderID);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");


            orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        OrderModel orderModel = snapshot.getValue(OrderModel.class);

                        assert orderModel != null;

                        orderIdTv.setText(orderModel.getOrderId());
                        orderCategoryTv.setText(orderModel.getCategory());
                        orderDateTv.setText(orderModel.getDate());
                        orderTimeTv.setText(
                                "From " + orderModel.getFrom() + " " + " To " + orderModel.getTo()
                        );

                        orderDescTv.setText(orderModel.getDescription());


                        userRef.child(orderModel.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                               if(snapshot.exists()){
                                   UserModel userModel = snapshot.getValue(UserModel.class);

                                   assert userModel != null;
                                   usernameTv.setText(userModel.getUsername());
                                   userEmailTv.setText(userModel.getEmailAddress());
                                   userPhoneTv.setText(userModel.getPhoneNumber());
                                   userAddressTv.setText(userModel.getAddress());


                                   userCard.setVisibility(View.VISIBLE);

                               }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        if(!orderModel.getPractitionerId().equals("")){
                            userRef.child(orderModel.getPractitionerId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        UserModel userModel = snapshot.getValue(UserModel.class);

                                        assert userModel != null;
                                        practitionerNameTv.setText(userModel.getUsername());
                                        practitionerEmailTv.setText(userModel.getEmailAddress());
                                        practitionerPhoneTv.setText(userModel.getPhoneNumber());
                                        practitionerAddressTv.setText(userModel.getAddress());


                                        practitionerCard.setVisibility(View.VISIBLE);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });

                        }

                        loadOrdersInfoProgress.setVisibility(View.GONE);
                        orderCard.setVisibility(View.VISIBLE);

                    } else {
                        Toast.makeText(context, "order not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }
}