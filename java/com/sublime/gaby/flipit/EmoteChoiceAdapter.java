package com.sublime.gaby.flipit;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Gaby on 2/25/2017.
 */

public class EmoteChoiceAdapter extends BaseAdapter {


    private Context mContext;


    public EmoteChoiceAdapter(Context context) {

        mContext = context;

    }


    @Override
    public int getCount() {
        return mContext.getResources().getStringArray(R.array.emotes_array).length;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        EmoteChoiceView emoteChoiceView;
        emoteChoiceView = (EmoteChoiceView)convertView;

        if(emoteChoiceView == null) {
            emoteChoiceView = new EmoteChoiceView(mContext);
            Log.v("Emote Choice Adapter","creating new view for index " + position);
        } else {
            Log.e("Emote Choice Adapter","RECYCLING view for index "+ position);
        }

        emoteChoiceView.setText(getItem(position));

        return emoteChoiceView;

    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public String getItem(int position) {

        return mContext.getResources().getStringArray(R.array.emotes_array)[position];

    }





}
