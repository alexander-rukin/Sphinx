package com.airwhip.sphinx;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import java.util.ArrayList;
import java.util.List;


public class ResultActivity extends Activity {

    public static boolean canPublishPost = false;
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
            DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.VKONTAKTE);
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
                if (session.getPermissions().contains("publish_actions")) {
                    Request request = Request.newStatusUpdateRequest(
                            session, "ТЕСТ!", new Request.Callback() {
                                @Override
                                public void onCompleted(Response response) {
                                }
                            }
                    );
                    request.executeAsync();
                }
                return;
            }
            if (state.isOpened() && canFacebookPost) {
                DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.FACEBOOK);
                dlg.show(getFragmentManager(), "");
            }
        }
    };
    private UiLifecycleHelper uiHelper;

    private ImageView typeAvatar;
    private TextView typeName;
    private TextView typeDefinition;

    private LoginButton loginBtn;
    private ListView feedBackList;

    private int maxResultIndex;

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
        feedBackList = (ListView) findViewById(R.id.feedBackList);
        loginBtn = (LoginButton) findViewById(R.id.login_button);

        for (int i = 0; i < Characteristic.size(); i++) {
            if (Characteristic.get(i) > Characteristic.get(maxResultIndex)) {
                maxResultIndex = i;
            }
        }

        if (Characteristic.isUFO()) {
            typeAvatar.setImageResource(R.drawable.ufo);
            typeName.setText(R.string.ufo);
            typeDefinition.setText(R.string.ufo_definitions);
        } else {
            typeAvatar.setImageResource(Constants.imgs[maxResultIndex]);
            typeName.setText(getResources().getStringArray(R.array.types)[maxResultIndex].toUpperCase());
            typeDefinition.setText(getResources().getStringArray(R.array.definitions)[maxResultIndex]);
        }
        feedBackList.setFocusable(false);

        List<String> types = new ArrayList<>();
        List<Integer> progress = new ArrayList<>();
        for (int i = 0; i < Characteristic.size() - (Characteristic.containsPikabu() ? 0 : 1); i++) {
            types.add(getResources().getStringArray(R.array.types)[i]);
            progress.add(Characteristic.get(i));
        }

        ArrayAdapter<String> adapter = new CustomizeArrayAdapter(this, types.toArray(new String[types.size()]), progress.toArray(new Integer[progress.size()]), (TextView) findViewById(R.id.sphinxStatistic));
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
                    VKSdk.authorize(VKScope.WALL);
                } else {
                    DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.VKONTAKTE);
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
                    DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.FACEBOOK);
                    dlg.show(getFragmentManager(), "");
                }
            }
        });
        findViewById(R.id.shareTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                String message = "sdjfgpjg[sjd[fpgjks]dpfjkop]sdjfg"; // TODO generate tweet
                tweetIntent.putExtra(Intent.EXTRA_TEXT, message);
                tweetIntent.setType("text/plain");

                List<ResolveInfo> resolvedInfoList = getPackageManager().queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

                boolean resolved = false;
                for (ResolveInfo resolveInfo : resolvedInfoList) {
                    if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                        tweetIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                        resolved = true;
                        break;
                    }
                }
                if (resolved) {
                    startActivity(tweetIntent);
                } else {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        uiHelper.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }

}
