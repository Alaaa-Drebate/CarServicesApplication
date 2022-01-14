package com.example.carservicesapp;

public class Constants {
    public static final String BASE_URL =  "car_services_api";
    public static final String SIGN_UP = "/signup.php";
    public static final String LOGIN = "/login.php";
    public static final String UPDATE_USER = "/update-user.php";
    public static final String GET_USER_BY_EMAIL = "/get_user_by_email.php";
    public static final String CHECK_OUT = "/create_request.php";
    public static final String GET_USER_REQUESTS = "/get_user_requests.php";
    public static final String GET_REQUEST_ORDERS = "/get_request_orders.php";
    public static final String GET_ALL_REQUESTS = "/get_all_requests.php";
    public static final String UPDATE_ORDER_STATUS = "/update_request.php";




    public static final int REQUEST_STATUS_NEW = 0;
    public static final int REQUEST_STATUS_PROCESSING = 1;
    public static final int REQUEST_STATUS_COMPLETE = 2;
    public static final int REQUEST_STATUS_REJECTED = 3;

    public static final double STARTING_PRICE = 30.00;
}