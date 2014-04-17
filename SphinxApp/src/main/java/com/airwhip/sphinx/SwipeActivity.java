package com.airwhip.sphinx;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.airwhip.sphinx.misc.Constants;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;


public class SwipeActivity extends Activity {

    public static boolean canPublishPost = false;
    public static boolean canFacebookPost = false;
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
    private UiLifecycleHelper uiHelper;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private SharedPreferences sharedPreferences;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_SWIPE, MODE_PRIVATE);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        VKUIHelper.onCreate(this);
        VKSdk.initialize(sdkListener, Constants.VK_APP_ID);

        sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!sharedPreferences.getBoolean(Constants.SHOW_SWIPE, true)) {
                    findViewById(R.id.controlLayout).setVisibility(View.GONE);
                }
                if (findViewById(R.id.controlLayout).getVisibility() != View.GONE && position == 1) {
                    findViewById(R.id.controlLayout).setVisibility(View.GONE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.SHOW_SWIPE, false);
                    editor.commit();
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new PreviewFragment();
            }
            return new ResultFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
