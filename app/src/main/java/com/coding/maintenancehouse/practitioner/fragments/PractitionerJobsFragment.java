package com.coding.maintenancehouse.practitioner.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.common.activities.ViewOrderDetailsActivity;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.OrderModel;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.coding.maintenancehouse.practitioner.adapters.PractitionerHomeAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class PractitionerJobsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    SharedPref sharedPref;

    ConstraintLayout noInternetLayout;
    SwipeRefreshLayout practitionerJobsRefresh;
    ProgressBar loadOrdersProgress;
    RecyclerView practitionerJobsRecycler;
    ConstraintLayout haveNoOrdersLayout;
    DatabaseReference userRef, orderRef;
    List<OrderModel> orderModelList;
    String currentUserId, userCategory;
    PractitionerHomeAdapter practitionerHomeAdapter;

    boolean refreshJobs = false;

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_practitioner_jobs, container, false);

        context = requireContext();


        sharedPref = new SharedPref(context);


        init(view);

        showOrders();

        return view;
    }

    private void init(View v) {
        practitionerJobsRefresh = v.findViewById(R.id.practitionerJobsRefresh);
        loadOrdersProgress = v.findViewById(R.id.loadOrdersProgress);
        haveNoOrdersLayout = v.findViewById(R.id.haveNoOrdersLayout);

        noInternetLayout = v.findViewById(R.id.noInternetLayout);

        practitionerJobsRefresh.setOnRefreshListener(this);

        practitionerJobsRecycler = v.findViewById(R.id.practitionerJobsRecycler);
        practitionerJobsRecycler.setHasFixedSize(true);
        practitionerJobsRecycler.setLayoutManager(new LinearLayoutManager(context));
        orderModelList = new ArrayList<>();

        currentUserId = sharedPref.getString("user_id");
    }

    private void addOrdersToRecyclerView() {
        if (currentUserId != null) {

            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    assert userModel != null;
                    userCategory = userModel.getCategory();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            orderRef = FirebaseDatabase.getInstance().getReference("Services");


            orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SimpleDateFormat")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    orderModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);
                        assert orderModel != null;
                        if (orderModel.getCategory().equals(userCategory) &&
                                orderModel.getPractitionerId().equals(currentUserId) && !orderModel.getState().equals("Done"))
                            orderModelList.add(orderModel);
                    }

                    if (!orderModelList.isEmpty()) {


                        orderModelList.sort((o1, o2) -> sortByDate(o1, o2));


                        orderModelList.sort((o1, o2) -> o2.getUrgent().compareTo(o1.getUrgent()));


                        practitionerHomeAdapter = new PractitionerHomeAdapter(requireActivity(), orderModelList);

                        practitionerJobsRecycler.setAdapter(practitionerHomeAdapter);

                        practitionerHomeAdapter.setOnItemClickListener(position -> {
                            Intent viewOrderIntent = new Intent(context, ViewOrderDetailsActivity.class);
                            refreshJobs = true;
                            viewOrderIntent.putExtra("orderId", orderModelList.get(position).getOrderId());
                            requireActivity().startActivity(viewOrderIntent);
                        });

                        loadOrdersProgress.setVisibility(View.GONE);
                        haveNoOrdersLayout.setVisibility(View.GONE);
                        practitionerJobsRecycler.setVisibility(View.VISIBLE);


                    } else {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadOrdersProgress.setVisibility(View.GONE);
                            practitionerJobsRecycler.setVisibility(View.GONE);
                            haveNoOrdersLayout.setVisibility(View.VISIBLE);
                        }, 1000);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    @SuppressLint("SimpleDateFormat")
    private int sortByDate(OrderModel o1, OrderModel o2) {
        int value = 0;
        try {
            value = new SimpleDateFormat("DD/MM/yyyy").parse(o1.getDate())
                    .compareTo(new SimpleDateFormat("DD/MM/yyyy").parse(o2.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return value;
    }


    private void showOrders() {
        new NetworkUtil(internet -> {
            if (internet) {
                addOrdersToRecyclerView();
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadOrdersProgress.setVisibility(View.GONE);
                    noInternetLayout.setVisibility(View.VISIBLE);
                }, 1000);
                tryCheckAgain();
            }
            practitionerJobsRefresh.setRefreshing(false);

        });
    }


    @Override
    public void onRefresh() {
        showOrders();
    }


    private void tryCheckAgain() {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.no_internet_try_layout,
                        requireActivity().findViewById(R.id.noInternetLayout));

        view.findViewById(R.id.tryAgainBtn).setOnClickListener(v -> {
            noInternetLayout.setVisibility(View.GONE);
            practitionerJobsRecycler.setVisibility(View.GONE);
            loadOrdersProgress.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(this::showOrders, 1000);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshJobs)
            showOrders();
    }
}