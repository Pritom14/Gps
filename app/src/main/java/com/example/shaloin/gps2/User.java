package com.example.shaloin.gps2;

/**
 * Created by shaloin on 16/10/17.
 */

public class User {

    private String full_name,phone_number;

    public User() {
    }

    public User(String full_name, String phone_number) {
        this.full_name = full_name;
        this.phone_number = phone_number;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
