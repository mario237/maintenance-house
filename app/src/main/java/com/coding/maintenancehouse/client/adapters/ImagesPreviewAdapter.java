package com.coding.maintenancehouse.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.models.ImagesPreviewModel;

import java.util.List;

public class ImagesPreviewAdapter extends RecyclerView.Adapter<ImagesPreviewAdapter.ImagesViewHolder> {

    final Context context;
    final List<ImagesPreviewModel> previewModelList;
    final RecyclerView recyclerView;

    public ImagesPreviewAdapter(Context context, List<ImagesPreviewModel> previewModelList, RecyclerView recyclerView) {
        this.context = context;
        this.previewModelList = previewModelList;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item_style, parent, false);

        return new ImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {

        Glide.with(context).load(previewModelList.get(position).getImageUri()).into(holder.serviceImageView);


        holder.deleteServiceIcon.setOnClickListener(v -> {
            if (previewModelList.size() == 1) {
                recyclerView.setVisibility(View.GONE);
            }
            if (previewModelList.size() > position) {
                previewModelList.remove(position);
                notifyItemRemoved(position);
            } else {
                previewModelList.remove(0);
                notifyItemRemoved(0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return previewModelList.size();
    }

    public static class ImagesViewHolder extends RecyclerView.ViewHolder {

        final ImageView serviceImageView;
        final ImageButton deleteServiceIcon;

        public ImagesViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceImageView = itemView.findViewById(R.id.serviceImageView);
            deleteServiceIcon = itemView.findViewById(R.id.deleteServiceIcon);
        }
    }
}
