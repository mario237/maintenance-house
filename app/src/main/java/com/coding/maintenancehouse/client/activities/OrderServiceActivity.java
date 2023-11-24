package com.coding.maintenancehouse.client.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coding.maintenancehouse.R;
import com.coding.maintenancehouse.client.adapters.ImagesPreviewAdapter;
import com.coding.maintenancehouse.helpers.LocaleHelper;
import com.coding.maintenancehouse.helpers.SharedPref;
import com.coding.maintenancehouse.models.ImagesPreviewModel;
import com.coding.maintenancehouse.models.OrderModel;
import com.coding.maintenancehouse.network.NetworkUtil;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class OrderServiceActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPref sharedPref;

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;

    String serviceNamEN, serviceNamAR;

    ImageView backIcon;
    TextView serviceNameText;


    EditText orderDescEdt, orderDateEdt, fromTimeEdt, toTimeEdt;
    MaterialCheckBox urgentCheckBox;
    Button addImageBtn, sendOrderBtn;

    int hour, minutes;


    List<ImagesPreviewModel> imagesList;

    RecyclerView imagePreviewRecycler;
    ImagesPreviewAdapter imagesPreviewAdapter;

    ImagesPreviewModel imagesPreviewModel;

    List<String> imagesURLList;

    AlertDialog loadingDialog, networkDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_service);

        serviceNamAR = getIntent().getStringExtra("serviceNamAR");
        serviceNamEN = getIntent().getStringExtra("serviceNamEN");

        sharedPref = new SharedPref(this);

        init();


        addViewsToOnClick();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        backIcon = findViewById(R.id.backIcon);

        serviceNameText = findViewById(R.id.serviceNameText);

        imagesList = new ArrayList<>();
        imagesURLList = new ArrayList<>();

        addImageBtn = findViewById(R.id.addImageBtn);

        orderDescEdt = findViewById(R.id.orderDescEdt);

        urgentCheckBox = findViewById(R.id.urgentCheckBox);

        orderDateEdt = findViewById(R.id.orderDateEdt);

        fromTimeEdt = findViewById(R.id.fromTimeEdt);

        toTimeEdt = findViewById(R.id.toTimeEdt);

        sendOrderBtn = findViewById(R.id.sendOrderBtn);

        serviceNameText.setText(getResources().getString(R.string.service) + " " + serviceNamAR);

        imagePreviewRecycler = findViewById(R.id.imagePreviewRecycler);
        imagePreviewRecycler.setHasFixedSize(true);
        imagePreviewRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


    }

    private void addViewsToOnClick() {
        backIcon.setOnClickListener(this);
        addImageBtn.setOnClickListener(this);
        orderDateEdt.setOnClickListener(this);
        fromTimeEdt.setOnClickListener(this);
        toTimeEdt.setOnClickListener(this);
        sendOrderBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backIcon)
            onBackPressed();
        else if (v.getId() == R.id.addImageBtn)
            checkGalleryPermission();
        else if (v.getId() == R.id.orderDateEdt)
            openDatePicker();
        else if (v.getId() == R.id.fromTimeEdt)
            checkStartTimeState("from");
        else if (v.getId() == R.id.toTimeEdt)
            checkStartTimeState("to");
        else if (v.getId() == R.id.sendOrderBtn)
            validateOrderData();
    }

    private void validateOrderData() {
        if (validateOrderDescription() && validateOrderDate() && isSetOrderTime())
            checkInternetConnection();
        else
            Toast.makeText(this, getResources().getString(R.string.all_fields_required),
                    Toast.LENGTH_SHORT).show();
    }

    private void checkGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(OrderServiceActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OrderServiceActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            chooseImage();
        }

    }

    @SuppressWarnings("deprecation")
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

        if (requestCode == CODE_IMAGE_GALLERY && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                int totalImagesCount = data.getClipData().getItemCount();

                for (int i = 0; i < totalImagesCount; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    String imageName = UUID.randomUUID().toString();


                    setImagesToRecyclerView(imageUri, imageName);
                }


            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                String imageName = UUID.randomUUID().toString();



                setImagesToRecyclerView(imageUri, imageName);
            }


        }
    }

    private void setImagesToRecyclerView(Uri uri, String name) {
        imagesPreviewModel = new ImagesPreviewModel(name, uri);

        imagesList.add(imagesPreviewModel);

        imagesPreviewAdapter = new ImagesPreviewAdapter(OrderServiceActivity.this, imagesList, imagePreviewRecycler);
        imagePreviewRecycler.setVisibility(View.VISIBLE);
        imagePreviewRecycler.setAdapter(imagesPreviewAdapter);


    }





    private void uploadOrderImages() {
        StorageReference ordersImagesFolder = FirebaseStorage.getInstance().getReference().child("orders_Images");


        for (int i = 0; i < imagesList.size(); i++) {
            Uri uploadedImageUri = imagesList.get(i).getImageUri();


            StorageReference uploadedImageName = ordersImagesFolder.child(imagesList.get(i).getImageName());


            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uploadedImageUri);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert bmp != null;
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();


            uploadedImageName.putBytes(data).addOnSuccessListener(taskSnapshot ->
                    uploadedImageName.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageURL = String.valueOf(uri);
                        imagesURLList.add(imageURL);
                        uploadOrderToDatabase();
                    })

            ).addOnFailureListener(e -> {
                loadingDialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        uploadOrderToDatabase();


    }


    private void uploadOrderToDatabase() {

        if (imagesURLList.size() == imagesList.size()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Services");

            String orderId = databaseReference.push().getKey();


            OrderModel orderModel = new OrderModel(
                    sharedPref.getString("user_id"),
                    serviceNamEN,
                    imagesURLList,
                    orderDescEdt.getText().toString().trim(),
                    urgentCheckBox.isChecked(),
                    orderDateEdt.getText().toString(),
                    fromTimeEdt.getText().toString(),
                    toTimeEdt.getText().toString(),
                    "Pending",
                    "",
                    orderId
            );

            assert orderId != null;
            databaseReference.child(orderId).setValue(orderModel).addOnSuccessListener(aVoid -> {
                loadingDialog.dismiss();
                imagesURLList.clear();
                finish();
            }).addOnFailureListener(e -> {
                loadingDialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

    }


    @SuppressLint("SetTextI18n")
    private void checkStartTimeState(String which) {
        if (which.equals("from")) {
            openTimePickerDialog("from");
        } else if (which.equals("to")) {
            if (fromTimeEdt.getText().toString().equals(getResources().getString(R.string.from))) {
                Toast.makeText(this, getResources().getString(R.string.no_start_time_message), Toast.LENGTH_SHORT).show();
            } else {
                openTimePickerDialog("to");
            }
        }


    }

    private void openTimePickerDialog(String which) {
        TimePickerDialog.OnTimeSetListener mTimeSetListener =
                (view, hourOfDay, minute) -> {
                    hour = hourOfDay;
                    minutes = minute;
                    String timeSet;
                    if (hour > 12) {
                        hour -= 12;
                        timeSet = "PM";
                    } else if (hour == 0) {
                        hour += 12;
                        timeSet = "AM";
                    } else if (hour == 12) {
                        timeSet = "PM";
                    } else {
                        timeSet = "AM";
                    }

                    String min;
                    if (minutes < 10)
                        min = "0" + minutes;
                    else
                        min = String.valueOf(minutes);

                    // Append in a StringBuilder
                    String aTime = String.valueOf(hour) + ':' +
                            min + " " + timeSet;

                    if (which.equals("from"))
                        fromTimeEdt.setText(aTime);
                    else if (which.equals("to"))
                        toTimeEdt.setText(aTime);

                };

        TimePickerDialog timePickerDialog = new TimePickerDialog(OrderServiceActivity.this, mTimeSetListener, hour, minutes, false);
        timePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void openDatePicker() {
        Calendar mCurrentDate = Calendar.getInstance();
        int mYear = mCurrentDate.get(Calendar.YEAR);
        int mMonth = mCurrentDate.get(Calendar.MONTH);
        int mDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(OrderServiceActivity.this, (datepicker, selectedYear, selectedMonth, selectedDay) -> {
            selectedMonth += 1;
            orderDateEdt.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
        }, mYear, mMonth, mDay);
        mDatePicker.show();
    }


    private Boolean validateOrderDescription() {

        String orderDescTxt = orderDescEdt.getText().toString();

        if (orderDescTxt.isEmpty()) {
            orderDescEdt.setError(getResources().getString(R.string.field_empty_message));
            return false;
        } else {
            orderDescEdt.setError(null);
            return true;
        }


    }

    private Boolean validateOrderDate() {

        String orderDateTxt = orderDateEdt.getText().toString();

        if (orderDateTxt.isEmpty()) {
            orderDateEdt.setError(getResources().getString(R.string.field_empty_message));
            return false;
        } else {
            orderDateEdt.setError(null);
            return true;
        }


    }


    private Boolean validateStartTime() {

        String orderStartTimeTxt = fromTimeEdt.getText().toString();

        if (orderStartTimeTxt.equals(getResources().getString(R.string.from))) {
            fromTimeEdt.setError(getResources().getString(R.string.field_empty_message));
            return false;
        } else {
            fromTimeEdt.setError(null);
            return true;
        }


    }

    private Boolean validateFinishTime() {

        String orderFinishTimeTxt = toTimeEdt.getText().toString();

        if (orderFinishTimeTxt.equals(getResources().getString(R.string.to))) {
            toTimeEdt.setError(getResources().getString(R.string.field_empty_message));
            return false;
        } else {
            toTimeEdt.setError(null);
            return true;
        }
    }


    private Boolean sameTime() {
        boolean state = false;
        if (!fromTimeEdt.getText().toString().equals(getResources().getString(R.string.from))
                && !toTimeEdt.getText().toString().equals(getResources().getString(R.string.to))) {
            if (fromTimeEdt.getText().toString().equals(toTimeEdt.getText().toString())) {
                fromTimeEdt.setError("");
                toTimeEdt.setError("");
                state = true;
            } else {
                fromTimeEdt.setError(null);
                toTimeEdt.setError(null);
            }
        }

        return state;
    }


    private Boolean isSetOrderTime() {
        boolean state = false;

        if (validateStartTime() && validateFinishTime()) {

            if (!sameTime()) {
                state = true;
            }

        }

        return state;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(OrderServiceActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }

    private void checkInternetConnection() {
        loadingDialogMethod();
        new NetworkUtil(internet -> {
            if (internet) {
                new Handler(getMainLooper()).postDelayed(this::uploadOrderImages, 1000);
            } else {
                new Handler(getMainLooper()).postDelayed(() -> {
                    loadingDialog.dismiss();
                    networkErrorDialogMethod();
                }, 1000);
            }

        });
    }

    private void networkErrorDialogMethod() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(OrderServiceActivity.this);


        LayoutInflater.from(OrderServiceActivity.this)
                .inflate(R.layout.no_internet, findViewById(R.id.no_internet));


        builder.setView(R.layout.no_internet);

        networkDialog = builder.create();


        networkDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        networkDialog.show();


    }

}