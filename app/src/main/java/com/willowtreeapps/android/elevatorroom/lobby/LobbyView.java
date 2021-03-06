package com.willowtreeapps.android.elevatorroom.lobby;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.MutableLiveData;

import com.willowtreeapps.android.elevatorroom.BaseGameView;
import com.willowtreeapps.android.elevatorroom.dagger.AppComponent;
import com.willowtreeapps.android.elevatorroom.livedata.DistinctLiveData;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

/**
 * Created by willowtree on 5/6/17.
 */

public class LobbyView extends BaseGameView {

    public final MutableLiveData<Integer> floorToRender = new DistinctLiveData<>();

    public LobbyView(AppComponent appComponent, LifecycleActivity activity) {
        super(appComponent, activity);
    }

    @Override
    protected PersonWidget createNewPersonWidget() {
        return new PersonWidget(context).init(appComponent, true,
                gameStateManager.multiWindowDividerSize, floorToRender, doorsOpen);
    }

}
