package com.willowtreeapps.android.elevatorroom;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by willowtree on 4/28/17.
 */

public class DisplayUtil {
    private DisplayUtil() {
    }

    /**
     * in portrait mode, returns true if the view is in the upper half of the multiwindow split, false if in the bottom half
     * <p>
     * in landscape mode, returns true if the view is in the left half of the multiwindow split, false if in the right half
     */
    public static boolean isMultiWindowPrimary(View view) {
        Rect outRect = new Rect();
        view.getWindowVisibleDisplayFrame(outRect);
        switch (MyApplication.getContext().getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                float screenHeight = outRect.bottom - outRect.top;
                float widthLeft = outRect.left;
                float aspectRatioLeft = widthLeft / screenHeight; // aspect ratio of the space to the left of this view
                return aspectRatioLeft < 0.2; // if aspect ratio is tiny, assume it must be the nav bar
            default: // assume portrait
                float screenWidth = outRect.right - outRect.left;
                float heightAbove = outRect.top;
                float aspectRatioAbove = heightAbove / screenWidth; // aspect ratio of the space above this view
                return aspectRatioAbove < 0.2; // if aspect ratio is tiny, assume it must be the status bar
        }
    }

}
