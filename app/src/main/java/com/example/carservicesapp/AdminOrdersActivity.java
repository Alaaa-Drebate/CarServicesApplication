package com.example.carservicesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.carservicesapp.adapters.AdminOrdersAdapter;
import com.example.carservicesapp.model.Order;
import com.example.carservicesapp.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminOrdersActivity extends AppCompatActivity {

    LinearLayout mNoItemsPlaceHolder;
    RecyclerView mOrdersList;
    ArrayList<Order> orders;
    AdminOrdersAdapter mOrdersAdapter;

    ImageView mRefreshBtn;
    ImageView mLogoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);


        bindViews();

        hidePlaceHolder();

        getAllOrders();

        mRefreshBtn.setOnClickListener(v -> {
            getAllOrders();
        });

        mLogoutBtn.setOnClickListener(v -> logOut());
    }

    public void getAllOrders(){
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();
        mRefreshBtn.setEnabled(false);

        String url = Constants.BASE_URL + Constants.GET_ALL_REQUESTS;

        orders = new ArrayList<>();

        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        mRefreshBtn.setEnabled(true);

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                //getting the problems from the response
                                JSONArray reportsArray = obj.getJSONArray("data");
                                Order order;
                                for(int i = 0; i < reportsArray.length(); i++){
                                    JSONObject orderJson = reportsArray.getJSONObject(i);
                                    order = new Order(
                                            Integer.parseInt(orderJson.getString("id")),
                                            Double.parseDouble(orderJson.getString("total_price")),
                                            Integer.parseInt(orderJson.getString("status")),
                                            orderJson.getString("user_name"),
                                            orderJson.getString("date"),
                                            orderJson.getString("time"),
                                            orderJson.getString("user_phone")
                                    );
                                    orders.add(order);
                                }

                                if(orders.isEmpty()){
                                    showPlaceHolder();
                                }else {
                                    mOrdersAdapter = new AdminOrdersAdapter(AdminOrdersActivity.this, orders);
                                    mOrdersList.setAdapter(mOrdersAdapter);
                                    hidePlaceHolder();
                                }


                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(AdminOrdersActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mRefreshBtn.setEnabled(true);
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mRefreshBtn.setEnabled(true);
                        Log.e("orders:", anError.getMessage());
                        Toast.makeText(AdminOrdersActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindViews() {
        mNoItemsPlaceHolder = findViewById(R.id.place_holder);
        mOrdersList = findViewById(R.id.orders_list);
        mRefreshBtn = findViewById(R.id.refresh_btn);
        mLogoutBtn = findViewById(R.id.logout_btn);
    }

    void showPlaceHolder(){
        mNoItemsPlaceHolder.setVisibility(View.VISIBLE);
    }

    void hidePlaceHolder(){
        mNoItemsPlaceHolder.setVisibility(View.INVISIBLE);
    }


    public void logOut(){
        SharedPrefManager.getInstance(this).logout();
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllOrders();
    }
}