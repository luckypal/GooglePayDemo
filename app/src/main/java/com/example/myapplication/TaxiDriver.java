package com.example.myapplication;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TaxiDriver {

    @SerializedName("fbToken")
    @Expose
    private String fbToken;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("carName")
    @Expose
    private String carName;


    TaxiDriver(String fbToken, String name, String carName) {
        this.carName = carName;
        this.fbToken = fbToken;
        this.name = name;
    }

    public String getFbToken() {
        return fbToken;
    }

    public void setFbToken(String fbToken) {
        this.fbToken = fbToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }
}
