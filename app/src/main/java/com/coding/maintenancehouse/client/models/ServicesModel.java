package com.coding.maintenancehouse.client.models;

public class ServicesModel {
    private final String serviceName;
    private final int serviceImage;
    private final String serviceNameEN;

    public ServicesModel(String serviceName, int serviceImage, String serviceNameEN) {
        this.serviceName = serviceName;
        this.serviceImage = serviceImage;
        this.serviceNameEN = serviceNameEN;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getServiceImage() {
        return serviceImage;
    }

    public String getServiceNameEN() {
        return serviceNameEN;
    }
}
