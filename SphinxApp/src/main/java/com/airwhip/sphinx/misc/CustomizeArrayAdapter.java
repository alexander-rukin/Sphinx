package com.airwhip.sphinx.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.airwhip.sphinx.R;
import com.airwhip.sphinx.parser.Characteristic;

/**
 * Created by Whiplash on 30.03.14.
 */
public class CustomizeArrayAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] names;
    private Integer[] progress;
    private TextView sphinxStatistic;

    private int[] sorted;

    public CustomizeArrayAdapter(Context context, String[] names, Integer[] progress, TextView sphinxStatistic) {
        super(context, R.layout.listviewitem, names);
        this.context = context;
        this.names = names;
        this.progress = progress;
        this.sphinxStatistic = sphinxStatistic;

        sphinxStatistic.setText(names.length + "/" + names.length);

        sorted = new int[progress.length];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = i;
        }

        for (int i = 0; i < progress.length; i++) {
            for (int j = i + 1; j < progress.length; j++) {
                if (progress[sorted[i]] < progress[sorted[j]]) {
                    int tmp = sorted[i];
                    sorted[i] = sorted[j];
                    sorted[j] = tmp;
                }
            }
        }
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.listviewitem, null, true);

            holder = new ViewHolder();
            holder.progressBar = (ImageView) rowView.findViewById(R.id.progressBar);
            holder.progressBarTriangle = (ImageView) rowView.findViewById(R.id.progressBarTriangle);
            holder.progressText = (TextView) rowView.findViewById(R.id.progressText);
            holder.typeText = (TextView) rowView.findViewById(R.id.typeName);
            holder.isRightButton = (CheckBox) rowView.findViewById(R.id.isRightButton);
            holder.questionButton = (Button) rowView.findViewById(R.id.questionButton);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.typeText.setText(names[position]);
        if (progress[position] != -1) {
            holder.progressBar.getLayoutParams().width = 4 * progress[position];
            holder.progressText.setText(String.valueOf(progress[position] + "%"));
            holder.progressBar.setBackgroundColor(Constants.colors[position]);
            holder.progressBarTriangle.setBackgroundColor(Constants.colors[position]);
        } else {
            holder.progressText.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.progressBarTriangle.setVisibility(View.GONE);
        }
        holder.questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.questionButton.setVisibility(View.GONE);
                holder.isRightButton.setVisibility(View.VISIBLE);
            }
        });
        holder.isRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Characteristic.feedBackResult[position] == 1) {
                    Characteristic.feedBackResult[position] = 0;
                } else {
                    Characteristic.feedBackResult[position] = 1;
                }
                int count = names.length;
                for (int i : Characteristic.feedBackResult) {
                    if (i == 1) {
                        count--;
                    }
                }
                sphinxStatistic.setText(count + "/" + names.length);
            }
        });

        return rowView;
    }

    private class ViewHolder {
        public ImageView progressBar;
        public ImageView progressBarTriangle;
        public TextView progressText;
        public TextView typeText;
        public CheckBox isRightButton;
        public Button questionButton;
    }
}
