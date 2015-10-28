package com.rayy.android.editad.activity;

import android.app.FragmentManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rayy.android.editad.R;
import com.rayy.android.editad.adapter.SmsItemAdapter;
import com.rayy.android.editad.constant.CommonConstant;
import com.rayy.android.editad.constant.UriConstant;
import com.rayy.android.editad.fragment.EditSmsDiagFragment;
import com.rayy.android.editad.model.SmsMessage;

import java.util.ArrayList;
import java.util.List;

public class ThreadDetailActivity extends ActionBarActivity {
    static final String tag = ThreadDetailActivity.class.getSimpleName();
    ListView threadDetailList;
    ProgressBar progressBar;
    EditSmsDiagFragment editDiag;
    SmsItemAdapter smsItemAdapter;
    List<SmsMessage> messages = new ArrayList<>();
    int threadId;
    String otherPhoto;
    String[] MSG_PROJ = new String[]{"_id", "type", "address", "body", "date"};
    Handler uiHander;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_detail);

        Bundle bundle = getIntent().getExtras();
        threadId = bundle.getInt(CommonConstant.THREAD_ID);
        otherPhoto = bundle.getString(CommonConstant.OTHER_PHOTO);

        threadDetailList = (ListView) findViewById(R.id.threadDetailList);
        //threadDetailList.setStackFromBottom(true);
        threadDetailList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        threadDetailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SmsMessage msg = messages.get(position);

                Bundle bundle = new Bundle();
                bundle.putInt(CommonConstant.MSG_ID, msg.msgId);
                bundle.putString(CommonConstant.SENDER, msg.addr);
                bundle.putInt(CommonConstant.TYPE, msg.type);
                bundle.putString(CommonConstant.BODY, msg.msg);
                bundle.putLong(CommonConstant.DATE, msg.timestamp);

                showEditSmsDialog(bundle);
            }
        });

        smsItemAdapter = new SmsItemAdapter(this, messages);
        smsItemAdapter.setOtherPhoto(otherPhoto);
        threadDetailList.setAdapter(smsItemAdapter);

        progressBar = (ProgressBar) findViewById(R.id.loadingBar);
        uiHander = new Handler(Looper.getMainLooper());

        adsPrepare();
        prepare();
    }

    private void adsPrepare() {
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("B86B2ACAE0C325B2DA7490F96C18F994")
                .build());
    }

    private void prepare() {
        Cursor cursor = getContentResolver().query(Uri.parse(UriConstant.SMS_URI), MSG_PROJ, "thread_id = ?", new String[]{threadId + ""}, "date");

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            messages.clear();

            do {
                SmsMessage smsMessage = new SmsMessage(cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(0))),
                        threadId,
                        cursor.getInt(cursor.getColumnIndex(cursor.getColumnName(1))),
                        cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))),
                        cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3))),
                        cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(4))));

                messages.add(smsMessage);
            } while (cursor.moveToNext());
        }

        smsItemAdapter.setMessages(messages);
        smsItemAdapter.notifyDataSetChanged();

        threadDetailList.setSelection(messages.size() - 1);

        hideLoading();
    }

    protected void showEditSmsDialog(Bundle bundle) {
        FragmentManager fm = getFragmentManager();

        editDiag = new EditSmsDiagFragment();
        editDiag.setArguments(bundle);
        editDiag.setRetainInstance(true);

        editDiag.show(fm, "e_dialog");
    }

    public void notifyDataUpdate() {
        prepare();
    }

    public void showLoading() {
        uiHander.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoading() {
        uiHander.post(new Runnable() {
            @Override
            public void run() {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return false;
    }
}