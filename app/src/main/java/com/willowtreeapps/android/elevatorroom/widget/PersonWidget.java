package com.willowtreeapps.android.elevatorroom.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.willowtreeapps.android.elevatorroom.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by willowtree on 5/3/17.
 * <p>
 * a custom view for a person that walks around and hits elevator buttons
 */

public class PersonWidget extends FrameLayout {

    @BindView(R.id.background) View background;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    public PersonWidget(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PersonWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PersonWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        inflate(getContext(), R.layout.widget_person, this);
        ButterKnife.bind(this, this);
    }

}
