package com.example.carservicesapp.model;

public class Order {

    int id;
    double totalPrice;
    int status;
    String userName;
    String date;
    String time;
    String phone;


    public Order(int id, double totalPrice, int status) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Order(int id, double totalPrice, int status, String userName, String date, String time, String phone) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.status = status;
        this.userName = userName;
        this.date = date;
        this.time = time;
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
