package com.example.carservicesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.carservicesapp.model.User;
import com.example.carservicesapp.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateProfileActivity extends AppCompatActivity {

    EditText mNameET, mEmailET, mPhoneET, mZipET, mAddressET;
    Button mSaveBtn;

    User user;
    SharedPrefManager prefManager;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        prefManager = SharedPrefManager.getInstance(this);
        user = prefManager.getUserData();

        bindViews();

        if(user.getName() != null){
            mNameET.setText(user.getName());
        }
        if(user.getEmail() != null){
            mEmailET.setText(user.getEmail());
        }
        if(!user.getPhone().equals("0")){
            mPhoneET.setText(user.getPhone());
        }
        if(!user.getAddress().equals("0")){
            mAddressET.setText(user.getAddress());
        }
        if(!user.getZipCode().equals("0")){
            mZipET.setText(user.getZipCode());
        }

        mEmailET.setEnabled(false);

        userId = user.getId();


        mSaveBtn.setOnClickListener(v -> {
            mSaveBtn.setEnabled(false);
            if(validateUserData()){
                registerUser();
            }
        });


    }

    private void bindViews() {
        mNameET = findViewById(R.id.full_name);
        mEmailET = findViewById(R.id.email);
        mSaveBtn = findViewById(R.id.save_btn);
        mPhoneET = findViewById(R.id.phone);
        mZipET = findViewById(R.id.zip);
        mAddressET = findViewById(R.id.address);
    }

    private boolean validateUserData() {

        //first getting the values
        final String name = mNameET.getText().toString();
        final String email = mEmailET.getText().toString();
        final String address = mAddressET.getText().toString();
        final String phone = mPhoneET.getText().toString();
        final String zip = mZipET.getText().toString();

        //checking if username is empty
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getResources().getString(R.string.name_missing_message), Toast.LENGTH_SHORT).show();
            mSaveBtn.setEnabled(true);
            return false;
        }

        //checking if email is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getResources().getString(R.string.email_missing_message), Toast.LENGTH_SHORT).show();
            mSaveBtn.setEnabled(true);
            return false;
        }


        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "enter a valid phone number !", Toast.LENGTH_SHORT).show();
            mSaveBtn.setEnabled(true);
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, getResources().getString(R.string.address_missing_message), Toast.LENGTH_SHORT).show();
            mSaveBtn.setEnabled(true);
            return false;
        }

        if (TextUtils.isEmpty(zip)) {
            Toast.makeText(this, getResources().getString(R.string.zip_code_missing), Toast.LENGTH_SHORT).show();
            mSaveBtn.setEnabled(true);
            return false;
        }


        return true;
    }

    private void registerUser() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        //getting the values
        final int id = userId;
        final String name = mNameET.getText().toString();
        final String address = mAddressET.getText().toString();
        final String phone = mPhoneET.getText().toString();
        final String zip = mZipET.getText().toString();

        String url = Constants.BASE_URL + Constants.UPDATE_USER;
        AndroidNetworking.post(url)
                .addBodyParameter("id", String.valueOf(id))
                .addBodyParameter("name", name)
                .addBodyParameter("address", address)
                .addBodyParameter("phone", phone)
                .addBodyParameter("zip_code", zip)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        mSaveBtn.setEnabled(true);

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("data");
                                User user;
                                user = new User(
                                        Integer.parseInt(userJson.getString("id")),
                                        userJson.getString("name"),
                                        userJson.getString("email"),
                                        userJson.getString("phone"),
                                        userJson.getString("address"),
                                        userJson.getString("zip_code")
                                );

                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                finish();

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mSaveBtn.setEnabled(true);
                        Toast.makeText(UpdateProfileActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}