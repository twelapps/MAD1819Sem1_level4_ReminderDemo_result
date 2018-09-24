package com.twelker.reminderdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Local variables
    private List<Reminder> mReminders;
    private ReminderAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private EditText mNewReminderText;

    //Constants used when calling the update activity
    public static final String EXTRA_REMINDER = "Reminder";
    public static final int REQUESTCODE = 1234;
    private int mModifyPosition;

    static AppDatabase db;

    public final static int TASK_GET_ALL_REMINDERS = 0;
    public final static int TASK_DELETE_REMINDER = 1;
    public final static int TASK_UPDATE_REMINDER = 2;
    public final static int TASK_INSERT_REMINDER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize the local variables
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mNewReminderText = findViewById(R.id.editText_main);

        mReminders = new ArrayList<>();

        db = AppDatabase.getInstance(this);

//        updateUI();
        new ReminderAsyncTask(TASK_GET_ALL_REMINDERS).execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user text from the textfield
                String text = mNewReminderText.getText().toString();
                Reminder newReminder = new Reminder(text);

                //Check if some text has been added
                if (!(TextUtils.isEmpty(text))) {

//                    db.reminderDao().insertReminders(newReminder);

                    //Add the text to the list (datamodel)
                    mReminders.add(newReminder);

                    //Tell the adapter that the data set has been modified: the screen will be refreshed.
//                    updateUI();

                    new ReminderAsyncTask(TASK_INSERT_REMINDER).execute(newReminder);

                    //Initialize the EditText for the next item
                    mNewReminderText.setText("");
                } else {
                    //Show a message to the user if the textfield is empty
                    Snackbar.make(view, "Please enter some text in the textfield", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }

            }
        });

        // https://stackoverflow.com/questions/31427881/handling-recyclerview-onclicklistener-in-activity-fragment/40629675
        // https://stackoverflow.com/questions/6645537/how-to-detect-the-swipe-left-or-right-in-android
        // Intercept onClick events and pass control to a second activity in order to update the clicked reminder data.
        // 1. Define a screen gesture detector, for the current demo only for a finger shortly touching
        // the screen and leaving it (onSingleTapUp). You can also create code for swiping, long clicks, etc.
        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });

        // 2. Create an instance of class RecyclerView.OnItemTouchListener
        RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                // (x,y) is the screen position where the finger leaves the screen.

                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    // below code get the position of data
                    mModifyPosition = recyclerView.getChildAdapterPosition(child);

                    Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                    intent.putExtra(EXTRA_REMINDER,  mReminders.get(mModifyPosition));
                    startActivityForResult(intent, REQUESTCODE);

                    return true;
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };

        // 3. Connect it to the current recyclerView
        mRecyclerView.addOnItemTouchListener(onItemTouchListener);


        /*
            Add a touch helper to the RecyclerView to recognize when a user swipes to delete a list entry.
            An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
            and uses callbacks to signal when a user is performing these actions.
        */
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
                            target) {
                        return false;
                    }

                    //Called when a user swipes left or right on a ViewHolder
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                        //Get the index corresponding to the selected position
                        int position = (viewHolder.getAdapterPosition());

//                        db.reminderDao().deleteReminders(mReminders.get(position));

                        new ReminderAsyncTask(TASK_DELETE_REMINDER).execute(mReminders.get(position));

                        mReminders.remove(position);
//                        mAdapter.notifyItemRemoved(position);
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    private void updateUI() {
//        mReminders = db.reminderDao().getAllReminders();
        if (mAdapter == null) {
            mAdapter = new ReminderAdapter(mReminders);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.swapList(mReminders);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUESTCODE) /* Original request code; second activity may be invoked from
    multiple places and it has to keep track from where it was called in order to support multiple functions */ {
            if (resultCode == RESULT_OK) {
                //Put updated reminder data into a temporary reminder
                Reminder updatedReminder = data.getParcelableExtra(MainActivity.EXTRA_REMINDER);

                //In the datamodel, replace the reminder that the user touched by this temporary reminder
//                mReminders.set(mModifyPosition, updatedReminder);

                //Update the user interface accordingly
//                updateUI();

                new ReminderAsyncTask(TASK_UPDATE_REMINDER).execute(updatedReminder);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ReminderAsyncTask extends AsyncTask<Reminder, Void, List> {

        private int taskCode;

        public ReminderAsyncTask(int taskCode) {
            this.taskCode = taskCode;
        }

        @Override
        protected List doInBackground(Reminder... reminders) {
            switch (taskCode){
                case TASK_DELETE_REMINDER:
                    db.reminderDao().deleteReminders(reminders[0]);
                    break;
                case TASK_UPDATE_REMINDER:
                    db.reminderDao().updateReminders(reminders[0]);
                    break;
                case TASK_INSERT_REMINDER:
                    db.reminderDao().insertReminders(reminders[0]);
                    break;
            }

            //To return a new list with the updated data, we get all the data from the database again.
            return db.reminderDao().getAllReminders();
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            onReminderDbUpdated(list);
        }
    }

    public void onReminderDbUpdated(List list) {
        mReminders = list;
        updateUI();
    }

}
