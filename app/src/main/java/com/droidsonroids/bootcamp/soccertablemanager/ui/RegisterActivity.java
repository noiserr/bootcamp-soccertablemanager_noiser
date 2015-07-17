package com.droidsonroids.bootcamp.soccertablemanager.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.droidsonroids.bootcamp.soccertablemanager.Const;
import com.droidsonroids.bootcamp.soccertablemanager.R;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterResponseEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class RegisterActivity extends AppCompatActivity {

    @Bind(R.id.registration_id_et)
    EditText userIdEditText;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSharedPreferences();
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.register_button)
    public void registerUser(View view) {
        EventBus.getDefault().post(new RegisterRequestEvent(userIdEditText.getText() + ""));
    }

    private void getSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }


    public void showToast(String information) {
        Toast.makeText(getApplicationContext(),
                information, Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(RegisterResponseEvent event) {
        if (event.getApiError() == null) {
            showToast("Registered with id= " + event.getUserId());
            sharedPreferences.edit().putInt(Const.USER_ID, event.getUserId()).apply();
            sharedPreferences.edit().putString(Const.USER_NAME, userIdEditText.getText().toString()).apply();
            startActivity(new Intent(this, TablesActivity.class));
        } else {
            showToast("Cant Registered");
        }
    }
}
