package com.airwhip.sphinx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
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
import com.airwhip.sphinx.getters.CallLogInformation;
import com.airwhip.sphinx.getters.MusicInformation;
import com.airwhip.sphinx.getters.SMSInformation;
import com.airwhip.sphinx.misc.Internet;
import com.airwhip.sphinx.misc.Names;
import com.airwhip.sphinx.parser.Characteristic;
import com.airwhip.sphinx.parser.InformationParser;

public class WelcomeActivity extends Activity {

    private ImageButton circle;
    private TextView startText;
    private TextView tipText;
    private ImageView plugImage;
    private ImageView socketImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        circle = (ImageButton) findViewById(R.id.circle);
        startText = (TextView) findViewById(R.id.startText);
        tipText = (TextView) findViewById(R.id.tipText);

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            findViewById(R.id.tabletImage).setVisibility(View.VISIBLE);
            tipText.setText(getString(R.string.do_not_support_tablets));
            startText.setVisibility(View.INVISIBLE);
        } else {
            plugImage = (ImageView) findViewById(R.id.plugImage);
            socketImage = (ImageView) findViewById(R.id.socketImage);

            circle.setOnClickListener(new StartButtonClick(ProgramState.START));

            Names.generate(this);
            Characteristic.initDataBase(this);
            Characteristic.updateDataBase("USER_ID", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        }
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
                    tipText.setText(" ");
                    circle.setOnClickListener(null);
                    new ImageLoader().execute();
                    break;
                case NO_INTERNET:
                    plugImage.setVisibility(View.VISIBLE);
                    socketImage.setVisibility(View.VISIBLE);
                    findViewById(R.id.noInternetText).setVisibility(View.VISIBLE);
                    tipText.setText(getString(R.string.check_internet));
                    tipText.setVisibility(View.VISIBLE);
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
            if (Build.VERSION.SDK_INT <= 15) {
                fadeOut = new Fade(startText, 1f);
                startText.setText(" ");
            }
            switch (state) {
                case START:
                    if (Internet.checkInternetConnection(getApplicationContext())) {
                        fadeOut.setAnimationListener(new StartButtonAnimation(ProgramState.START));
                        startText.startAnimation(fadeOut);
                    } else {
                        fadeOut.setAnimationListener(new StartButtonAnimation(ProgramState.NO_INTERNET));
                        startText.startAnimation(fadeOut);
                    }
                    tipText.setVisibility(View.INVISIBLE);
                    break;
                case NO_INTERNET:
                    if (Internet.checkInternetConnection(getApplicationContext())) {
                        plugImage.startAnimation(new Fade(plugImage, 0f));
                        socketImage.startAnimation(new Fade(socketImage, 0f));
                        findViewById(R.id.noInternetText).startAnimation(new Fade(findViewById(R.id.noInternetText), 0f));
                        fadeOut.setAnimationListener(new StartButtonAnimation(ProgramState.START));
                        tipText.startAnimation(fadeOut);
                    } else {
                        int dist = getResources().getInteger(R.integer.move_dist);
                        plugImage.startAnimation(new Move(-dist, dist, false));
                        socketImage.startAnimation(new Move(dist, -dist, false));
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
            tipText.setVisibility(View.VISIBLE);
            tipText.setText(getString(R.string.preparing));
        }

        @Override
        protected Void doInBackground(Void... params) {
            Characteristic.setProgress(AccountInformation.size(getApplicationContext()) + ApplicationInformation.size(getApplicationContext()) + BrowserInformation.size(getApplicationContext())
                    + CallLogInformation.size(getApplicationContext()) + MusicInformation.size(getApplicationContext()) + SMSInformation.size(getApplicationContext()), new ProgressUpdater());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tipText.setText(getString(R.string.analyzing));
                    startText.setText("0%");
                    Animation fadeIn = new Fade(startText, 1);
                    fadeIn.setDuration(200);
                    startText.startAnimation(fadeIn);
                }
            });

            SMSInformation.get(getApplicationContext());
            CallLogInformation.get(getApplicationContext());
            StringBuilder partOfXml = AccountInformation.get(getApplicationContext());
            InformationParser parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.ACCOUNT);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.updateDataBase("ACCOUNT", partOfXml.toString());
            partOfXml = ApplicationInformation.get(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.APPLICATION);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.updateDataBase("APPLICATION", partOfXml.toString());
            partOfXml = BrowserInformation.getHistory(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.HISTORY);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.updateDataBase("HISTORY", partOfXml.toString());
            partOfXml = BrowserInformation.getBookmarks(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.BOOKMARKS);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.updateDataBase("BOOKMARKS", partOfXml.toString());
            partOfXml = MusicInformation.get(getApplicationContext());
            parser = new InformationParser(getApplicationContext(), partOfXml, InformationParser.ParserType.MUSIC);
            Characteristic.addAll(parser.getAllWeight(), parser.getAllMax());
            Characteristic.updateDataBase("MUSIC", partOfXml.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startText.setText("100%");
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public class ProgressUpdater {
        public void updateValue(final int value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startText.setText(String.valueOf(value) + "%");
                }
            });
        }
    }
}