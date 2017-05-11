package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.view.ViewGroup;

import com.willowtreeapps.android.elevatorroom.dagger.AppComponent;
import com.willowtreeapps.android.elevatorroom.livedata.DistinctLiveData;
import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by willowtree on 5/6/17.
 */

public abstract class BaseGameView implements LifecycleObserver {

    @Inject protected GameStateManager gameStateManager;
    protected final AppComponent appComponent;
    protected final Context context;
    private final Unbinder unbinder;
    public final MutableLiveData<Boolean> doorsOpen = new DistinctLiveData<>();

    @BindView(R.id.persons_container) ViewGroup personsContainer;

    public BaseGameView(AppComponent appComponent, LifecycleActivity activity) {
        this.appComponent = appComponent;
        appComponent.inject(this);
        context = activity;
        unbinder = ButterKnife.bind(this, activity);
        activity.getLifecycle().addObserver(this);
    }

    public void updateForPeople(List<Person> people) {
        int childCount = personsContainer.getChildCount();
        for (int childIndex = childCount - 1; childIndex >= 0; childIndex--) {
            PersonWidget widget = (PersonWidget) personsContainer.getChildAt(childIndex);
            boolean found = false;
            for (Person person : people) {
                if (widget.setPerson(person)) {
                    people.remove(person); // person has been accounted for
                    found = true;
                    break;
                }
            }
            if (!found) {
                // didn't match an active person, so remove it from playfield
                personsContainer.removeView(widget);
            }
        }
        // add persons that haven't been accounted for
        for (Person person : people) {
            PersonWidget widget = createNewPersonWidget();
            personsContainer.addView(widget);
            widget.setPerson(person);
        }
        updateWidgets(null);
    }

    public void updateWidgets(Object o) {
        for (int i = 0; i < personsContainer.getChildCount(); i++) {
            PersonWidget widget = (PersonWidget) personsContainer.getChildAt(i);
            widget.update();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    protected abstract PersonWidget createNewPersonWidget();

}
