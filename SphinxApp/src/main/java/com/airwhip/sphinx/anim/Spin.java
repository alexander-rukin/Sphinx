package com.airwhip.sphinx.anim;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

/**
 * Created by Whiplash on 27.03.14.
 */
public class Spin extends RotateAnimation {

    public Spin() {
        super(0, 360, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        setDuration(1500);
        setRepeatCount(INFINITE);
    }

}
