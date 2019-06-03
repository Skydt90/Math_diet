package com.skydt.matematiskvgttab.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.skydt.matematiskvgttab.R;
import com.skydt.matematiskvgttab.models.Diet;

import java.util.List;
import java.util.Locale;

public class DietAdapter extends ArrayAdapter
{
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<Diet> diets;

    public DietAdapter(Context context, int resource, List<Diet> diets)
    {
        super(context, resource);
        this.layoutInflater = LayoutInflater.from(context);
        this.layoutResource = resource;
        this.diets = diets;
    }

    @Override
    public int getCount()
    {
        return diets.size();
    }

    @Override
    public Object getItem(int position)
    {
        return diets.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Diet diet = diets.get(position);
        viewHolder.tvName.setText(diet.getDietName());
        viewHolder.tvStartWeight.setText(String.format(Locale.getDefault(), "%.1f", diet.getStartWeight()));
        viewHolder.tvStartWeight.append(" kg -> ");
        viewHolder.tvDesiredWeight.setText(String.format(Locale.getDefault(), "%.1f", diet.getDesiredWeight()));
        viewHolder.tvDesiredWeight.append(" kg");

        return convertView;
    }

    private class ViewHolder
    {
        final TextView tvName;
        final TextView tvStartWeight;
        final TextView tvDesiredWeight;

        ViewHolder(View v)
        {
            this.tvName = v.findViewById(R.id.tvName);
            this.tvStartWeight = v.findViewById(R.id.tvStartWeight);
            this.tvDesiredWeight = v.findViewById(R.id.tvDesiredWeight);
        }
    }
}
