package com.example.carservicesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.carservicesapp.adapters.CartAdapter;
import com.example.carservicesapp.model.Order;
import com.example.carservicesapp.model.User;
import com.example.carservicesapp.sqlite.CartItemsDB;
import com.example.carservicesapp.sqlite.Myappdatabas;
import com.example.carservicesapp.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    TextView mUserName, mPhone;
    Button dateChooser;
    Button from;
    Button to;

    Button save;

    CartAdapter mAdapter;
    List<CartItemsDB> orders;
    RecyclerView mList;
    Myappdatabas myappdatabas;

    Calendar calendar;

    SharedPrefManager prefManager;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        myappdatabas = Myappdatabas.getDatabase(this);
        orders = myappdatabas.myDao().getItems();

        prefManager = SharedPrefManager.getInstance(this);
        user = prefManager.getUserData();

        mUserName = findViewById(R.id.user_name);
        mPhone = findViewById(R.id.phone);
        dateChooser = findViewById(R.id.date);
        from = findViewById(R.id.from);
        to = findViewById(R.id.to);
        save = findViewById(R.id.save);
        mList = findViewById(R.id.cart_list);

        mAdapter = new CartAdapter(this, orders);
        mList.setAdapter(mAdapter);

        if(user.getName() != null){
            mUserName.setText(user.getName());
        }
        if(user.getPhone() != null){
            mPhone.setText(user.getPhone());
        }

        calendar = Calendar.getInstance();

        dateChooser.setOnClickListener(v1 -> {
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "yyyy/MM/dd"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    dateChooser.setText(sdf.format(calendar.getTime()));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        from.setOnClickListener(v1 -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    from.setText( selectedHour + ":" + selectedMinute);
                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        to.setOnClickListener(v1 -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    to.setText( selectedHour + ":" + selectedMinute);
                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date, timefrom, timeto;
                if(dateChooser.getText().equals(getResources().getString(R.string.select_date))){
                    String myFormat = "yyyy/MM/dd"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    date = sdf.format(calendar.getTime());
                }else{
                    date = dateChooser.getText().toString();
                }
                if(from.getText().equals(getResources().getString(R.string.from))){
                    timefrom = "NA";
                }else {
                    timefrom = from.getText().toString();
                }
                if(to.getText().equals(getResources().getString(R.string.to))){
                    timeto = "NA";
                }else {
                    timeto = to.getText().toString();
                }

                if(orders.isEmpty()){
                    Toast.makeText(CartActivity.this, "no orders to send !", Toast.LENGTH_SHORT).show();
                }else
                    checkOut(date, timefrom+"-"+timeto);
            }
        });
    }


    private JSONArray ordersToSend(){

        JSONArray jsonArray = new JSONArray();
        ArrayList<CartItemsDB> orders = (ArrayList<CartItemsDB>) mAdapter.getOrders();

        for (CartItemsDB item : orders){
            JSONObject order = new JSONObject();
            try {

                order.put("details", String.valueOf(item.getDetails()));
                order.put("name", String.valueOf(item.getCategory()));
                order.put("price", String.valueOf(item.getPrice()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(order);
        }

        return jsonArray;
    }

    private void checkOut(String date, String time) {
        String URL = Constants.BASE_URL + Constants.CHECK_OUT;

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(URL)
                .setPriority(Priority.MEDIUM)
                .addBodyParameter("user_id", String.valueOf(user.getId()))
                .addBodyParameter("total_price", calculateTotalPrice())
                .addBodyParameter("status", String.valueOf(Constants.REQUEST_STATUS_NEW))
                .addBodyParameter("date", date)
                .addBodyParameter("time", time)
                .addBodyParameter("orders", ordersToSend().toString())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.hide();
                        Log.e("Result", response.toString());
                        try {
                            Toast.makeText(CartActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        myappdatabas.myDao().deleteAll(mAdapter.getOrders());
                        mAdapter = new CartAdapter(CartActivity.this, new ArrayList<CartItemsDB>());
                        mList.setAdapter(mAdapter);
                        dateChooser.setText(getResources().getString(R.string.select_date));
                        from.setText(getResources().getString(R.string.from));
                        to.setText(getResources().getString(R.string.to));
                        finish();
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e("Error", error.getMessage());
                        pDialog.hide();
                    }
                });

    }

    private String calculateTotalPrice() {
        double total = 0;
        for(CartItemsDB itemsDB : orders){
            total += itemsDB.getPrice();
        }
        return String.valueOf(total);
    }
}