package com.coding.maintenancehouse.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.client.activities.OrderServiceActivity;
import com.coding.maintenancehouse.client.adapters.ServicesListAdapter;
import com.coding.maintenancehouse.client.models.ServicesModel;
import com.coding.maintenancehouse.listeners.OnServiceClickListener;

import java.util.ArrayList;
import java.util.List;

public class ClientHomeFragment extends Fragment implements OnServiceClickListener {
    RecyclerView servicesRecyclerView;
    List<ServicesModel> servicesModelList;
    ServicesListAdapter servicesListAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_client_home, container, false);


        initViews(view);

        return view;
    }

    private void initViews(View view) {
        servicesRecyclerView = view.findViewById(R.id.servicesRecyclerView);
        servicesRecyclerView.setHasFixedSize(true);
        servicesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext() , 2));

        servicesModelList = new ArrayList<>();

        servicesModelList.add(new ServicesModel(getResources().getString(R.string.electricity) , R.drawable.ic_electric  , "Electricity"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.plumbing) , R.drawable.ic_plumb , "Plumbing"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.carpentry) , R.drawable.ic_carpentry , "Carpentry"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.painting) , R.drawable.ic_painting , "Painting"));

        servicesModelList.add(new ServicesModel(getResources().getString(R.string.conditioning) , R.drawable.ic_air_conditioner , "Conditioning"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.heaters_cookers) , R.drawable.ic_cooker , "Heaters and cookers"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.gypsum_board) , R.drawable.ic_layers , "Gypsum Board"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.satellite_receivers) , R.drawable.ic_satellite , "Satellite and Receivers"));

        servicesModelList.add(new ServicesModel(getResources().getString(R.string.aluminum) , R.drawable.ic_kitchen , "Aluminum"));
        servicesModelList.add(new ServicesModel(getResources().getString(R.string.ceramic_tiles) , R.drawable.ic_tiles , "Ceramic and Tiles"));


        servicesListAdapter = new ServicesListAdapter(servicesModelList);

        servicesRecyclerView.setAdapter(servicesListAdapter);

        servicesListAdapter.setOnItemClickListener(this);
    }


    @Override
    public void onServiceClick(int position) {
        Intent orderIntent = new Intent(requireContext() , OrderServiceActivity.class);
        orderIntent.putExtra("serviceNamAR",servicesModelList.get(position).getServiceName());
        orderIntent.putExtra("serviceNamEN",servicesModelList.get(position).getServiceNameEN());
        startActivity(orderIntent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
    }
}