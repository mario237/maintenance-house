package com.coding.maintenancehouse.admin.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.models.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NewPractitionerAdapter extends RecyclerView.Adapter<NewPractitionerAdapter.ViewHolder> {

    final Context context;
    final List<UserModel> userModelList;

    AlertDialog loadingDialog;

    public NewPractitionerAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.practitioner_item , parent , false);

       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NewPractitionerAdapter.ViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);


        if (userModel.getProfileImage() != null && !userModel.getProfileImage().equals("")){
            Glide.with(context).load(userModel.getProfileImage())
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.loadProfileImage.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.loadProfileImage.setVisibility(View.GONE);
                            return false;
                        }


                    })
                    .into(holder.profileImage);


        }else {
            holder.loadProfileImage.setVisibility(View.GONE);
            holder.profileImage.setImageResource(R.drawable.ic_person_gray);
            holder.profileImage.setPadding(8 , 8 , 8 , 8);

        }


        holder.practitionerName.setText(userModel.getUsername());
        holder.practitionerPhoneNumber.setText(userModel.getPhoneNumber());


        holder.acceptBtn.setOnClickListener(v -> acceptPractitioner(position , userModel.getUserId()));
        holder.declineBtn.setOnClickListener(v -> deletePractitioner(position , userModel.getUserId()));

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ProgressBar loadProfileImage;

        final ImageView profileImage;

        final TextView practitionerName;
        final TextView practitionerPhoneNumber;

        final ImageButton declineBtn;
        final ImageButton acceptBtn;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            loadProfileImage = itemView.findViewById(R.id.loadProfileImage);
            profileImage = itemView.findViewById(R.id.profileImage);

            practitionerName = itemView.findViewById(R.id.practitionerName);
            practitionerPhoneNumber = itemView.findViewById(R.id.practitionerPhoneNumber);

            declineBtn = itemView.findViewById(R.id.declineBtn);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);


        }
    }

    private void acceptPractitioner(int position, String id) {
        loadingDialogMethod();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(id);
        userRef.child("role").setValue("practitioner");
        userRef.child("verified").setValue(true).addOnSuccessListener(unused -> {
            userModelList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, userModelList.size());

            loadingDialog.dismiss();

        });


    }

    private void deletePractitioner(int position,String id) {

        loadingDialogMethod();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(id);
        userRef.removeValue().addOnSuccessListener(unused -> {
            userModelList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, userModelList.size());

            loadingDialog.dismiss();
        });
    }

    private void loadingDialogMethod() {
        final androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

}
