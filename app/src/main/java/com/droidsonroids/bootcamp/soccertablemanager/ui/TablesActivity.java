package com.droidsonroids.bootcamp.soccertablemanager.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.Toast;

import com.droidsonroids.bootcamp.soccertablemanager.Const;
import com.droidsonroids.bootcamp.soccertablemanager.R;
import com.droidsonroids.bootcamp.soccertablemanager.adapter.TableAdapter;
import com.droidsonroids.bootcamp.soccertablemanager.event.CreateTableRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.CreateTableResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.GetTablesRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.GetTablesResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.JoinResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.LeaveResponseEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class TablesActivity extends AppCompatActivity {


    @Bind(R.id.table_register_time)
    EditText createTableTimeET;
    @Bind(R.id.table_list)
    RecyclerView recyclerView;
    @Bind(R.id.activity_main_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    SharedPreferences sharedPreferences;
    private TableAdapter mTableAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        getSharedPreferences();
        setUpRecyclerView();
        getTables();
        setupSwipeToRefresh();
        int userID = sharedPreferences.getInt(Const.USER_ID, 0);
        if (userID == 0) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    private void getSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void getTables() {
        EventBus.getDefault().post(new GetTablesRequestEvent());

    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        getTables();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }.execute();
            }
        });
    }

    @OnClick(R.id.unregister_table_button)
    public void unRegister() {
        sharedPreferences.edit().putInt(Const.USER_ID, 0).apply();
        sharedPreferences.edit().putString(Const.USER_NAME, "").apply();
        startActivity(new Intent(this, RegisterActivity.class));
    }


    @OnClick(R.id.add_table_button)
    public void addTable() {
        int userID = sharedPreferences.getInt(Const.USER_ID, 0);
        String time = createTableTimeET.getText().toString();
        if (userID != 0) {
            EventBus.getDefault().post(new CreateTableRequestEvent(time, userID));
        }

    }

    public void showToast(String information) {
        Toast.makeText(getApplicationContext(),
                information, Toast.LENGTH_SHORT).show();
    }


    public void onEventMainThread(CreateTableResponseEvent event) {
        if (event.getApiError() == null) {
            showToast("Create table with id = " + event.getTableId());
        } else {
            showToast("Table not created");
        }

    }

    public void onEventMainThread(GetTablesResponseEvent event) {
        if (event.getApiError() == null) {
            mTableAdapter = new TableAdapter(event.getTables());
            recyclerView.setAdapter(mTableAdapter);

        }
    }

    public void onEventMainThread(JoinResponseEvent event) {
        if (event.getApiError() == null) {
            showToast("Joined to table");
            EventBus.getDefault().post(new GetTablesRequestEvent());
        } else {
            showToast("Table is full");
        }
    }

    public void onEventMainThread(LeaveResponseEvent event) {
        if (event.getApiError() == null) {
            showToast("Leaved from table");
            EventBus.getDefault().post(new GetTablesRequestEvent());
        }
    }
}
