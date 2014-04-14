package com.airwhip.sphinx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airwhip.sphinx.parser.Characteristic;
import com.vk.sdk.VKUIHelper;

import org.w3c.dom.Text;


public class PreviewActivity extends Activity {

    private final static int SCREEN_MULTIPLIER = 2;

    TextView genderText;

    TextView maleValueText;
    TextView femaleValueText;

    ImageView maleValueImage;
    ImageView femaleValueImage;

    TextView smthText;

    TextView ageText;

    TextView continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        genderText = (TextView) findViewById(R.id.genderText);

        maleValueText = (TextView) findViewById(R.id.maleValueText);
        femaleValueText = (TextView) findViewById(R.id.femaleValueText);

        maleValueImage = (ImageView) findViewById(R.id.maleValueImage);
        femaleValueImage = (ImageView) findViewById(R.id.femaleValueImage);

        smthText = (TextView) findViewById(R.id.smthText);

        ageText = (TextView) findViewById(R.id.ageText);

        continueButton = (TextView) findViewById(R.id.continueButton);

        if (Characteristic.isMale()) {
            genderText.setText(getString(R.string.male));
            smthText.setText(String.format(getString(R.string.smth_text), getString(R.string.male).toLowerCase()));
        } else {
            genderText.setText(getString(R.string.female));
            smthText.setText(String.format(getString(R.string.smth_text), getString(R.string.female).toLowerCase()));
        }

        maleValueText.setText(Characteristic.getMale() + "%");
        femaleValueText.setText(Characteristic.getFemale() + "%");
        maleValueImage.getLayoutParams().width = Characteristic.getMale() * SCREEN_MULTIPLIER;
        femaleValueImage.getLayoutParams().width = Characteristic.getFemale() * SCREEN_MULTIPLIER;
        ageText.setText(String.valueOf(Characteristic.getAge()));

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
