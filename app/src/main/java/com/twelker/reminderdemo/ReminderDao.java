package com.twelker.reminderdemo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    @Query("SELECT * FROM reminder")
    public List<Reminder> getAllReminders();

    @Insert
    public void insertReminders(Reminder reminders);

    @Delete
    public void deleteReminders(Reminder reminders);

    @Update
    public void updateReminders(Reminder reminders);
}

