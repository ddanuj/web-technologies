package edu.usc.cs_server.stockinfo.util.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.usc.cs_server.stockinfo.R;
import edu.usc.cs_server.stockinfo.model.CurrentStockDataModel;

/**
 * Created by Anuj Doiphode on 20-11-2017.
 */

public class CurrentStockListAdapter extends ArrayAdapter<CurrentStockDataModel> {
    private ArrayList<CurrentStockDataModel> dataSet;
    Context context;

    private static class ViewHolder {
        TextView stockInfoKey;
        TextView stockInfoValue;
        ImageView changeImage;
    }

    public CurrentStockListAdapter(ArrayList<CurrentStockDataModel> data, Context c) {
        super(c, R.layout.fragment_current_stock_row, data);
        this.dataSet = data;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CurrentStockDataModel currentStockDataModel = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.fragment_current_stock_row, parent, false);
            viewHolder.stockInfoKey = convertView.findViewById(R.id.stock_info_key);
            viewHolder.stockInfoValue = convertView.findViewById(R.id.stock_info_value);
            viewHolder.changeImage = convertView.findViewById(R.id.change_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.stockInfoKey.setText(currentStockDataModel.getKeyString());
        viewHolder.stockInfoValue.setText(currentStockDataModel.getValueString());
        if(currentStockDataModel.getKeyString().equals(getContext().getString(R.string.stock_into_table_change))){
                viewHolder.stockInfoValue.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                if(Float.parseFloat(currentStockDataModel.getValueString().split(" ")[0]) > 0.0) {
                    viewHolder.changeImage.setBackground(getContext().getDrawable(R.drawable.up_icon));
                    viewHolder.changeImage.setVisibility(View.VISIBLE);
                }else if(Float.parseFloat(currentStockDataModel.getValueString().split(" ")[0]) < 0.0) {
                    viewHolder.changeImage.setBackground(getContext().getDrawable(R.drawable.down_icon));
                    viewHolder.changeImage.setVisibility(View.VISIBLE);
                }
        }
        return convertView;
    }
}
