package com.coding.maintenancehouse.admin.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.admin.adapters.NewPractitionerAdapter;
import com.coding.maintenancehouse.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class NewPractitionerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ConstraintLayout haveNoPractitionersLayout;
    ProgressBar loadAllPractitioner;
    RecyclerView newPractitionerRecycler;
    List<UserModel> userModelList;
    NewPractitionerAdapter newPractitionerAdapter;

    SwipeRefreshLayout refreshPractitioners;

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_practitioner, container, false);

        context = requireContext();

        init(view);

        return view;
    }

    private void init(View view) {
        haveNoPractitionersLayout = view.findViewById(R.id.haveNoPractitionersLayout);
        loadAllPractitioner = view.findViewById(R.id.loadAllPractitioner);
        newPractitionerRecycler = view.findViewById(R.id.newPractitionerRecycler);
        newPractitionerRecycler.setHasFixedSize(true);
        newPractitionerRecycler.setLayoutManager(new LinearLayoutManager(context));

        refreshPractitioners = view.findViewById(R.id.refreshPractitioners);

        refreshPractitioners.setOnRefreshListener(this);


        getAllNewPractitionersFromDB();



    }

    private void getAllNewPractitionersFromDB() {

        userModelList = new ArrayList<>();

        DatabaseReference practitionerRef = FirebaseDatabase.getInstance().getReference("Users");

        practitionerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                userModelList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    assert userModel != null;
                    if(userModel.getRole().equals("pending")){
                        userModelList.add(userModel);
                    }
                }

                newPractitionerAdapter = new NewPractitionerAdapter(context , userModelList);
                newPractitionerRecycler.setAdapter(newPractitionerAdapter);

                loadAllPractitioner.setVisibility(View.GONE);

                if(userModelList.isEmpty()){
                    haveNoPractitionersLayout.setVisibility(View.VISIBLE);
                }else {
                    haveNoPractitionersLayout.setVisibility(View.GONE);
                    newPractitionerRecycler.setVisibility(View.VISIBLE);

                }

                refreshPractitioners.setRefreshing(false);


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRefresh() {
        getAllNewPractitionersFromDB();
    }
}