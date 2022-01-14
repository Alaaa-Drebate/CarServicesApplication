package com.example.carservicesapp.sqlite;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CartItemsDB.class},version = 1)
public abstract class Myappdatabas extends RoomDatabase {
    private static Myappdatabas INSTANCE;

    public abstract myDao myDao();

    public static Myappdatabas getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    Myappdatabas.class, "cartdatabase")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}