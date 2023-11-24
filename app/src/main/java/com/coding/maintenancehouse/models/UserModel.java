package com.coding.maintenancehouse.models;

@SuppressWarnings("ALL")
public class UserModel {

    private String userId;
    private String role;
    private String username;
    private String profileImage;
    private String emailAddress;
    private String phoneNumber;
    private String address;
    private String gender;
    private String category;
    private String password;
    private boolean verified;

    public UserModel() {
    }


    public UserModel(String userId, String role, String username, String profileImage, String emailAddress, String phoneNumber, String address, String gender, String category, String password, boolean verified) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.profileImage = profileImage;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.gender = gender;
        this.category = category;
        this.password = password;
        this.verified = verified;
    }




    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getCategory() {
        return category;
    }

    public String getPassword() {
        return password;
    }

    public boolean isVerified() {
        return verified;
    }
}
