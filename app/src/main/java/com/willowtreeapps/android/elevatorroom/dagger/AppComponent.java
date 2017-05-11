package com.willowtreeapps.android.elevatorroom.dagger;

import android.content.Context;

import com.willowtreeapps.android.elevatorroom.BaseGameView;
import com.willowtreeapps.android.elevatorroom.elevator.ElevatorActivity;
import com.willowtreeapps.android.elevatorroom.elevator.ElevatorViewModel;
import com.willowtreeapps.android.elevatorroom.intro.IntroActivity;
import com.willowtreeapps.android.elevatorroom.lobby.LobbyActivity;
import com.willowtreeapps.android.elevatorroom.lobby.LobbyViewModel;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Created by willowtree on 5/10/17.
 */
@Component(modules = GameModule.class)
@Singleton
public interface AppComponent {

    void inject(IntroActivity introActivity);
    void inject(ElevatorActivity elevatorActivity);
    void inject(LobbyActivity lobbyActivity);

    void inject(ElevatorViewModel elevatorViewModel);
    void inject(LobbyViewModel lobbyViewModel);

    void inject(BaseGameView baseGameView);

    void inject(PersonWidget personWidget);

    @Component.Builder
    interface Builder {

        AppComponent build();

        @BindsInstance
        Builder context(Context context);
    }

}
