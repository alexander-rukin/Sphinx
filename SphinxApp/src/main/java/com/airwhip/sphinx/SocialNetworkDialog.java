package com.airwhip.sphinx;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.airwhip.sphinx.misc.Constants;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.Arrays;

/**
 * Created by Whiplash on 14.04.2014.
 */
public class SocialNetworkDialog extends DialogFragment implements OnClickListener {

    private SocialNetwork network;

    public SocialNetworkDialog(SocialNetwork network) {
        super();
        this.network = network;
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
        if (v.getId() == R.id.acceptDialog) {
            switch (network) {
                case VKONTAKTE:
                    vkClick();
                    break;
                case FACEBOOK:
                    facebookClick();
                    break;
                case TWITTER:
                    break;
                default:
                    Log.wtf(Constants.ERROR_TAG, "Strange social network");
                    break;
            }
        }
        dismiss();
    }

    private void facebookClick() {
        Session session = Session.getActiveSession();
        if (session.getPermissions().contains("publish_actions")) {
            ResultActivity.canPublishPost = true;
            Request request = Request.newStatusUpdateRequest(
                    session, "ТЕСТ!", new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                        }
                    }
            );
            request.executeAsync();
        } else {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(getActivity(), Arrays.asList("publish_actions"));
            session.requestNewPublishPermissions(newPermissionsRequest);
            ResultActivity.canPublishPost = true;
        }
    }

    private void vkClick() {
        VKRequest request = VKApi.users().get();
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    int userId = response.json.getJSONArray("response").getJSONObject(0).getInt("id");
                    VKRequest request = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, String.valueOf(userId), VKApiConst.MESSAGE, "ТЕСТ!"));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                        }
                    });
                } catch (Exception e) {
                    Log.d(Constants.ERROR_TAG, "WAT??");
                }
            }
        });

//                VKRequest image = VKApi.uploadWallPhotoRequest(new VKUploadImage(BitmapFactory.decodeResource(getResources(), R.drawable.anime_addicted), VKImageParameters.jpgImage(0.9f)), 26284681, 0);
//                image.executeWithListener(new VKRequest.VKRequestListener() {
//                    @Override
//                    public void onComplete(VKResponse response) {
//                        super.onComplete(response);
//                        VKAttachments attachments = new VKAttachments();
//                        VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
//                        attachments.add(photoModel);
//                        VKRequest request = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, "26284681", VKApiConst.MESSAGE, "ТЕСТ!", VKApiConst.ATTACHMENTS, attachments));
//                        request.executeWithListener(new VKRequest.VKRequestListener() {
//                            @Override
//                            public void onComplete(VKResponse response) {
//                            }
//                        });
//                    }
//                });
    }

    public static enum SocialNetwork {
        VKONTAKTE, FACEBOOK, TWITTER
    }
}
