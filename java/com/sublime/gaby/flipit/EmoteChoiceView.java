package com.sublime.gaby.flipit;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Gaby on 2/25/2017.
 */

public class EmoteChoiceView extends TextView {

    public EmoteChoiceView(Context context){
        super(context);

        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        setGravity(Gravity.LEFT);
        setTextColor(Color.WHITE);
        setTextSize(16);
        setPadding(14,6,14,6);
        setBackgroundResource(R.drawable.button_gray_alpha);

    }

}
