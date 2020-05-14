package com.ahagari.howmanyminutesleft;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyListArrayAdapter extends ArrayAdapter<CustomListData> {

    private int mResource;
    private List<CustomListData> mItems;
    private LayoutInflater mInflater;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソースID
     * @param items リストビューの要素
     */
    public MyListArrayAdapter(Context context, int resource, List<CustomListData> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = mInflater.inflate(mResource, null);
        }

        // リストビューに表示する要素を取得
        CustomListData item = mItems.get(position);


        // 表示用のリストに値を設定
        TextView textIndex = view.findViewById(R.id.listTextIndex);
        textIndex.setText(item.getListTextIndex());
        textIndex.setTextColor(Color.RED);

        TextView countTime = view.findViewById(R.id.listTextCountTime);
        countTime.setText(item.getListTextCountTime());
        countTime.setTextColor(Color.WHITE);

        TextView trainType = view.findViewById(R.id.listTextTrainType);
        trainType.setText(item.getListTextTrainType());
        trainType.setTextColor(Color.MAGENTA);

        TextView trainTime = view.findViewById(R.id.listTextTime);
        trainTime.setText(item.getListTextTime());
        trainTime.setTextColor(Color.WHITE);

        TextView dest = view.findViewById(R.id.listTextDest);
        dest.setText(item.getListTextDest());
        dest.setTextColor(Color.WHITE);

        return view;
    }
}