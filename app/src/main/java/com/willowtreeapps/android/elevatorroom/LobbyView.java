package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.MutableLiveData;

import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

/**
 * Created by willowtree on 5/6/17.
 */

public class LobbyView extends BaseGameView {

    public final MutableLiveData<Integer> floorToRender = new DistinctLiveData<>();

    public LobbyView(LifecycleActivity activity) {
        super(activity);
    }

    @Override
    protected PersonWidget createNewPersonWidget() {
        return new PersonWidget(context).init(true,
                gameStateManager.multiWindowDividerSize, floorToRender, doorsOpen);
    }

}
