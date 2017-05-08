package com.willowtreeapps.android.elevatorroom;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.view.ViewGroup;

import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by willowtree on 5/6/17.
 */

public abstract class BaseGameView {

    protected final Context context;
    protected final GameStateManager gameStateManager;
    public final MutableLiveData<Boolean> doorsOpen = new DistinctLiveData<>();

    @BindView(R.id.persons_container) ViewGroup personsContainer;

    public BaseGameView(Activity activity) {
        context = activity;
        gameStateManager = MyApplication.getGameStateManager();
        ButterKnife.bind(this, activity);
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

    protected abstract PersonWidget createNewPersonWidget();

}
