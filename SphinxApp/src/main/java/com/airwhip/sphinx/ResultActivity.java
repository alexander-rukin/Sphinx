package com.airwhip.sphinx;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.misc.CustomizeArrayAdapter;
import com.airwhip.sphinx.parser.Characteristic;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ResultActivity extends Activity {

    public static boolean canPublishPost = false;
    public static int maxResultIndex;
    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
            Log.d(Constants.DEBUG_TAG, "onCaptchaError");
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            Log.d(Constants.DEBUG_TAG, "onTokenExpired");
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            Log.d(Constants.DEBUG_TAG, "onAccessDenied");
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.VKONTAKTE, ResultActivity.this);
            dlg.show(getFragmentManager(), "");
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d(Constants.DEBUG_TAG, "onAcceptUserToken");
        }
    };
    private boolean canFacebookPost = false;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (canPublishPost) {
                try {
                    Request request = Request.newUploadPhotoRequest(session, new File(Constants.FILE_PATH), new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            Log.d(Constants.DEBUG_TAG, "POST");
                        }
                    });
                    Bundle params = request.getParameters();
                    params.putString("message", getString(R.string.who_are_you) + "\n" + "www.sphinx-app.com");
                    request.setParameters(params);
                    request.executeAsync();
                } catch (Exception e) {
                    Log.e(Constants.ERROR_TAG, e.getMessage());
                    Toast.makeText(ResultActivity.this, ResultActivity.this.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
                return;
            }
            if (state.isOpened() && canFacebookPost) {
                DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.FACEBOOK, ResultActivity.this);
                dlg.show(getFragmentManager(), "");
            }
        }
    };
    private UiLifecycleHelper uiHelper;
    private ImageView typeAvatar;
    private TextView typeName;
    private TextView typeDefinition;
    private TextView postText;
    private ImageView postAvatar;
    private LoginButton loginBtn;
    private ListView feedBackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        VKUIHelper.onCreate(this);
        VKSdk.initialize(sdkListener, Constants.VK_APP_ID);

        typeAvatar = (ImageView) findViewById(R.id.typeAvatar);
        typeName = (TextView) findViewById(R.id.typeName);
        typeDefinition = (TextView) findViewById(R.id.typeDefinition);
        postText = (TextView) findViewById(R.id.postText);
        postAvatar = (ImageView) findViewById(R.id.postAvatar);
        feedBackList = (ListView) findViewById(R.id.feedBackList);
        loginBtn = (LoginButton) findViewById(R.id.login_button);

        for (int i = 0; i < Characteristic.size(); i++) {
            if (Characteristic.get(i) > Characteristic.get(maxResultIndex)) {
                maxResultIndex = i;
            }
        }

        if (Characteristic.isUFO()) {
            typeAvatar.setImageResource(R.drawable.ufo);
            typeName.setText(R.string.ufo_for_title);
            typeDefinition.setText(R.string.ufo_definitions);
            postText.setText(R.string.ufo_definitions);
            postAvatar.setImageResource(R.drawable.ufo);
        } else {
            typeAvatar.setImageResource(Constants.imgs[maxResultIndex]);
            typeName.setText(String.format(getResources().getString(R.string.sphinx_thinks_you_look_like), getResources().getStringArray(R.array.types_for_title)[maxResultIndex]));
            typeDefinition.setText(getResources().getStringArray(R.array.definitions)[maxResultIndex]);
            postAvatar.setImageResource(Constants.imgs[maxResultIndex]);
        }
        feedBackList.setFocusable(false);

        int[] sorted = new int[Characteristic.size() - (Characteristic.containsPikabu() ? 0 : 1)];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = i;
        }
        for (int i = 0; i < sorted.length; i++) {
            for (int j = i + 1; j < sorted.length; j++) {
                if (Characteristic.get(sorted[i]) < Characteristic.get(sorted[j])) {
                    int tmp = sorted[i];
                    sorted[i] = sorted[j];
                    sorted[j] = tmp;
                }
            }
        }

        List<String> types = new ArrayList<>();
        List<Integer> progress = new ArrayList<>();

        // add study or not
        if (Characteristic.get(Constants.STUDENT_ID) > Constants.MIN) {
            types.add(getString(R.string.studying));
            progress.add(Characteristic.get(Constants.STUDENT_ID));
        } else {
            types.add(getString(R.string.not_studying));
            progress.add(100 - Characteristic.get(Constants.STUDENT_ID));
        }
        // add in relationship or single
        if (true) {
            types.add(getString(R.string.in_relationship));
            progress.add(Characteristic.get(Constants.STUDENT_ID));
        } else {
            types.add(getString(R.string.single));
            progress.add(100 - Characteristic.get(Constants.STUDENT_ID));
        }
        // add two uniq characteristics
        int incorrectField = 0;
        for (int i = 0; i < 2 + incorrectField; i++) {
            if (sorted[i] != Constants.STUDENT_ID && sorted[i] != Constants.LONER_ID) {
                types.add(getResources().getStringArray(R.array.types)[sorted[i]]);
                progress.add(Characteristic.get(sorted[i]));
            } else {
                incorrectField++;
            }
        }
        // add male or female
        if (Characteristic.isMale()) {
            types.add(getString(R.string.man));
            progress.add(Characteristic.getMale());
        } else {
            types.add(getString(R.string.woman));
            progress.add(Characteristic.getFemale());
        }
        // add age category
        types.add(getResources().getStringArray(R.array.ages)[Characteristic.getAgeCategory()]);
        progress.add(-1);

        Characteristic.fillFeedBackCategory(this, types.toArray(new String[types.size()]));
        Characteristic.generateResult(this);

        ArrayAdapter<String> adapter = new CustomizeArrayAdapter(this, types.toArray(new String[types.size()]), progress.toArray(new Integer[progress.size()]),
                (TextView) findViewById(R.id.sphinxStatistic), (TextView) findViewById(R.id.postText));
        feedBackList.setAdapter(adapter);

        // --------fixed bug: ListView in ScrollView--------
        int totalHeight = feedBackList.getPaddingTop() + feedBackList.getPaddingBottom();
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, feedBackList);
            if (item != null) {
                if (item instanceof ViewGroup) {
                    item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                item.measure(0, 0);
                totalHeight += item.getMeasuredHeight();
            }
        }
        ViewGroup.LayoutParams params = feedBackList.getLayoutParams();
        if (params != null) {
            params.height = totalHeight + (feedBackList.getDividerHeight() * (adapter.getCount() - 1));
            feedBackList.setLayoutParams(params);
        }
        // -----------------------------------------------------

        findViewById(R.id.shareVK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VKSdk.isLoggedIn()) {
                    VKSdk.authorize(VKScope.WALL, VKScope.PHOTOS);
                } else {
                    DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.VKONTAKTE, ResultActivity.this);
                    dlg.show(getFragmentManager(), "");
                }
            }
        });
        findViewById(R.id.shareFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canFacebookPost = true;
                canPublishPost = false;
                if (!facebookIsLoggedIn()) {
                    loginBtn.callOnClick();
                } else {
                    DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.FACEBOOK, ResultActivity.this);
                    dlg.show(getFragmentManager(), "");
                }
            }
        });
        findViewById(R.id.shareTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getPackageManager().getApplicationInfo("com.twitter.android", 0);
                    DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.TWITTER, ResultActivity.this);
                    dlg.show(getFragmentManager(), "");
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(ResultActivity.this, R.string.twitter_not_find, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean facebookIsLoggedIn() {
        Session session = Session.getActiveSession();
        return session != null && session.isOpened();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        uiHelper.onResume();
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
    }

    @Override
    protected void onPause() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);

        uiHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);

        VKUIHelper.onDestroy(this);
        uiHelper.onDestroy();
        new File(Constants.FILE_PATH).delete();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedState) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);

        uiHelper.onSaveInstanceState(savedState);
        super.onSaveInstanceState(savedState);
    }

}
