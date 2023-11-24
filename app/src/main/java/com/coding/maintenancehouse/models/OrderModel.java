package com.coding.maintenancehouse.models;

import java.util.List;

public class OrderModel {
    private String userId;
    private String category;
    private List<String> images;
    private String description;
    private Boolean urgent;
    private String date;
    private String from;
    private String to;
    private String state;
    private String practitionerId;
    private String orderId;

    public OrderModel() {
    }

    public OrderModel(String userId, String category, List<String> images, String description, Boolean urgent, String date, String from, String to, String state, String practitionerId, String orderId) {
        this.userId = userId;
        this.category = category;
        this.images = images;
        this.description = description;
        this.urgent = urgent;
        this.date = date;
        this.from = from;
        this.to = to;
        this.state = state;
        this.practitionerId = practitionerId;
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getImages() {
        return images;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public String getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getState() {
        return state;
    }

    public String getPractitionerId() {
        return practitionerId;
    }

    public String getOrderId() {
        return orderId;
    }


}
