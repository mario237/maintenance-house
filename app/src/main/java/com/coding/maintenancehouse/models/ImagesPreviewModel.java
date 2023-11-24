package com.coding.maintenancehouse.models;

import android.net.Uri;

public class ImagesPreviewModel {
    final String imageName;
    final Uri imageUri;


    public ImagesPreviewModel(String imageName, Uri imageUri) {
        this.imageName = imageName;
        this.imageUri = imageUri;
    }


    public Uri getImageUri() {
        return imageUri;
    }


    public String getImageName() {
        return imageName;
    }

}
