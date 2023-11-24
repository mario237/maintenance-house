package com.coding.maintenancehouse.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.listeners.OnOrderClickListener;
import com.coding.maintenancehouse.models.OrderModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectFromOrdersAdapter extends RecyclerView.Adapter<SelectFromOrdersAdapter.ViewHolder> {

    final Context context;
    final List<OrderModel> orderModelList;

    private OnOrderClickListener mListener;

    public void setOnItemClickListener(OnOrderClickListener listener) {
        mListener = listener;
    }


    public SelectFromOrdersAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_order_item , parent , false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull SelectFromOrdersAdapter.ViewHolder holder, int position) {

        OrderModel orderModel = orderModelList.get(position);

        holder.serviceNameTextView.setText(orderModel.getCategory());
        holder.serviceDateTextView.setText(orderModel.getDate());
        holder.serviceTimeTextView.setText(
                context.getResources().getString(R.string.from) + " " + orderModel.getFrom() + " " + context.getResources().getString(R.string.to) + " " + orderModel.getTo()
        );

        holder.serviceStateTextView.setText(orderModel.getState());
    }

    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView serviceNameTextView;
        final TextView serviceDateTextView;
        final TextView serviceTimeTextView;
        final TextView serviceStateTextView;

        public ViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);

            serviceNameTextView = itemView.findViewById(R.id.serviceNameTextView);
            serviceDateTextView = itemView.findViewById(R.id.serviceDateTextView);
            serviceTimeTextView = itemView.findViewById(R.id.serviceTimeTextView);
            serviceStateTextView = itemView.findViewById(R.id.serviceStateTextView);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onOrderClick(position);
                }
            }
        }
    }
}
