package com.coding.maintenancehouse.common.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.authentication.LoginActivity;
import com.coding.maintenancehouse.common.activities.VerifyOTPActivity;
import com.coding.maintenancehouse.common.activities.EditProfileActivity;
import com.coding.maintenancehouse.common.activities.SupportActivity;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.coding.maintenancehouse.splash.SplashActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

public class ProfileFragment extends Fragment implements View.OnClickListener , SwipeRefreshLayout.OnRefreshListener {

    SharedPref sharedPref;

    ConstraintLayout noInternetLayout;


    SwipeRefreshLayout refreshProfile;

    ProgressBar loadUserProfile;

    ImageButton editProfileIcon;
    ImageView profileImage;
    ProgressBar loadProfileImage;
    TextView usernameTv, userEmailTv, userPhoneTv, userAddressTv;
    RelativeLayout userProfileLayout, settingLayout, supportLayout;

    DatabaseReference userRef;
    String userId , userPhoneNumber;

    AlertDialog settingDialog;

    boolean refresh = false;

    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_client_profile, container, false);

        context = requireContext();


        sharedPref = new SharedPref(context);


        init(view);




        addViewsToOnClick();


        return view;
    }

    private void init(View view) {



        noInternetLayout = view.findViewById(R.id.noInternetLayout);

        refreshProfile = view.findViewById(R.id.refreshProfile);

        loadUserProfile = view.findViewById(R.id.loadUserProfile);
        userProfileLayout = view.findViewById(R.id.userProfileLayout);

        editProfileIcon = view.findViewById(R.id.editProfileIcon);

        profileImage = view.findViewById(R.id.profileImage);
        loadProfileImage = view.findViewById(R.id.loadProfileImage);

        usernameTv = view.findViewById(R.id.usernameTv);
        userEmailTv = view.findViewById(R.id.userEmailTv);
        userPhoneTv = view.findViewById(R.id.userPhoneTv);
        userAddressTv = view.findViewById(R.id.userAddressTv);

        settingLayout = view.findViewById(R.id.settingLayout);
        supportLayout = view.findViewById(R.id.supportLayout);

        userId = sharedPref.getString("user_id");

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);


        showProfile();


    }

    private void addViewsToOnClick() {



        editProfileIcon.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
        supportLayout.setOnClickListener(this);

        refreshProfile.setOnRefreshListener(this);
    }


    private void setUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                assert userModel != null;
                if (userModel.getProfileImage()!=null && !userModel.getProfileImage().isEmpty()) {
                    Glide.with(context).load(userModel.getProfileImage())
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
                } else {
                    loadProfileImage.setVisibility(View.GONE);
                    profileImage.setImageResource(R.drawable.ic_person_gray);
                }
                usernameTv.setText(userModel.getUsername());
                userEmailTv.setText(userModel.getEmailAddress());

                userPhoneNumber = userModel.getPhoneNumber();
                userPhoneTv.setText(userPhoneNumber);

                userAddressTv.setText(userModel.getAddress());

                loadUserProfile.setVisibility(View.GONE);
                userProfileLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editProfileIcon:
                goToEditProfile();
                break;
            case R.id.settingLayout:
                goToSettings();
                break;
            case R.id.supportLayout:
                goToSupport();
                break;
            case R.id.englishImage:
                changeLanguage("en");
                break;
            case R.id.arabicImage:
                changeLanguage("ar");
                break;
            case R.id.logoutBtn:
                logout();
                break;
            case R.id.changePassBtn:
                goToChangePassBtn();
                break;
        }
    }



    private void changeLanguage(String language) {
        LocaleHelper.setLocale(context, language);
        settingDialog.dismiss();
        restartApp();
    }

    private void goToEditProfile() {
        startActivity(new Intent(context, EditProfileActivity.class));
        refresh = true;
    }

    private void goToSettings() {
        showSettingDialog();
    }


    private void goToSupport() {
        Intent supportIntent = new Intent(context, SupportActivity.class);
        supportIntent.putExtra("userId" , userId);
        supportIntent.putExtra("username",usernameTv.getText().toString());
        supportIntent.putExtra("email",userEmailTv.getText().toString());
        supportIntent.putExtra("phone",userPhoneTv.getText().toString());
        startActivity(supportIntent);
    }

    private void goToChangePassBtn() {
        settingDialog.dismiss();
        Intent verifyOTPIntent = new Intent(context, VerifyOTPActivity.class);
        verifyOTPIntent.putExtra("userId",userId);
        verifyOTPIntent.putExtra("email",userEmailTv.getText().toString());
        verifyOTPIntent.putExtra("from" , "profile");
        startActivity(verifyOTPIntent);
    }

    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(
                R.layout.setting_style,
                requireActivity().findViewById(R.id.setting_style_layout));

        builder.setView(view);

        settingDialog = builder.create();


        ImageButton englishImage = view.findViewById(R.id.englishImage);
        ImageButton arabicImage = view.findViewById(R.id.arabicImage);
        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        Button changePassBtn = view.findViewById(R.id.changePassBtn);

        englishImage.setOnClickListener(this);
        arabicImage.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        changePassBtn.setOnClickListener(this);

        settingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);



        settingDialog.show();
    }

    private void logout(){
        settingDialog.dismiss();
        sharedPref.putBoolean("isLogin" , false);
        sharedPref.putString("role","");
        sharedPref.putString("user_id","");
        Intent intent = new Intent(context, LoginActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finishAffinity();
    }

    private void restartApp() {
        Intent intent = new Intent(context, SplashActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finishAffinity();
    }


    @Override
    public void onRefresh() {
        showProfile();
    }

    private void showProfile(){
        new NetworkUtil(internet -> {
            if (internet){
                setUserData();
            }
            else {
                new Handler(Looper.getMainLooper()).postDelayed(()->{
                    loadUserProfile.setVisibility(View.GONE);
                    noInternetLayout.setVisibility(View.VISIBLE);
                } ,500);
                tryCheckAgain();
            }
            refreshProfile.setRefreshing(false);

        });

    }

    private void tryCheckAgain(){
        View view = LayoutInflater.from(context)
                .inflate(R.layout.no_internet_try_layout,
                        requireActivity().findViewById(R.id.noInternetLayout));

        view.findViewById(R.id.tryAgainBtn).setOnClickListener(v -> {
            noInternetLayout.setVisibility(View.GONE);
            loadUserProfile.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(this::showProfile,1000);
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        if (refresh)
            showProfile();
    }
}