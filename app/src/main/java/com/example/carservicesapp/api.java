package com.example.carservicesapp;

import com.example.carservicesapp.model.User;

public interface api {

    /*
    * user table :
    * id
    * name (required)
    * email (required)
    * password (required)
    * address (optional)
    * phone (optional)
    * zip_code (optional)
    * */

    //returns user data if this user has an account associated with this email and null otherwise
    public User userExisted(String email);

    public User login(String email, String password);

    public User register(String email, String password, String name);
    //please pay attention!...some parameters are optional :)
    public User register(String email, String password, String name, String address, String phone, String zip_code);

    //id is required but anything else is optional
    public User updateProfile(int id, String name, String email, String password, String address, String phone, String zip_code);


}
