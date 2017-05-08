package com.willowtreeapps.android.elevatorroom;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by willowtree on 5/8/17.
 */

public class RxUtil {

    private RxUtil() {
    }

    /**
     * perform an action on a background thread
     */
    public static void runInBg(Action action) {
        Observable.defer(() -> {
            action.run();
            return Observable.empty();
        }).subscribeOn(Schedulers.io()).subscribe();
    }

}
