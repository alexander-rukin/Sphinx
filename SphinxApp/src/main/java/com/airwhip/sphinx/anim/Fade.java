package com.airwhip.sphinx.anim;

import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by Whiplash on 16.03.14.
 */
public class Fade extends AlphaAnimation {

    public Fade(View view, float newAlpha) {
        super(Build.VERSION.SDK_INT <= 14 ? (newAlpha == 0 ? 1 : 0) : view.getAlpha(), newAlpha);
        this.setFillEnabled(true);
        this.setFillAfter(true);
        this.setDuration(500);
    }

}
