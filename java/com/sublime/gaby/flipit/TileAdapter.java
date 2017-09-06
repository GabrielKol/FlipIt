package com.sublime.gaby.flipit;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sublime.gaby.flipit.Logic.Board;
import com.sublime.gaby.flipit.Logic.GamePiece;
import com.sublime.gaby.flipit.Logic.Tile;

/**
 * Created by Gaby on 2/18/2017.
 */

public class TileAdapter extends BaseAdapter {

    private Board mBoard;
    private Context mContext;


    public TileAdapter(Context context, Board board) {

        mBoard = board;
        mContext = context;

    }


    @Override
    public int getCount() {
        return mBoard.getNumberOfTiles();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TileView tileView;
        tileView = (TileView)convertView;

        if(tileView == null) {
            tileView = new TileView(mContext);
            Log.v("Tile Adapter","creating new view for index ("+ position/mBoard.getNumberOfCols()+
                    ", "+ position%mBoard.getNumberOfCols()+ ")");
        } else {
            Log.e("Tile Adapter","RECYCLING view for index ("+ position/mBoard.getNumberOfCols()+
                    ", "+ position%mBoard.getNumberOfCols()+ ")");
        }





        // If
        if(getItem(position).getmOccupyingGamePiece() == null){
            tileView.pieceImage.setImageResource(R.drawable.piece_none);
        }
        else if (getItem(position).getmOccupyingGamePiece() == GamePiece.BLACK){
            if(!getItem(position).ismRecentlyFlipped()) {
                tileView.pieceImage.setImageResource(R.drawable.piece_black);
            }
            else{

                tileView.pieceImage.setImageDrawable(mContext.getDrawable(R.drawable.animation_white_to_black));
                AnimationDrawable animation = (AnimationDrawable)tileView.pieceImage.getDrawable();
                animation.start();

            }
        }
        else {
            if(!getItem(position).ismRecentlyFlipped()) {
                tileView.pieceImage.setImageResource(R.drawable.piece_white);
            }
            else{

                tileView.pieceImage.setImageDrawable(mContext.getDrawable(R.drawable.animation_black_to_white));
                AnimationDrawable animation = (AnimationDrawable)tileView.pieceImage.getDrawable();
                animation.start();

            }

        }



        return tileView;

    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public Tile getItem(int position) {
        return mBoard.getTile(position/mBoard.getNumberOfCols(), position%mBoard.getNumberOfCols());
    }


    // For tutorial purposes:
    public void setmBoard(Board mBoard) {
        this.mBoard = mBoard;
    }



}
