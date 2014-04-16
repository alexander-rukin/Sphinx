package com.airwhip.sphinx;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.facebook.Session;
import com.facebook.widget.LoginButton;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;


public class ResultFragment extends Fragment {

    private LoginButton loginBtn;
    private ListView otherResults;
    private int maxResultIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);

        loginBtn = (LoginButton) rootView.findViewById(R.id.login_button);
        otherResults = (ListView) rootView.findViewById(R.id.otherResults);

        if (Characteristic.isUFO()) {
            ((TextView) rootView.findViewById(R.id.youAreText)).setText(R.string.ufo);
            ((TextView) rootView.findViewById(R.id.definitionText)).setText(R.string.ufo_definitions);
            otherResults.setVisibility(View.GONE);
            rootView.findViewById(R.id.generalStatistics).setVisibility(View.GONE);
        } else {
            for (int i = 0; i < Characteristic.size(); i++) {
                if (Characteristic.get(i) > Characteristic.get(maxResultIndex)) {
                    maxResultIndex = i;
                }
            }
            ((TextView) rootView.findViewById(R.id.youAreText)).setText(getResources().getStringArray(R.array.types)[maxResultIndex].toUpperCase());
            ((ImageView) rootView.findViewById(R.id.avatar)).setImageResource(Constants.imgs[maxResultIndex]);
            ((TextView) rootView.findViewById(R.id.definitionText)).setText(getResources().getStringArray(R.array.definitions)[maxResultIndex]);
            otherResults.setFocusable(false);

            List<String> types = new ArrayList<>();
            List<Integer> progress = new ArrayList<>();
            for (int i = 0; i < Characteristic.size() - (Characteristic.containsPikabu() ? 0 : 1); i++) {
                types.add(getResources().getStringArray(R.array.types)[i]);
                progress.add(Characteristic.get(i));
            }

            ArrayAdapter<String> adapter = new CustomizeArrayAdapter(getActivity(), types.toArray(new String[types.size()]), progress.toArray(new Integer[progress.size()]));
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
        }
        rootView.findViewById(R.id.shareVK).setOnClickListener(new View.OnClickListener() {
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
        rootView.findViewById(R.id.shareFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeActivity.canFacebookPost = true;
                SwipeActivity.canPublishPost = false;
                if (!facebookIsLoggedIn()) {
                    loginBtn.callOnClick();
                } else {
                    DialogFragment dlg = new SocialNetworkDialog(SocialNetworkDialog.SocialNetwork.FACEBOOK);
                    dlg.show(getFragmentManager(), "");
                }
            }
        });
        rootView.findViewById(R.id.shareTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                String message = "sdjfgpjg[sjd[fpgjks]dpfjkop]sdjfg"; // TODO generate tweet
                tweetIntent.putExtra(Intent.EXTRA_TEXT, message);
                tweetIntent.setType("text/plain");

                List<ResolveInfo> resolvedInfoList = getActivity().getPackageManager().queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

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
                    Toast.makeText(getActivity(), R.string.twitter_not_find, Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    private boolean facebookIsLoggedIn() {
        Session session = Session.getActiveSession();
        return session != null && session.isOpened();
    }

}