package com.airwhip.sphinx;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airwhip.sphinx.parser.Characteristic;


public class PreviewFragment extends Fragment {

    private final static int SCREEN_MULTIPLIER = 2;
    TextView genderText;
    TextView maleValueText;
    TextView femaleValueText;
    ImageView maleValueImage;
    ImageView femaleValueImage;
    TextView genderDefinition;
    TextView ageText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preview, container, false);

        genderText = (TextView) rootView.findViewById(R.id.genderText);

        maleValueText = (TextView) rootView.findViewById(R.id.maleValueText);
        femaleValueText = (TextView) rootView.findViewById(R.id.femaleValueText);

        maleValueImage = (ImageView) rootView.findViewById(R.id.maleValueImage);
        femaleValueImage = (ImageView) rootView.findViewById(R.id.femaleValueImage);

        genderDefinition = (TextView) rootView.findViewById(R.id.genderDefinition);

        ageText = (TextView) rootView.findViewById(R.id.ageText);


        if (Characteristic.isMale()) {
            genderText.setText(getString(R.string.male));
            genderDefinition.setText(String.format(getString(R.string.gender_definition), getString(R.string.male).toLowerCase()));
        } else {
            genderText.setText(getString(R.string.female));
            genderDefinition.setText(String.format(getString(R.string.gender_definition), getString(R.string.female).toLowerCase()));
        }

        maleValueText.setText(Characteristic.getMale() + "%");
        femaleValueText.setText(Characteristic.getFemale() + "%");
        maleValueImage.getLayoutParams().width = Characteristic.getMale() * SCREEN_MULTIPLIER;
        femaleValueImage.getLayoutParams().width = Characteristic.getFemale() * SCREEN_MULTIPLIER;
        ageText.setText(String.valueOf(Characteristic.getAge()));

        return rootView;
    }

}
