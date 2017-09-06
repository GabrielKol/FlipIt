package com.sublime.gaby.flipit;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Gaby on 2/18/2017.
 */

public class TileView extends FrameLayout {

    public ImageView pieceImage;
    private final int PADDING = 10;


    public TileView(Context context) {
        super(context);

        pieceImage = new ImageView(context);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        pieceImage.setLayoutParams(layoutParams);

        setPadding(PADDING, PADDING, PADDING, PADDING);
        setBackgroundResource(R.drawable.button_tile);

        addView(pieceImage);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Making the tiles square (setting its height to be equivalent to its width).
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }





}
