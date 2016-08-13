package com.example.cmc7.hktoilet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by cmc7 on 2/27/2016.
 */
public class ToiletArrayAdapter extends ArrayAdapter<Toilet>{
    private final Context context;
    //private Toilet[] toilets;
    private List<Toilet> toiletList;


    public ToiletArrayAdapter(Context context, List<Toilet> toiletList){
        super(context, -1, toiletList);
        this.context = context;
        this.toiletList = toiletList;

        //toilets = toiletList.toArray(new Toilet[toiletList.size()]);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_layout, parent, false);

        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        TextView thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);

        firstLine.setText(toiletList.get(position).name);
        secondLine.setText(toiletList.get(position).address);
        thirdLine.setText(context.getText(R.string.distance)+
                String.valueOf(toiletList.get(position).distance)+
                context.getText(R.string.distance_meter));
        return rowView;
    }

}


