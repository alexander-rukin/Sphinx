package com.airwhip.sphinx.anim;

import android.view.animation.TranslateAnimation;

/**
 * Created by Whiplash on 17.03.14.
 */
public class Move extends TranslateAnimation {

    public Move(int deltaX, int deltaY, boolean isFill) {
        super(0, deltaX, 0, deltaY);
        this.setFillEnabled(isFill);
        this.setFillAfter(isFill);
        this.setDuration(800);
    }

}
