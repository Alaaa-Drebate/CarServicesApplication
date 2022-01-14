package com.example.carservicesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carservicesapp.adapters.ServicesAdapter;
import com.example.carservicesapp.model.Service;
import com.example.carservicesapp.model.User;
import com.example.carservicesapp.utils.SharedPrefManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;
    public Toolbar mToolBar;

    RecyclerView mServicesList;
    ServicesAdapter mAdapter;
    ArrayList<Service> services;

    TextView mUserName;
    ImageView mLogoutBtn;

    SharedPrefManager prefManager;
    User user;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = SharedPrefManager.getInstance(this);
        user = prefManager.getUserData();

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        navigationView = findViewById(R.id.nav_view);
        mToolBar = findViewById(R.id.toolbar);
        mServicesList = findViewById(R.id.services_list);

        View headerView = navigationView.getHeaderView(0);
        mUserName = headerView.findViewById(R.id.user_name);
        mLogoutBtn = headerView.findViewById(R.id.logout);

        mLogoutBtn.setOnClickListener(v -> {
            prefManager.logout();
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "logged out successfully!", Toast.LENGTH_SHORT).show();
                            PackageManager packageManager = getPackageManager();
                            Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
                            assert intent != null;
                            ComponentName componentName = intent.getComponent();
                            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                            startActivity(mainIntent);
                            Runtime.getRuntime().exit(0);
                        }
                    });

        });

        mUserName.setText(user.getName());


        //disable the shadow when navigation menu opens
        drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));

        setSupportActionBar(mToolBar);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_orders:
                        startActivity(new Intent(MainActivity.this, MyOrders.class));
                        return true;
                    case R.id.nav_update_profile:
                        startActivity(new Intent(MainActivity.this, UpdateProfileActivity.class));
                        return true;

                }
                return false;
            }
        });
        NavigationMenuView navMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        navMenuView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));



        services = new ArrayList<Service>()
        {{
            add(new Service(R.drawable.ic_car_battery, "battery"));
            add(new Service(R.drawable.ic_fuel, "fuel"));
            add(new Service(R.drawable.ic_engine, "engine"));
            add(new Service(R.drawable.ic_tire, "twirl"));
            add(new Service(R.drawable.ic_car_wash, "car wash"));
            add(new Service(R.drawable.ic_check, "normal check"));
            add(new Service(R.drawable.ic_car, "change interior"));
            add(new Service(R.drawable.ic_computer, "computer check"));
        }};

        mAdapter = new ServicesAdapter(this, services);
        mServicesList.setAdapter(mAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if(item.getItemId() == R.id.cart){
            startActivity(new Intent(this, CartActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        super.onStart();
    }



}