package com.example.carservicesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.carservicesapp.model.User;
import com.example.carservicesapp.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

public class SetPasswordActivity extends AppCompatActivity {

    public static final String TAG = "Set Password Activity";


    EditText mPasswordET, mPasswordRetypeET;
    Button mRegisterBtn;

    String fName, lName;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        bindViews();

        Intent sender = getIntent();
        if (sender != null){
            fName = sender.getStringExtra("firstName");
            lName = sender.getStringExtra("lastName");
            email = sender.getStringExtra("email");
        }

        mRegisterBtn.setOnClickListener(v -> {
            mRegisterBtn.setEnabled(false);
            if(validateUserData()){
                registerUser();
            }
        });
    }

    private void bindViews() {
        mPasswordRetypeET = findViewById(R.id.password_retype);
        mPasswordET = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.register_btn);
    }

    private boolean validateUserData() {

        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String passRetype = mPasswordRetypeET.getText().toString();

        //checking if username is empty
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getResources().getString(R.string.password_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }

        //checking if password is empty
        if (!TextUtils.equals(pass, passRetype)) {
            Toast.makeText(this, getResources().getString(R.string.password_not_matching_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }


        return true;
    }

    private void registerUser() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String name = fName + " " + lName;
        final String mail = email;

        String url = Constants.BASE_URL + Constants.SIGN_UP;
        AndroidNetworking.post(url)
                .addBodyParameter("name", name)
                .addBodyParameter("email", mail)
                .addBodyParameter("password", pass)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        mRegisterBtn.setEnabled(true);

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
                                goToMainActivity();
                                finish();

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mRegisterBtn.setEnabled(true);
                        Toast.makeText(SetPasswordActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

}