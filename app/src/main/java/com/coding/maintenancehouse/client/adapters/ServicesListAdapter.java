package com.coding.maintenancehouse.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.client.models.ServicesModel;
import com.coding.maintenancehouse.listeners.OnServiceClickListener;

import java.util.List;

public class ServicesListAdapter extends RecyclerView.Adapter<ServicesListAdapter.ServiceViewHolder> {

    final List<ServicesModel> servicesModelList;

    private OnServiceClickListener mListener;

    public void setOnItemClickListener(OnServiceClickListener listener) {
        mListener = listener;
    }


    public ServicesListAdapter(List<ServicesModel> servicesModelList) {
        this.servicesModelList = servicesModelList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);

        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        holder.serviceImageView.setImageResource(servicesModelList.get(position).getServiceImage());
        holder.serviceTextView.setText(servicesModelList.get(position).getServiceName());
    }

    @Override
    public int getItemCount() {
        return servicesModelList.size();
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView serviceImageView;
        final TextView serviceTextView;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImageView = itemView.findViewById(R.id.serviceImageView);
            serviceTextView = itemView.findViewById(R.id.serviceTextView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onServiceClick(position);
                }
            }
        }
    }
}
