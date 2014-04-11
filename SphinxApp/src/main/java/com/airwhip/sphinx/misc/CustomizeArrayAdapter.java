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

    private static final int ENABLED_POSITIONS = Constants.xmls.length;

    private Context context;
    private String[] names;
    private Integer[] progress;

    private int[] sorted;

    public CustomizeArrayAdapter(Context context, String[] names, Integer[] progress) {
        super(context, R.layout.listviewitem, names);
        this.context = context;
        this.names = names;
        this.progress = progress;

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
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.listviewitem, null, true);

            holder = new ViewHolder();
            holder.icon = (ImageView) rowView.findViewById(R.id.icon);
            holder.progressBar = (ImageView) rowView.findViewById(R.id.progressBar);
            holder.progressBarTriangle = (ImageView) rowView.findViewById(R.id.progressBarTriangle);
            holder.progressText = (TextView) rowView.findViewById(R.id.progressText);
            holder.typeText = (TextView) rowView.findViewById(R.id.typeName);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        int id = findTypeIndex(names[position]);

        holder.progressText.setText(String.valueOf(progress[position] + "%"));
        holder.typeText.setText(names[position]);
        holder.progressBar.getLayoutParams().width = 3 * progress[position];
        holder.progressBar.setBackgroundColor(Constants.colors[id]);
        holder.progressBarTriangle.setBackgroundColor(Constants.colors[id]);
        holder.icon.setImageResource(Constants.icos[id]);
        holder.icon.setBackgroundColor(Constants.colors[id]);

        if (!isEnabled(position)) {
            rowView.setAlpha(0.5f);
        }

        return rowView;
    }

    private int findTypeIndex(String typeName) {
        int id = 0;
        for (String type : context.getResources().getStringArray(R.array.types)) {
            if (type.equals(typeName)) {
                break;
            }
            id++;
        }
        return id;
    }

    @Override
    public boolean isEnabled(int position) {
        for (int i = 0; i < sorted.length && i < ENABLED_POSITIONS; i++) {
            if (sorted[i] == position) {
                return true;
            }
        }
        return false;
    }

    private class ViewHolder {
        public ImageView icon;
        public ImageView progressBar;
        public ImageView progressBarTriangle;
        public TextView progressText;
        public TextView typeText;
    }
}
