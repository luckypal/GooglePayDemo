package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.myapplication.googlepay.CheckoutActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;


public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref = null;

    final static String FB_TOKE_KEY = "FBTOKENKEY";
    final static String USER_REGISTER_KEY = "USERREGISTERKEY";
    final static String FB_DEMO_RECEIVER = "FB_DEMO_RECEIVER";

    private String messageId;

    public class FBMessageReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(FB_DEMO_RECEIVER)) {

                String action = intent.getExtras().getString("action");
                if (TextUtils.equals(ACTION_GOT_NOTIFICATION, action)) {
                    builderGotNotification.setTitle(intent.getExtras().getString("title"));
                    builderGotNotification.setMessage(intent.getExtras().getString("message"));
                    messageId = intent.getExtras().getString("id");
                    AlertDialog dialog = builderGotNotification.create();
                    Toast.makeText(MainActivity.this, "GOT NOTIFICATION", Toast.LENGTH_LONG).show();
                    dialog.show();
                } else if (TextUtils.equals(ACTION_EXPIRED, action)) {
                    builderExpired.setTitle(intent.getExtras().getString("title"));
                    builderExpired.setMessage(intent.getExtras().getString("message"));
                    AlertDialog dialog = builderExpired.create();
                    Toast.makeText(MainActivity.this, "Notification Expired", Toast.LENGTH_LONG).show();
                    dialog.show();
                }
            }
        }
    }

    AlertDialog.Builder builderGotNotification = null;

    AlertDialog.Builder builderExpired = null;

    BroadcastReceiver broadcastReceiver = null;


    DemoAPI demoAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        sharedPref = getPreferences(Context.MODE_PRIVATE);


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://1-dot-taxi2dealin.appspot.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        demoAPI = retrofit.create(DemoAPI.class);

        storeFBToken();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton googlepay = findViewById(R.id.googlepay);
        googlepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, CheckoutActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        final AppCompatEditText carName = findViewById(R.id.etCarName);
        final AppCompatEditText name = findViewById(R.id.etName);
        final AppCompatButton btnRegister = findViewById(R.id.btnRegister);
        final AppCompatButton btnClear = findViewById(R.id.btnClear);
        final AppCompatTextView tvMessage = findViewById(R.id.tvMessage);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(carName.getText()) || TextUtils.isEmpty(name.getText())) {
                    Toast.makeText(MainActivity.this, "Name or Car Name is empty", Toast.LENGTH_LONG).show();
                } else {

                    try {


                        final Call<TaxiDriver> call = demoAPI.save(new TaxiDriver(getFbToke(), name.getText().toString(), carName.getText().toString()));

                        AsyncTask task = new AsyncTask() {
                            @Override
                            protected Response<TaxiDriver> doInBackground(Object[] objects) {
                                try {
                                    return call.execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                Toast.makeText(MainActivity.this, "Store on Server" + o, Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(USER_REGISTER_KEY, true);
                                editor.commit();
                                setView(carName, name, btnRegister, btnClear, tvMessage);

                            }
                        }.execute();


                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }


                }
            }


        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(USER_REGISTER_KEY, false);
                editor.commit();

                setView(carName, name, btnRegister, btnClear, tvMessage);

            }
        });


        setView(carName, name, btnRegister, btnClear, tvMessage);

        builderGotNotification = new AlertDialog.Builder(this);
// Add the buttons
        builderGotNotification.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {

                final Call<String> call = demoAPI.accept(getFbToke(), messageId);

                // User clicked OK button
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Response<String> doInBackground(Object[] objects) {
                        try {
                            return call.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Store on Server" + o, Toast.LENGTH_SHORT).show();
                        setView(carName, name, btnRegister, btnClear, tvMessage);
                    }
                }.execute();
            }
        });
        builderGotNotification.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builderGotNotification.setCancelable(false);


        builderExpired = new AlertDialog.Builder(this);

        builderExpired.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builderExpired.setCancelable(false);


    }

    public final static String ACTION_GOT_NOTIFICATION = "ACTION_GOT_NOTIFICATION";
    public final static String ACTION_EXPIRED = "ACTION_EXPIRED";

    private void setView(AppCompatEditText carName, AppCompatEditText name, AppCompatButton btnRegister, AppCompatButton btnClear, AppCompatTextView tvMessage) {
        if (isRegister()) {
            name.setVisibility(View.GONE);
            carName.setVisibility(View.GONE);
            btnRegister.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);

            IntentFilter filter = new IntentFilter(FB_DEMO_RECEIVER);
            broadcastReceiver = new FBMessageReciver();
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        } else {
            name.setVisibility(View.VISIBLE);
            carName.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            tvMessage.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);

            if (broadcastReceiver != null)
                LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    public void finish() {
        if (broadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        super.finish();
    }

    private String getFbToke() {
        return sharedPref.getString(FB_TOKE_KEY, "");
    }

    private boolean isRegister() {
        return sharedPref.getBoolean(USER_REGISTER_KEY, false);
    }


    private void storeFBToken() {

        String fbToken = getFbToke();

        if (TextUtils.isEmpty(fbToken)) ;
        {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(FB_TOKE_KEY, token);
                            editor.commit();

                            Toast.makeText(MainActivity.this, "Got the Firebase Token", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

}
