package com.example.carservicesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "REGISTER_ACTIVITY";

    private final int RC_SIGN_IN = 101;

    GoogleSignInClient mGoogleSignInClient;

    Button mGoogleSignInBtn;

    EditText mNameET, mEmailET, mPasswordET, mPhoneET, mZipET, mAddressET;
    Button  mRegisterBtn;
    TextView mSignUpLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();

        mSignUpLoginBtn.setOnClickListener(v -> {
            finish();
        });

        mRegisterBtn.setOnClickListener(v -> {
            mRegisterBtn.setEnabled(false);
            if(validateUserData()){
                registerUser();
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
                signUp();
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



    }

    private void bindViews() {
        mNameET = findViewById(R.id.full_name);
        mEmailET = findViewById(R.id.email);
        mPasswordET = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.register_btn);
        mSignUpLoginBtn = findViewById(R.id.signup_login_btn);
        mPhoneET = findViewById(R.id.phone);
        mZipET = findViewById(R.id.zip);
        mAddressET = findViewById(R.id.address);
    }

    private boolean validateUserData() {

        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String name = mNameET.getText().toString();
        final String email = mEmailET.getText().toString();
        final String address = mAddressET.getText().toString();
        final String phone = mPhoneET.getText().toString();
        final String zip = mZipET.getText().toString();

        //checking if username is empty
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getResources().getString(R.string.name_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }

        //checking if email is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getResources().getString(R.string.email_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }else{
            String regex = "^(.+)@(.+)$";
            if (!email.trim().matches(regex) ) {
                Toast.makeText(this, "enter a valid email address", Toast.LENGTH_SHORT).show();
                mRegisterBtn.setEnabled(true);
                return false;
            }
        }


        //checking if password is empty
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getResources().getString(R.string.password_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "enter a valid phone number !", Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, getResources().getString(R.string.address_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }

        if (TextUtils.isEmpty(zip)) {
            Toast.makeText(this, getResources().getString(R.string.zip_code_missing), Toast.LENGTH_SHORT).show();
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
        final String name = mNameET.getText().toString();
        final String email = mEmailET.getText().toString().trim();
        final String address = mAddressET.getText().toString();
        final String phone = mPhoneET.getText().toString();
        final String zip = mZipET.getText().toString();

        String url = Constants.BASE_URL + Constants.SIGN_UP;
        AndroidNetworking.post(url)
                .addBodyParameter("name", name)
                .addBodyParameter("email", email)
                .addBodyParameter("password", pass)
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
                                goToMainActivity(null);
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
                        Toast.makeText(RegisterActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMainActivity(GoogleSignInAccount account) {
        if(account != null){
            Toast.makeText(this, "welcome " + account.getGivenName(), Toast.LENGTH_SHORT).show();
            Intent setPassword = new Intent(this, SetPasswordActivity.class);
            setPassword.putExtra("firstName", account.getGivenName());
            setPassword.putExtra("lastName", account.getFamilyName());
            setPassword.putExtra("email", account.getEmail());
            startActivity(setPassword);
        }else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    //..........................google signIn functions........................................................
    private void signUp() {
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
                        mRegisterBtn.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, anError.getMessage());
                        Log.e(TAG, anError.getCause().toString());
                    }
                });

    }
}