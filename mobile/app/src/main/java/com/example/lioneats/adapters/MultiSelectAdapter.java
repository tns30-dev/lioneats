package com.example.lioneats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.lioneats.R;

import java.util.List;

public class MultiSelectAdapter extends ArrayAdapter<String> {
    private final List<String> items;
    private final List<String> selectedItems;

    public MultiSelectAdapter(Context context, List<String> items, List<String> selectedItems) {
        super(context, 0, items);
        this.items = items;
        this.selectedItems = selectedItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_multiselect, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textViewItem);
        CheckBox checkBox = convertView.findViewById(R.id.checkBoxItem);

        String item = items.get(position);
        textView.setText(item);

        checkBox.setChecked(selectedItems.contains(item));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedItems.contains(item)) {
                    selectedItems.add(item);
                }
            } else {
                selectedItems.remove(item);
            }
        });

        return convertView;
    }
}