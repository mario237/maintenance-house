package com.coding.maintenancehouse.client.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.listeners.OnOrderClickListener;
import com.coding.maintenancehouse.models.OrderModel;
import com.google.firebase.database.annotations.Nullable;

import java.util.List;

public class DisplayOrderAdapter extends RecyclerView.Adapter<DisplayOrderAdapter.DisplayViewHolder> {

    final Context context;
    final List<OrderModel> orderModelList;

    private OnOrderClickListener mListener;

    public void setOnItemClickListener(OnOrderClickListener listener) {
        mListener = listener;
    }


    public DisplayOrderAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
    }

    @NonNull
    @Override
    public DisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.display_order_item , parent , false);

        return new DisplayViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DisplayViewHolder holder, int position) {
        OrderModel orderModel = orderModelList.get(position);

        if (orderModel.getImages() != null){
            Glide.with(context).load(orderModel.getImages().get(0))
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.orderLoad.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.orderLoad.setVisibility(View.GONE);
                            return false;
                        }


                    })
                    .into(holder.serviceImageView);


        }else {
            holder.orderLoad.setVisibility(View.GONE);
            holder.setServiceIcon(orderModel.getCategory());
        }

        holder.serviceNameTextView.setText(holder.getServiceName(orderModel.getCategory()));

        holder.serviceDateTextView.setText(orderModel.getDate());

        holder.serviceTimeTextView.setText(
                context.getResources().getString(R.string.from) + " " + orderModel.getFrom() + " " + context.getResources().getString(R.string.to) + " " + orderModel.getTo()
        );

        holder.serviceStateTextView.setText(orderModel.getState());

        switch (orderModel.getState()) {
            case "Accepted":
                holder.serviceStateTextView.setTextColor(context.getColor(R.color.colorGreen));
                break;
            case "Done":
                holder.goToReviewTextView.setVisibility(View.VISIBLE);
                break;
            default:
                holder.serviceStateTextView.setTextColor(context.getColor(R.color.colorLightGray));
                break;
        }



    }

    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class DisplayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ProgressBar orderLoad;
        final ImageView serviceImageView;
        final TextView serviceNameTextView;
        final TextView serviceDateTextView;
        final TextView serviceTimeTextView;
        final TextView serviceStateTextView;
        final TextView goToReviewTextView;

        public DisplayViewHolder(@NonNull View itemView) {
            super(itemView);
            orderLoad = itemView.findViewById(R.id.orderLoad);
            serviceImageView = itemView.findViewById(R.id.serviceImageView);
            serviceNameTextView = itemView.findViewById(R.id.serviceNameTextView);
            serviceDateTextView = itemView.findViewById(R.id.serviceDateTextView);
            serviceTimeTextView = itemView.findViewById(R.id.serviceTimeTextView);
            serviceStateTextView = itemView.findViewById(R.id.serviceStateTextView);
            goToReviewTextView = itemView.findViewById(R.id.goToReviewTextView);

            itemView.setOnClickListener(this);
        }

        private String getServiceName(String category){
            String value = null;

            switch (category) {
                case "Electricity":
                    value = context.getResources().getString(R.string.electricity);
                    break;
                case "Plumbing":
                    value = context.getResources().getString(R.string.plumbing);
                    break;
                case "Carpentry":
                    value = context.getResources().getString(R.string.carpentry);
                    break;
                case "Painting":
                    value = context.getResources().getString(R.string.painting);
                    break;
                case "Conditioning":
                    value = context.getResources().getString(R.string.conditioning);
                    break;
                case "Heaters and cookers":
                    value = context.getResources().getString(R.string.heaters_cookers);
                    break;
                case "Gypsum Board":
                    value = context.getResources().getString(R.string.gypsum_board);
                    break;
                case "Satellite and Receivers":
                    value = context.getResources().getString(R.string.satellite_receivers);
                    break;
                case "Aluminum":
                    value = context.getResources().getString(R.string.aluminum);
                    break;
                case "Ceramic and Tiles":
                    value = context.getResources().getString(R.string.ceramic_tiles);
                    break;
            }

            return value;
        }

        private void setServiceIcon(String category){

            switch (category) {
                case "Electricity":
                    serviceImageView.setImageResource(R.drawable.ic_electric);
                    break;
                case "Plumbing":
                    serviceImageView.setImageResource(R.drawable.ic_plumb);
                    break;
                case "Carpentry":
                    serviceImageView.setImageResource(R.drawable.ic_carpentry);
                    break;
                case "Painting":
                    serviceImageView.setImageResource(R.drawable.ic_painting);
                    break;
                case "Conditioning":
                    serviceImageView.setImageResource(R.drawable.ic_air_conditioner);
                    break;
                case "Heaters and cookers":
                    serviceImageView.setImageResource(R.drawable.ic_cooker);
                    break;
                case "Gypsum Board":
                    serviceImageView.setImageResource(R.drawable.ic_layers);
                    break;
                case "Satellite and Receivers":
                    serviceImageView.setImageResource(R.drawable.ic_satellite);
                    break;
                case "Aluminum":
                    serviceImageView.setImageResource(R.drawable.ic_kitchen);
                    break;
                case "Ceramic and Tiles":
                    serviceImageView.setImageResource(R.drawable.ic_tiles);
                    break;
            }

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
