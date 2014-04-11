package com.airwhip.sphinx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.misc.CustomizeArrayAdapter;
import com.airwhip.sphinx.parser.Characteristic;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;

import java.util.ArrayList;
import java.util.List;


public class ResultActivity extends Activity {

    private ListView otherResults;
    private int maxResultIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        otherResults = (ListView) findViewById(R.id.otherResults);

        for (int i = 0; i < Characteristic.size(); i++) {
            if (Characteristic.get(i) > Characteristic.get(maxResultIndex)) {
                maxResultIndex = i;
            }
        }
        ((TextView) findViewById(R.id.youAreText)).setText(getResources().getStringArray(R.array.types)[maxResultIndex].toUpperCase());
        ((ImageView) findViewById(R.id.avatar)).setImageResource(Constants.imgs[maxResultIndex]);
        ((TextView) findViewById(R.id.definitionText)).setText("DEFINITION");
        otherResults.setFocusable(false);

        List<String> types = new ArrayList<>();
        List<Integer> progress = new ArrayList<>();
        for (int i = 0; i < Characteristic.size() - (Characteristic.containsPikabu() ? 0 : 1); i++) {
            types.add(getResources().getStringArray(R.array.types)[i]);
            progress.add(Characteristic.get(i));
        }

        ArrayAdapter<String> adapter = new CustomizeArrayAdapter(getApplicationContext(), types.toArray(new String[types.size()]), progress.toArray(new Integer[progress.size()]));
        otherResults.setAdapter(adapter);

        // --------fixed bug: ListView in ScrollView--------
        int totalHeight = otherResults.getPaddingTop() + otherResults.getPaddingBottom();
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, otherResults);
            if (item != null) {
                if (item instanceof ViewGroup) {
                    item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                item.measure(0, 0);
                totalHeight += item.getMeasuredHeight();
            }
        }
        ViewGroup.LayoutParams params = otherResults.getLayoutParams();
        if (params != null) {
            params.height = totalHeight + (otherResults.getDividerHeight() * (adapter.getCount() - 1));
            otherResults.setLayoutParams(params);
        }
        // -----------------------------------------------------

        findViewById(R.id.shareVK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.DEBUG_TAG, "VK");
                VKSdk.authorize(WelcomeActivity.sMyScope, true, false);

            }
        });
        findViewById(R.id.shareFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.DEBUG_TAG, "FACEBOOK");
            }
        });
        findViewById(R.id.shareTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.DEBUG_TAG, "TWITTER");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }

}
