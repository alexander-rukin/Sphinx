package com.airwhip.sphinx.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airwhip.sphinx.R;

/**
 * Created by Whiplash on 30.03.14.
 */
public class CustomizeArrayAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] names;
    private Integer[] progress;
    private int[] buttonState;
    private TextView sphinxStatistic;

    private int[] sorted;

    public CustomizeArrayAdapter(Context context, String[] names, Integer[] progress, TextView sphinxStatistic) {
        super(context, R.layout.listviewitem, names);
        this.context = context;
        this.names = names;
        this.progress = progress;
        this.buttonState = new int[names.length];
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
            holder.isRightButton = (TextView) rowView.findViewById(R.id.isRightButton);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.progressText.setText(String.valueOf(progress[position] + "%"));
        holder.typeText.setText(names[position]);
        holder.progressBar.getLayoutParams().width = 3 * progress[position];
        holder.progressBar.setBackgroundColor(Constants.colors[position]);
        holder.progressBarTriangle.setBackgroundColor(Constants.colors[position]);
        holder.isRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonState[position] == 0) {
                    buttonState[position] = 1;
                    holder.isRightButton.setBackgroundColor(getContext().getResources().getColor(R.color.accept));
                    holder.isRightButton.setText(getContext().getString(R.string.yes));
                } else {
                    if (buttonState[position] == 1) {
                        buttonState[position] = 2;
                        holder.isRightButton.setBackgroundColor(getContext().getResources().getColor(R.color.reject));
                        holder.isRightButton.setText(getContext().getString(R.string.no));
                    } else {
                        buttonState[position] = 1;
                        holder.isRightButton.setBackgroundColor(getContext().getResources().getColor(R.color.accept));
                        holder.isRightButton.setText(getContext().getString(R.string.yes));
                    }
                }
                int count = names.length;
                for (int i : buttonState) {
                    if (i == 2) {
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
        public TextView isRightButton;
    }
}
