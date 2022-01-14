package com.example.carservicesapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carservicesapp.Constants;
import com.example.carservicesapp.OrderDetailsActivity;
import com.example.carservicesapp.R;
import com.example.carservicesapp.model.Order;


import java.util.ArrayList;
import java.util.List;

public class MyOrdersAdapter  extends RecyclerView.Adapter<MyOrdersAdapter.ViewHolder>{


    Context context;
    private List<Order> orders;

    // RecyclerView recyclerView;
    public MyOrdersAdapter(Context context, ArrayList<Order> orders) {
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

        holder.itemView.setOnClickListener(v -> {
            Intent details = new Intent(context, OrderDetailsActivity.class);
            details.putExtra("user",order.getUserName());
            details.putExtra("id", order.getId());
            details.putExtra("phone",order.getPhone());
            details.putExtra("date", order.getDate());
            details.putExtra("time", order.getTime());
            context.startActivity(details);
        });



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