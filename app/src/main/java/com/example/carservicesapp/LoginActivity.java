package com.example.carservicesapp;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.example.carservicesapp.model.User;
import com.example.carservicesapp.utils.SharedPrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LOGIN_ACTIVITY";

    private final int RC_SIGN_IN = 101;

    GoogleSignInClient mGoogleSignInClient;

    Button mGoogleSignInBtn;

    EditText mEmailET, mPasswordET;
    Button mLoginBtn;
    TextView mLoginSignUpBtn;


    Button mAdminBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();

        mLoginSignUpBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        mLoginBtn.setOnClickListener(v -> {
            mLoginBtn.setEnabled(false);
            if(validateUserData()){
                userLogin();
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInBtn = findViewById(R.id.sign_in_button);

        mGoogleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if(account != null){
//            if(userExisted(account.getEmail())){
//                goToMainActivity(null);
//            }else {
//                goToMainActivity(account);
//            }
//        }

        mAdminBtn.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            LayoutInflater factory = LayoutInflater.from(this);
            final View view = factory.inflate(R.layout.enter_manager_key_dialog, null);
            final AlertDialog enterManagerKeyDialog = new AlertDialog.Builder(this).create();
            enterManagerKeyDialog.setCancelable(true);
            enterManagerKeyDialog.setView(view);

            EditText keyET = view.findViewById(R.id.key);
            TextView save = view.findViewById(R.id.save);
            TextView cancel = view.findViewById(R.id.cancel);


            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String key = keyET.getText().toString();
                    String managerKey = getResources().getString(R.string.manager_key);

                    if(TextUtils.isEmpty(key)){
                        Toast.makeText(LoginActivity.this, "manager key must not be empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        if(TextUtils.equals(key, managerKey)){
                            Toast.makeText(LoginActivity.this, "welcome sir", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, AdminOrdersActivity.class));
                            finish();
                            enterManagerKeyDialog.dismiss();
                        }else{
                            Toast.makeText(LoginActivity.this, "wrong key! please try again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterManagerKeyDialog.dismiss();
                }
            });
            enterManagerKeyDialog.show();

        });



    }

    private void bindViews() {
        mEmailET = findViewById(R.id.email);
        mPasswordET = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.login_btn);
        mLoginSignUpBtn = findViewById(R.id.login_signup_btn);
        mAdminBtn = findViewById(R.id.admin_btn);
    }

    private boolean validateUserData() {

        //first getting the values
        final String email = mEmailET.getText().toString();
        final String pass = mPasswordET.getText().toString();

        //checking if username is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getResources().getString(R.string.email_missing_message), Toast.LENGTH_SHORT).show();
            mLoginBtn.setEnabled(true);
            return false;
        }

        //checking if password is empty
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getResources().getString(R.string.password_missing_message), Toast.LENGTH_SHORT).show();
            mLoginBtn.setEnabled(true);
            return false;
        }

        return true;

    }


    private void userLogin() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        //first getting the values
        final String email = mEmailET.getText().toString();
        final String pass = mPasswordET.getText().toString();

        String url = Constants.BASE_URL + Constants.LOGIN;

        AndroidNetworking.post(url)
                .addBodyParameter("email", email)
                .addBodyParameter("password", pass)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        mLoginBtn.setEnabled(true);
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            Log.e(TAG, response);
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                JSONObject userJson = obj.getJSONObject("data");

                                //getting the user from the response
                                User user;
                                //getting the user from the response
                                user = new User(
                                        Integer.parseInt(userJson.getString("id")),
                                        userJson.getString("name"),
                                        userJson.getString("email"),
                                        userJson.getString("phone"),
                                        userJson.getString("address"),
                                        userJson.getString("zip_code")
                                );
                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(LoginActivity.this).userLogin(user);

                                Toast.makeText(LoginActivity.this, "welcome " + user.getName(), Toast.LENGTH_SHORT).show();

                                goToMainActivity(null);
                                finish();

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(LoginActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());

                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mLoginBtn.setEnabled(true);
                        Toast.makeText(LoginActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(LoginActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, anError.getMessage());
                        Log.e(TAG, anError.getCause().toString());
                    }
                });

    }

    private void goToMainActivity(GoogleSignInAccount account) {
        if(account != null){
            Toast.makeText(this, "welcome " + account.getGivenName(), Toast.LENGTH_SHORT).show();
            Intent toMain = new Intent(this, SetPasswordActivity.class);
            toMain.putExtra("firstName", account.getGivenName());
            toMain.putExtra("lastName", account.getFamilyName());
            toMain.putExtra("email", account.getEmail());
            toMain.putExtra("image", account.getPhotoUrl());
            startActivity(toMain);
        }else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }


    //..........................google signIn functions........................................................
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            userExisted(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }
    //..........................google signIn functions........................................................

    private void userExisted(GoogleSignInAccount account){

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.BASE_URL + Constants.GET_USER_BY_EMAIL;
        AndroidNetworking.post(url)
                .addBodyParameter("email", account.getEmail())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        mLoginBtn.setEnabled(true);

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
                                goToMainActivity(null);

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            } else if(obj.getInt("status") == -10){
                                goToMainActivity(account);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();


                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mLoginBtn.setEnabled(true);
                        Toast.makeText(LoginActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, anError.getMessage());
                        Log.e(TAG, anError.getCause().toString());
                    }
                });

    }
}