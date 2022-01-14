package com.example.carservicesapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carservicesapp.R;
import com.example.carservicesapp.sqlite.CartItemsDB;
import com.example.carservicesapp.sqlite.Myappdatabas;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>{


    Context context;
    private List<CartItemsDB> orders;
    Myappdatabas myappdatabas;

    // RecyclerView recyclerView;
    public CartAdapter(Context context, List<CartItemsDB> orders) {
        this.context = context;
        this.orders = orders;
        myappdatabas = Myappdatabas.getDatabase(context);
    }

    public List<CartItemsDB> getOrders() {
        return orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.cart_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CartItemsDB order = orders.get(position);

        holder.title.setText(order.getCategory());

        holder.itemView.setOnLongClickListener(v -> {

            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.delete_confirmation_dialog, null);
            final AlertDialog deleteProductDialog = new AlertDialog.Builder(context).create();
            deleteProductDialog.setView(view);

            TextView yes = view.findViewById(R.id.yes_btn);
            TextView no = view.findViewById(R.id.no_btn);


            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //to see changes immediately
                    removeOrder(position);
                    //when done dismiss;
                    deleteProductDialog.dismiss();

                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteProductDialog.dismiss();
                }
            });
            deleteProductDialog.show();

            return true;
        });

    }

    private void removeOrder(int index) {
        myappdatabas.myDao().deleteItem(orders.get(index));
        orders.remove(index);
        notifyItemRemoved(index);
    }




    @Override
    public int getItemCount() {
        return orders.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
        }
    }





}
