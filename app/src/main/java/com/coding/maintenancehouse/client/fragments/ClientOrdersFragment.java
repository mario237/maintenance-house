package com.coding.maintenancehouse.client.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.coding.maintenancehouse.client.activities.ClientReviewActivity;
import com.coding.maintenancehouse.client.adapters.DisplayOrderAdapter;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.OrderModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ClientOrdersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SharedPref sharedPref;

    ConstraintLayout noInternetLayout;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar loadOrdersProgress;
    RecyclerView displayOrdersRecycler;
    ConstraintLayout haveNoOrdersLayout;
    DatabaseReference orderRef;
    List<OrderModel> orderModelList;
    String currentUserId;
    DisplayOrderAdapter displayOrderAdapter;

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_client_orders, container, false);

        context = requireContext();

        sharedPref = new SharedPref(context);

        init(view);

        showOrders();

        return view;
    }

    private void init(View v) {
        swipeRefreshLayout = v.findViewById(R.id.refreshOrders);
        loadOrdersProgress = v.findViewById(R.id.loadOrdersProgress);
        haveNoOrdersLayout = v.findViewById(R.id.haveNoOrdersLayout);

        noInternetLayout = v.findViewById(R.id.noInternetLayout);

        swipeRefreshLayout.setOnRefreshListener(this);

        displayOrdersRecycler = v.findViewById(R.id.displayOrdersRecycler);
        displayOrdersRecycler.setHasFixedSize(true);
        displayOrdersRecycler.setLayoutManager(new LinearLayoutManager(context));
        orderModelList = new ArrayList<>();

        currentUserId = sharedPref.getString("user_id");


    }

    private void addOrdersToRecyclerView() {
        orderModelList.clear();

        orderRef = FirebaseDatabase.getInstance().getReference("Services");


        if (currentUserId != null) {

            orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);
                        assert orderModel != null;
                        if (orderModel.getUserId().equals(currentUserId))
                            orderModelList.add(orderModel);
                    }

                    if (!orderModelList.isEmpty()) {
                        displayOrderAdapter = new DisplayOrderAdapter(context, orderModelList);
                        displayOrdersRecycler.setAdapter(displayOrderAdapter);
                        displayOrderAdapter.setOnItemClickListener(position -> makeReviewAfterFinishOrder(position));

                        loadOrdersProgress.setVisibility(View.GONE);
                        haveNoOrdersLayout.setVisibility(View.GONE);
                        displayOrdersRecycler.setVisibility(View.VISIBLE);


                    } else {
                        loadOrdersProgress.setVisibility(View.GONE);
                        displayOrdersRecycler.setVisibility(View.GONE);
                        haveNoOrdersLayout.setVisibility(View.VISIBLE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void makeReviewAfterFinishOrder(int position) {
        if (orderModelList.get(position).getState().equals("Done")){
            Intent reviewIntent = new Intent(context , ClientReviewActivity.class);
            reviewIntent.putExtra("userId" , currentUserId);
            reviewIntent.putExtra("orderId" , orderModelList.get(position).getOrderId());
            startActivity(reviewIntent);
        }
    }

    @Override
    public void onRefresh() {
        showOrders();
    }

   private void showOrders(){
        new NetworkUtil(internet -> {
            if (internet){
                addOrdersToRecyclerView();
            }
            else {
                new Handler(Looper.getMainLooper()).postDelayed(()->{
                    loadOrdersProgress.setVisibility(View.GONE);
                    noInternetLayout.setVisibility(View.VISIBLE);
                } ,1000);
                tryCheckAgain();
            }
            swipeRefreshLayout.setRefreshing(false);

        });

   }

   private void tryCheckAgain(){
        View view = LayoutInflater.from(context)
                .inflate(R.layout.no_internet_try_layout,
                        requireActivity().findViewById(R.id.noInternetLayout));

       view.findViewById(R.id.tryAgainBtn).setOnClickListener(v -> {
           noInternetLayout.setVisibility(View.GONE);
           displayOrdersRecycler.setVisibility(View.GONE);
           loadOrdersProgress.setVisibility(View.VISIBLE);
           new Handler(Looper.getMainLooper()).postDelayed(this::showOrders,1000);
        });
   }
}