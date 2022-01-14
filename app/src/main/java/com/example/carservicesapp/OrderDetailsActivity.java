package com.example.carservicesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.carservicesapp.adapters.CartAdapter;
import com.example.carservicesapp.sqlite.CartItemsDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    TextView mUserName, mPhone;
    Button date;
    Button time;

    CartAdapter mAdapter;
    List<CartItemsDB> orders;
    RecyclerView mList;

    Intent sender;
    int requestId;
    String dateString, timeString, userName, phoneString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        sender = getIntent();
        if(sender != null){
            requestId = sender.getIntExtra("id", -1);
            dateString = sender.getStringExtra("date");
            timeString = sender.getStringExtra("time");
            phoneString = sender.getStringExtra("phone");
            userName = sender.getStringExtra("user");
        }
        mUserName = findViewById(R.id.user_name);
        mPhone = findViewById(R.id.phone);
        date = findViewById(R.id.date);
        time = findViewById(R.id.from);
        mList = findViewById(R.id.cart_list);

        date.setText(dateString);
        time.setText(timeString);
        date.setEnabled(false);
        time.setEnabled(false);

        mUserName.setText(phoneString);
        mPhone.setText(phoneString);

        getOrderDetails(requestId);

    }

    private void getOrderDetails(int requestId) {
        String URL = Constants.BASE_URL + Constants.GET_REQUEST_ORDERS;

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        orders = new ArrayList<>();

        AndroidNetworking.post(URL)
                .setPriority(Priority.MEDIUM)
                .addBodyParameter("request_id", String.valueOf(requestId))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.hide();
                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                //getting the problems from the response
                                JSONArray ordersArray = obj.getJSONArray("data");
                                CartItemsDB order;
                                for(int i = 0; i < ordersArray.length(); i++){
                                    JSONObject orderJson = ordersArray.getJSONObject(i);
                                    order = new CartItemsDB();
                                    order.setId(Integer.parseInt(orderJson.getString("order_id")));
                                    order.setCategory(orderJson.getString("order_name"));
                                    order.setPrice(Double.parseDouble(orderJson.getString("price")));
                                    order.setDetails(orderJson.getString("details"));
                                    orders.add(order);
                                }

                                mAdapter = new CartAdapter(OrderDetailsActivity.this, orders);
                                mList.setAdapter(mAdapter);

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(OrderDetailsActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        Log.e("orders:", anError.getMessage());
                        Toast.makeText(OrderDetailsActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}