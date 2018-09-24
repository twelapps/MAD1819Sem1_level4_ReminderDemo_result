package com.twelker.reminderdemo;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Reminder.class}, version = 1)
public abstract  class AppDatabase extends RoomDatabase {

    public abstract ReminderDao reminderDao();

    private final static String NAME_DATABASE = "reminder_db";

    //Static instance
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {

        if(sInstance == null) {
            sInstance = Room.databaseBuilder(context, AppDatabase.class,   NAME_DATABASE).allowMainThreadQueries().build();
        }
        return sInstance;
    }
}

