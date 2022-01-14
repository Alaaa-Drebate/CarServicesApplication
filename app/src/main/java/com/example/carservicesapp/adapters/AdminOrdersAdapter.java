package com.example.carservicesapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.carservicesapp.Constants;
import com.example.carservicesapp.OrderDetailsActivity;
import com.example.carservicesapp.R;
import com.example.carservicesapp.model.Order;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder>{


    Context context;
    private List<Order> orders;

    int chosenStatus = -1;

    // RecyclerView recyclerView;
    public AdminOrdersAdapter(Context context, ArrayList<Order> orders) {
        this.context = context;
        this.orders = orders;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.order_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Order order = orders.get(position);

        holder.date.setText(order.getDate());

        holder.price.setText(String.valueOf(order.getTotalPrice()) + context.getResources().getString(R.string.price_unit));

        switch (order.getStatus()){
            case Constants.REQUEST_STATUS_NEW :
                holder.status.setTextColor(context.getResources().getColor(R.color.status_new));
                holder.status.setText(context.getResources().getString(R.string.status_new));
                break;
            case Constants.REQUEST_STATUS_PROCESSING:
                holder.status.setTextColor(context.getResources().getColor(R.color.status_processing));
                holder.status.setText(context.getResources().getString(R.string.status_processing));
                break;
            case Constants.REQUEST_STATUS_COMPLETE :
                holder.status.setTextColor(context.getResources().getColor(R.color.status_completed));
                holder.status.setText(context.getResources().getString(R.string.status_completed));
                break;
            case Constants.REQUEST_STATUS_REJECTED :
                holder.status.setTextColor(context.getResources().getColor(R.color.status_rejected));
                holder.status.setText(context.getResources().getString(R.string.status_rejected));
                break;
        }

        holder.itemView.setOnLongClickListener(v -> {
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.status_chooser_dialog, null);
            final AlertDialog statusChooserDialog = new AlertDialog.Builder(context).create();
            statusChooserDialog.setView(view);

            TextView yes = view.findViewById(R.id.yes_btn);
            TextView no = view.findViewById(R.id.no_btn);
            RadioGroup group = view.findViewById(R.id.chooser_layout);
            chosenStatus = -1;

            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId){
                        case R.id.state_new:
                            chosenStatus = Constants.REQUEST_STATUS_NEW;
                            break;
                        case R.id.state_processing:
                            chosenStatus = Constants.REQUEST_STATUS_PROCESSING;
                            break;
                        case R.id.state_completed:
                            chosenStatus = Constants.REQUEST_STATUS_COMPLETE;
                            break;
                        case R.id.state_rejected:
                            chosenStatus = Constants.REQUEST_STATUS_REJECTED;
                            break;
                    }
                }
            });

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(chosenStatus == -1){
                        Toast.makeText(context, context.getResources().getString(R.string.chose_at_least_one_status), Toast.LENGTH_SHORT).show();
                    }else
                    if(chosenStatus == order.getStatus()){
                        Toast.makeText(context, context.getResources().getString(R.string.must_chose_new_status), Toast.LENGTH_SHORT).show();
                    }else{
                        order.setStatus(chosenStatus);
                        updateProblemStatus(order, position);
                        statusChooserDialog.dismiss();
                    }



                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusChooserDialog.dismiss();
                }
            });
            statusChooserDialog.show();
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            Intent details = new Intent(context, OrderDetailsActivity.class);
            details.putExtra("id", order.getId());
            details.putExtra("phone",order.getPhone());
            details.putExtra("user",order.getUserName());

            details.putExtra("date", order.getDate());
            details.putExtra("time", order.getTime());
            context.startActivity(details);
        });



    }

    private void updateProblemStatus(Order order, int index) {

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.BASE_URL + Constants.UPDATE_ORDER_STATUS;
        AndroidNetworking.post(url)
                .addBodyParameter("request_id", String.valueOf(order.getId()))
                .addBodyParameter("status", String.valueOf(order.getStatus()))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                updateProblem(index, order);

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        Toast.makeText(context, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateProblem(int index, Order order) {
        orders.set(index, order);
        notifyItemChanged(index);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public TextView status;
        public TextView price;

        public ViewHolder(View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.date);
            this.status = itemView.findViewById(R.id.status_code);
            this.price = itemView.findViewById(R.id.price);
        }
    }





}