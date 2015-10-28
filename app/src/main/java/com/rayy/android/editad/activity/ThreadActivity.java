package com.rayy.android.editad.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rayy.android.editad.GlobalState;
import com.rayy.android.editad.R;
import com.rayy.android.editad.adapter.ThreadItemAdapter;
import com.rayy.android.editad.constant.CommonConstant;
import com.rayy.android.editad.constant.UriConstant;
import com.rayy.android.editad.model.SmsThread;
import com.rayy.android.editad.util.ActivityHelper;
import com.rayy.android.editad.util.DialogBuilder;
import com.rayy.android.editad.util.TelephoneUtil;

import java.util.ArrayList;
import java.util.List;

public class ThreadActivity extends ActionBarActivity {
    static final String[] THREAD_PROJ = new String[]{"thread_id, address, body, date"};
    static final String tag = ThreadActivity.class.getSimpleName();
    ListView threadList;
    List<SmsThread> threads;
    ContentResolver contentResolver;
    ThreadItemAdapter threadAdapter;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String userDefaultSmsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_thread);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        threadList = (ListView) findViewById(R.id.lvMsgThreads);
        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openThreadView(threads.get(position));
            }
        });

        adsPrepare();

        contentResolver = getContentResolver();
        prepare();
    }

    private void adsPrepare() {
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("B86B2ACAE0C325B2DA7490F96C18F994")
                .build());
    }

    private void openThreadView(SmsThread smsThread) {
        Bundle bundle = new Bundle();
        bundle.putInt(CommonConstant.THREAD_ID, smsThread.theadId);
        bundle.putString(CommonConstant.OTHER_PHOTO, smsThread.avatar);
        bundle.putString(CommonConstant.OWN_PHOTO, TelephoneUtil.getOwnPhoto(this));

        ActivityHelper.startWithBundle(this, ThreadDetailActivity.class, bundle, false);

        GlobalState.MODIFY_FLAG = false;
    }

    private void prepare() {
        Cursor cursor = contentResolver.query(Uri.parse(UriConstant.THREAD_URI), THREAD_PROJ, null, null, "date desc");
        threads = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();
            threads.clear();

            do {
//                for (int i = 0; i < cols; i ++) {
//                    Log.i(tag, cursor.getColumnName(i) + " " + cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i))));
//                }

                SmsThread thread = new SmsThread(cursor.getInt(0),
                        cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1))),
                        cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))),
                        cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(3))));

                Cursor contactCursor = TelephoneUtil.getContactProfile(this, thread.from);

                if (contactCursor != null && contactCursor.getCount() > 0) {
                    contactCursor.moveToFirst();

                    thread.avatar = contactCursor.getString(4);
                    thread.displayName = contactCursor.getString(1);

                    contactCursor.close();
                }

                threads.add(thread);
            } while (cursor.moveToNext());

            cursor.close();
        }

        if (threadAdapter == null) {
            threadAdapter = new ThreadItemAdapter(this, threads);
            threadList.setAdapter(threadAdapter);
        } else {
            threadAdapter.setThreadList(threads);
            threadAdapter.notifyDataSetChanged();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onResume() {
        super.onResume();
        Log.i("", "resume, " + android.os.Build.VERSION.SDK_INT);

        if (pref.getBoolean("exit", false)) {
            return;
        }

        if (GlobalState.MODIFY_FLAG) {
            // reload content
            //prepare();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();//"com.rayy.android.editorad";

            userDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            if (userDefaultSmsApp != null && !userDefaultSmsApp.equals(myPackageName)) {
                // App is not default.
                // Show the "not currently set as the default SMS app" interface

                editor = pref.edit();

                editor.putString("default", userDefaultSmsApp);
                editor.commit();

                boolean change_diag_show = pref.getBoolean("change_diag_show", true);

                if (change_diag_show) {
                    // show alert builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);

                    //final View diag_view = findViewById(R.layout.diag_sms_hint);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_set_default_sms);
                    //builder.setTitle(R.string.diag_set_default_sms);
                    builder.setView(diag_view);
                    //builder.setMessage(R.string.msg_set_default_sms);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Setting default Sms");

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor.putBoolean("change_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(myPackageName);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(myPackageName);
                }

            } else {
                Log.i(tag, "Current app is set the default Sms app");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startChangeSmsDiagActivity(String packageToChange) {
        Log.i(tag, "change to " + packageToChange);

        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageToChange);

        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onStop() {
        super.onStop();

        //pref.edit().clear().apply();
        editor = pref.edit();
        editor.remove("exit").apply();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onBackPressed() {
        pref.edit().putBoolean("exit", true).commit();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String newDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            userDefaultSmsApp = pref.getString("default", "");

            Log.i("", newDefaultSmsApp + ", " + userDefaultSmsApp);

            if (newDefaultSmsApp != null && !newDefaultSmsApp.equals(userDefaultSmsApp)) {

                boolean revert_diag_show = pref.getBoolean("revert_diag_show", true);

                if (revert_diag_show) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);
                    //final View diag_view = findViewById(R.layout.diag_sms_hint);

                    //builder.setTitle(R.string.diag_set_default_sms);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_revert_default_sms);
                    builder.setView(diag_view);
                    //builder.setMessage(R.string.msg_revert_default_sms);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Revert default Sms");

                            //System.gc();

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor = pref.edit();
                                editor.putBoolean("revert_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(userDefaultSmsApp);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(userDefaultSmsApp);
                }
            } else {
                Log.i(tag, "Current app is not set the default Sms app");

                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu, m);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        String help = getResources().getString(R.string.help);
        String help_msg = getResources().getString(R.string.help_msg_);
        String abt = getResources().getString(R.string.about);
        String abt_msg = getResources().getString(R.string.about_msg);

        if (id == R.id.help) {
            DialogBuilder.showAlert(this, help, help_msg + "<br>" + abt_msg, true);
        }

        return true;
    }
}
