package com.cosroulette.android.utils;

import android.app.Activity;
import android.view.View;

/**
 * Class responsible for changing the view from full screen to non-full screen and vice versa.
 *
 * @author Pierfrancesco Soffritti
 */
public class FullScreenHelper {

    private Activity context;
    private View[] views;
    private Boolean isInFullScreen = false;

    /**
     * @param context
     * @param views to hide/show
     */
    public FullScreenHelper(Activity context, View ... views) {
        this.context = context;
        this.views = views;
    }

    /**
     * call this method to enter full screen
     */
    public void enterFullScreen() {

        isInFullScreen = true;

        View decorView = context.getWindow().getDecorView();

        hideSystemUi(decorView);

        for(View view : views) {
            view.setVisibility(View.GONE);
            view.invalidate();
        }
    }

    /**
     * call this method to exit full screen
     */
    public void exitFullScreen() {

        isInFullScreen = false;

        View decorView = context.getWindow().getDecorView();

        showSystemUi(decorView);

        for(View view : views) {
            view.setVisibility(View.VISIBLE);
            view.invalidate();
        }
    }

    private void hideSystemUi(View mDecorView) {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showSystemUi(View mDecorView) {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    public Boolean getIsInFullScreen() {
        return isInFullScreen;
    }
}