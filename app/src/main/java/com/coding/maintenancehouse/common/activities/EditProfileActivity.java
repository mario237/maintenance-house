package com.coding.maintenancehouse.common.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPref sharedPref;

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;

    ImageButton backIcon;
    RelativeLayout profileImageLayout;
    ImageView  profileImage;
    ProgressBar loadProfileImage;
    EditText nameEdt , emailEdt , phoneEdt ,addressEdt;
    Button submitBtn;

    Uri userImageUri;

    DatabaseReference userRef;
    String userId , profileImageURL;

    AlertDialog loadingDialog,networkDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPref = new SharedPref(this);

        init();

        addViewsToOnClick();
    }


    private void init() {
        backIcon = findViewById(R.id.backIcon);
        profileImageLayout = findViewById(R.id.profileImageLayout);
        profileImage = findViewById(R.id.profileImage);
        loadProfileImage = findViewById(R.id.loadProfileImage);
        nameEdt = findViewById(R.id.nameEdt);
        emailEdt = findViewById(R.id.emailEdt);
        phoneEdt = findViewById(R.id.phoneEdt);
        addressEdt = findViewById(R.id.addressEdt);
        submitBtn = findViewById(R.id.submitBtn);


        userId = sharedPref.getString("user_id");

        if (userId != null){
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            getUserData();
        }
    }




    private void addViewsToOnClick() {
        backIcon.setOnClickListener(this);
        profileImageLayout.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backIcon : onBackPressed(); break;
            case R.id.profileImageLayout : checkGalleryPermission(); break;
            case R.id.submitBtn : checkInternetConnection(); break;
        }
    }



    private void checkGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }else {
            chooseImage();
        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, CODE_IMAGE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_IMAGE_GALLERY && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            userImageUri = data.getData();

            Glide.with(this).load(userImageUri).into(profileImage);

        }
    }

    private void getUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                assert userModel != null;
                if (userModel.getProfileImage()!=null && !userModel.getProfileImage().isEmpty()){
                    Glide.with(EditProfileActivity.this).load(userModel.getProfileImage())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    loadProfileImage.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    loadProfileImage.setVisibility(View.GONE);
                                    return false;
                                }


                            })
                            .into(profileImage);
                }else {
                    loadProfileImage.setVisibility(View.GONE);
                    profileImage.setImageResource(R.drawable.ic_person_gray);
                }
                nameEdt.setText(userModel.getUsername());
                emailEdt.setText(userModel.getEmailAddress());
                phoneEdt.setText(userModel.getPhoneNumber());
                addressEdt.setText(userModel.getAddress());
                profileImageURL = userModel.getProfileImage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUserData() {

        String nameTxt = nameEdt.getText().toString();
        String emailTxt = emailEdt.getText().toString();
        String phoneTxt = phoneEdt.getText().toString();
        String addressTxt = addressEdt.getText().toString();



        if (userImageUri != null){

            deleteOldImage();

            StorageReference userImageRef = FirebaseStorage.getInstance().getReference("ProfileImages/"+ UUID.randomUUID().toString());

            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), userImageUri);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert bmp != null;
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();

            userImageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> userImageRef.getDownloadUrl().addOnSuccessListener(uri -> updateDatabase(
                    uri.toString(),
                    nameTxt,
                    emailTxt,
                    phoneTxt,
                    addressTxt
            ))).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());


        }else {
            updateDatabase(
                    profileImageURL,
                    nameTxt,
                    emailTxt,
                    phoneTxt,
                    addressTxt
            );
        }





    }

    private void deleteOldImage(){

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                assert userModel != null;
                if (userModel.getProfileImage()!=null && !userModel.getProfileImage().isEmpty()){
                    profileImageURL = userModel.getProfileImage();

                    final StorageReference photoRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(profileImageURL);
                    photoRef.delete();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void updateDatabase(String image  , String name ,String email , String phone , String address){
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImage" , image);
        updates.put("username" , name);
        updates.put("emailAddress" , email);
        updates.put("phoneNumber" , phone);
        updates.put("address" , address);
        userRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            loadingDialog.dismiss();
            onBackPressed();
        }).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(EditProfileActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

    private void checkInternetConnection() {
        loadingDialogMethod();
        new NetworkUtil(internet -> {
            if (internet) {
                updateUserData();
            } else {
                new Handler(getMainLooper()).postDelayed(()->{
                    loadingDialog.dismiss();
                    networkErrorDialogMethod();
                },1000);
            }

        });
    }

    private void networkErrorDialogMethod() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);


        LayoutInflater.from(EditProfileActivity.this)
                .inflate(R.layout.no_internet, findViewById(R.id.no_internet));


        builder.setView(R.layout.no_internet);

        networkDialog = builder.create();



        networkDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        networkDialog.show();


    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }
}