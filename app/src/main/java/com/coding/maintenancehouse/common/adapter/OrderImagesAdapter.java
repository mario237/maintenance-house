package com.coding.maintenancehouse.common.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coding.maintenancehouse.R;

import java.util.List;

public class OrderImagesAdapter extends RecyclerView.Adapter<OrderImagesAdapter.OrderImagesViewHolder> {

    final Context context;
    final List<String> urlsList;

    public OrderImagesAdapter(Context context, List<String> urlsList) {
        this.context = context;
        this.urlsList = urlsList;
    }


    @NonNull
    @Override
    public OrderImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_images_item_style , parent, false);
        return new OrderImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderImagesViewHolder holder, int position) {
        Glide.with(context).load(urlsList.get(position))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.orderImageLoad.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.orderImageLoad.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.serviceImageView);
    }

    @Override
    public int getItemCount() {
        return urlsList.size();
    }

    public static class OrderImagesViewHolder extends RecyclerView.ViewHolder {

        final ImageView serviceImageView;
        final ProgressBar orderImageLoad;

        public OrderImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImageView = itemView.findViewById(R.id.serviceImageView);
            orderImageLoad = itemView.findViewById(R.id.orderImageLoad);
        }
    }
}
