package com.coding.maintenancehouse.authentication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.models.UserModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;

    Uri userImageUri;


    String userRole, user_id;

    TextView registrationText;
    CardView profileImageCard;
    ImageView profileImage;
    EditText nameEdt, emailEdt, phoneEdt, addressEdt, passwordEdt, categoryEdt;
    RadioButton radioMale;
    Button registerBtn;

    String currLanguage;
    String selectedCategory = "";

    AlertDialog loadingDialog, networkDialog, policyDialog;

    CheckBox policyCheck;

    boolean userExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRole = getIntent().getStringExtra("userRole");

        init();


        checkUserRole(userRole);

        currLanguage = Locale.getDefault().getLanguage();

        if (currLanguage.equals("ar")) {
            passwordEdt.setGravity(Gravity.END);
            phoneEdt.setGravity(Gravity.END);
        } else {
            passwordEdt.setGravity(Gravity.START);
            phoneEdt.setGravity(Gravity.START);
        }

        if(userImageUri == null){
            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (20*scale + 0.5f);
            profileImage.setImageResource(R.drawable.ic_person_add_gray);
            profileImage.setPadding(dpAsPixels , dpAsPixels , dpAsPixels , dpAsPixels);
        }

    }


    private void init() {
        registrationText = findViewById(R.id.registrationText);
        profileImageCard = findViewById(R.id.profileImageCard);
        profileImage = findViewById(R.id.profileImage);
        nameEdt = findViewById(R.id.nameEdt);
        emailEdt = findViewById(R.id.emailEdt);
        phoneEdt = findViewById(R.id.phoneEdt);
        addressEdt = findViewById(R.id.addressEdt);
        passwordEdt = findViewById(R.id.passwordEdt);
        radioMale = findViewById(R.id.radioMale);
        categoryEdt = findViewById(R.id.categoryEdt);

        policyCheck = findViewById(R.id.policyCheck);


        registerBtn = findViewById(R.id.registerBtn);


        profileImageCard.setOnClickListener(this);
        categoryEdt.setOnClickListener(this);
        registerBtn.setOnClickListener(this);




        keepListenOnPolicyCheck();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void keepListenOnPolicyCheck() {
        policyCheck.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //show alert
                showDialog();
                return true; //this will prevent checkbox from changing state
            }
            return false;
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.policy_dialog_style, null);


        builder.setView(dialogView);

        TextView policyMessage = dialogView.findViewById(R.id.policyMessage);

        Button acceptBtn = dialogView.findViewById(R.id.acceptBtn);

        Button declineBtn = dialogView.findViewById(R.id.declineBtn);


        acceptBtn.setOnClickListener(v -> {
            policyCheck.setChecked(true);
            registerBtn.setEnabled(true);
            registerBtn.setBackgroundColor(getColor(R.color.colorAccent));
            policyDialog.dismiss();
        });

        declineBtn.setOnClickListener(v -> {
            policyCheck.setChecked(false);
            registerBtn.setEnabled(false);
            registerBtn.setBackgroundColor(getColor(R.color.colorLightGray));
            policyDialog.dismiss();
        });

        policyDialog = builder.create();

        if(userRole.equals("client")){
            policyMessage.setText(getResources().getString(R.string.policy_message_client));
        }else {
            policyMessage.setText(getResources().getString(R.string.policy_message_practitioner));
        }

        policyDialog.show();
    }

    private void checkUserRole(String role) {
        if (role.equals("pending")) {
            registrationText.setText(getResources().getString(R.string.practitioner_registration));
            categoryEdt.setVisibility(View.VISIBLE);
        } else {
            registrationText.setText(getResources().getString(R.string.client_registration));
            categoryEdt.setText("");
            categoryEdt.setVisibility(View.GONE);
        }
    }

    private void validateRegisterData() {
        if (userRole.equals("pending")) {
            if (validateName() && validateEmail() && validatePhoneNumber()
                    && validateAddress() && validatePassword() && validateCategory()) {
                checkInternetConnection();
            } else {
                Toast.makeText(this, getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
            }
        } else if (userRole.equals("client")) {
            if (validateName() && validateEmail() && validatePhoneNumber()
                    && validateAddress() && validatePassword()) {
                checkInternetConnection();
            } else {
                Toast.makeText(this, getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createAccount() {

        String username = nameEdt.getText().toString();
        String email = emailEdt.getText().toString().trim();
        String phone = phoneEdt.getText().toString();
        String address = addressEdt.getText().toString();
        String gender = getGender();
        String password = passwordEdt.getText().toString();


        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);

                        assert userModel != null;
                        if (userModel.getEmailAddress().equals(email)){
                            userExist = true;
                        }
                    }
                }

                if (userExist){
                    loadingDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "this email is already registered", Toast.LENGTH_SHORT).show();
                    userExist = false;
                }
                else {

                    if (userImageUri != null) {
                        StorageReference userImageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + UUID.randomUUID().toString());

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

                        userImageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {


                            user_id = userRef.push().getKey();


                            UserModel userModel = new UserModel(
                                    user_id,
                                    userRole,
                                    username,
                                    uri.toString(),
                                    email,
                                    phone,
                                    address,
                                    gender,
                                    selectedCategory,
                                    password,
                                    false
                            );

                            assert user_id != null;

                            userRef.child(user_id).setValue(userModel).addOnSuccessListener(e -> new Handler(getMainLooper()).postDelayed(() -> goToMain(), 1000)).addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                            });


                        })).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    else {

                        user_id = userRef.push().getKey();


                        UserModel userModel = new UserModel(
                                user_id,
                                userRole,
                                username,
                                "",
                                email,
                                phone,
                                address,
                                gender,
                                selectedCategory,
                                password,
                                false
                        );

                        assert user_id != null;

                        userRef.child(user_id).setValue(userModel)
                                .addOnSuccessListener(e -> new Handler(getMainLooper()).postDelayed(() -> goToMain(), 1000))
                                .addOnFailureListener(e -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                                });

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });






    }

    private void goToMain() {
        loadingDialog.dismiss();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }


    private String getGender() {
        if (radioMale.isChecked())
            return "Male";
        else
            return "Female";
    }

    private Boolean validateName() {

        String nameTxt = nameEdt.getText().toString();

        if (nameTxt.isEmpty()) {
            nameEdt.setError("Field cannot be empty");
            return false;
        } else {
            nameEdt.setError(null);
            return true;
        }


    }


    private Boolean validateEmail() {
        String emailTxt = emailEdt.getText().toString();

        if (emailTxt.isEmpty()) {
            emailEdt.setError("Field cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            emailEdt.setError("Invalid email address");
            return false;
        } else {
            emailEdt.setError(null);
            return true;
        }

    }

    private Boolean validatePhoneNumber() {
        String phoneTxt = phoneEdt.getText().toString();


        if (phoneTxt.isEmpty()) {
            phoneEdt.setError("Field cannot be empty");

            return false;
        } else if (phoneTxt.length() < 11) {
            phoneEdt.setError("Phone number is invalid");

            return false;
        } else {
            phoneEdt.setError(null);
            return true;
        }

    }

    private Boolean validateAddress() {

        String addressTxt = addressEdt.getText().toString();

        if (addressTxt.isEmpty()) {
            addressEdt.setError("Field cannot be empty");
            return false;
        } else {
            addressEdt.setError(null);
            return true;
        }


    }


    private Boolean validatePassword() {
        String passwordTxt = passwordEdt.getText().toString();


        if (passwordTxt.isEmpty()) {
            passwordEdt.setError("Field cannot be empty");

            return false;
        } else if (passwordTxt.length() < 8) {
            passwordEdt.setError("Password is too short");

            return false;
        } else {
            passwordEdt.setError(null);
            return true;
        }

    }

    private Boolean validateCategory() {

        String categoryTxt = categoryEdt.getText().toString();

        if (categoryTxt.equals(getResources().getString(R.string.category))) {
            categoryEdt.setError("Field cannot be empty");
            return false;
        } else {
            categoryEdt.setError(null);
            return true;
        }


    }


    private void showCategoryDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(RegisterActivity.this);
        builderSingle.setTitle(getResources().getString(R.string.category));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RegisterActivity.this,
                android.R.layout.simple_list_item_1);
        arrayAdapter.add(getResources().getString(R.string.electricity));
        arrayAdapter.add(getResources().getString(R.string.plumbing));
        arrayAdapter.add(getResources().getString(R.string.carpentry));
        arrayAdapter.add(getResources().getString(R.string.painting));
        arrayAdapter.add(getResources().getString(R.string.conditioning));
        arrayAdapter.add(getResources().getString(R.string.heaters_cookers));
        arrayAdapter.add(getResources().getString(R.string.gypsum_board));
        arrayAdapter.add(getResources().getString(R.string.satellite_receivers));
        arrayAdapter.add(getResources().getString(R.string.aluminum));
        arrayAdapter.add(getResources().getString(R.string.ceramic_tiles));


        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            getCategoryName(which);
            categoryEdt.setText(arrayAdapter.getItem(which));
        });
        builderSingle.show();
    }

    private void getCategoryName(int position) {
        if (position == 0)
            selectedCategory = "Electricity";
        else if (position == 1)
            selectedCategory = "Plumbing";
        else if (position == 2)
            selectedCategory = "Carpentry";
        else if (position == 3)
            selectedCategory = "Painting";
        else if (position == 4)
            selectedCategory = "Conditioning";
        else if (position == 5)
            selectedCategory = "Heaters and cookers";
        else if (position == 6)
            selectedCategory = "Gypsum Board";
        else if (position == 7)
            selectedCategory = "Satellite and Receivers";
        else if (position == 8)
            selectedCategory = "Aluminum";
        else if (position == 9)
            selectedCategory = "Ceramic and Tiles";
    }


    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

    private void networkErrorDialogMethod() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);


        LayoutInflater.from(RegisterActivity.this)
                .inflate(R.layout.no_internet, findViewById(R.id.no_internet));


        builder.setView(R.layout.no_internet);

        networkDialog = builder.create();


        networkDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        networkDialog.show();


    }

    private void checkInternetConnection() {
        loadingDialogMethod();
        new NetworkUtil(internet -> {
            if (internet) {
                createAccount();
            } else {
                new Handler(getMainLooper()).postDelayed(() -> {
                    loadingDialog.dismiss();
                    networkErrorDialogMethod();
                }, 1000);
            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.categoryEdt) {
            showCategoryDialog();
        } else if (v.getId() == R.id.registerBtn) {
            validateRegisterData();
        } else if (v.getId() == R.id.profileImageCard) {
            checkGalleryPermission();
        }
    }

    private void checkGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            chooseImage();
        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //noinspection deprecation
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
                && data.getData() != null) {

            userImageUri = data.getData();


            profileImage.setPadding(0 , 0 , 0 , 0);


            Glide.with(this).load(userImageUri).into(profileImage);

        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }

}