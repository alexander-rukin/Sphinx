package com.airwhip.sphinx;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.airwhip.sphinx.misc.Constants;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Whiplash on 14.04.2014.
 */
public class SocialNetworkDialog extends DialogFragment implements OnClickListener {

    public static String POST_MESSAGE;
    private SocialNetwork network;
    private Context context;

    public SocialNetworkDialog(SocialNetwork network, Context context) {
        super();
        this.network = network;
        this.context = context;
        POST_MESSAGE = context.getResources().getString(R.string.who_are_you) + "\n" + "www.sphinx-app.com";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog, null);
        switch (network) {
            case VKONTAKTE:
                ((TextView) view.findViewById(R.id.messageDialog)).setText(getString(R.string.vk_dialog_message));
                break;
            case FACEBOOK:
                ((TextView) view.findViewById(R.id.messageDialog)).setText(getString(R.string.facebook_dialog_message));
                break;
            case TWITTER:
                ((TextView) view.findViewById(R.id.messageDialog)).setText(getString(R.string.twitter_dialog_message));
                break;
            default:
                Log.wtf(Constants.ERROR_TAG, "Strange social network");
                break;
        }

        view.findViewById(R.id.acceptDialog).setOnClickListener(this);
        view.findViewById(R.id.rejectDialog).setOnClickListener(this);
        return view;
    }

    public void onClick(View v) {
        View content = getActivity().findViewById(R.id.postForm);
        try {
            content.setDrawingCacheEnabled(true);
            Bitmap bitmap = content.getDrawingCache();
            File file = new File(Constants.FILE_PATH);
            {
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.close();
                content.invalidate();
            }
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, e.getMessage());
        } finally {
            content.setDrawingCacheEnabled(false);
        }
        if (v.getId() == R.id.acceptDialog) {
            switch (network) {
                case VKONTAKTE:
                    new VKAsyncTask(context).execute();
                    break;
                case FACEBOOK:
                    facebookClick();
                    break;
                case TWITTER:
                    twitterClick();
                    break;
                default:
                    Log.wtf(Constants.ERROR_TAG, "Strange social network");
                    break;
            }
        }
        dismiss();
    }

    private void twitterClick() {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, POST_MESSAGE);
        tweetIntent.putExtra(Intent.EXTRA_STREAM, Constants.FILE_URI);
        tweetIntent.setType("text/plain");

        List<ResolveInfo> resolvedInfoList = getActivity().getPackageManager().queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                startActivity(tweetIntent);
                break;
            }
        }
    }

    private void facebookClick() {
        try {
            Session session = Session.getActiveSession();
            if (session.getPermissions().contains("publish_actions")) {
                Request request = Request.newUploadPhotoRequest(session, new File(Constants.FILE_PATH), new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        Log.d(Constants.DEBUG_TAG, "POST");
                    }
                });
                Bundle params = request.getParameters();
                params.putString("message", POST_MESSAGE);
                request.setParameters(params);
                request.executeAsync();
            } else {
                Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(getActivity(), Arrays.asList("publish_actions"));
                session.requestNewPublishPermissions(newPermissionsRequest);
                ResultActivity.canPublishPost = true;
            }
        } catch (Exception e) {
            Log.e(Constants.ERROR_TAG, e.getMessage());
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }
    }

    public static enum SocialNetwork {
        VKONTAKTE, FACEBOOK, TWITTER
    }

    private class VKAsyncTask extends AsyncTask<Void, Void, Void> {
        Context context;

        public VKAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            VKRequest request = VKApi.users().get();
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        final int userId = response.json.getJSONArray("response").getJSONObject(0).getInt("id");
                        VKRequest image = VKApi.uploadWallPhotoRequest(new File(Constants.FILE_PATH), userId, 0);
                        image.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                VKAttachments attachment = new VKAttachments();
                                VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                                attachment.add(photoModel);
                                VKRequest request = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.MESSAGE, POST_MESSAGE, VKApiConst.ATTACHMENTS, attachment));
                                request.executeWithListener(new VKRequest.VKRequestListener() {
                                    @Override
                                    public void onComplete(VKResponse response) {
                                        super.onComplete(response);
                                    }

                                    @Override
                                    public void onError(VKError error) {
                                        Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(VKError error) {
                                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                        Log.d(Constants.ERROR_TAG, e.getMessage());
                    }
                }

                @Override
                public void onError(VKError error) {
                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
            });

            return null;
        }
    }
}
