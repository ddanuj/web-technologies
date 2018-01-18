package edu.usc.cs_server.stockinfo.util.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.usc.cs_server.stockinfo.R;
import edu.usc.cs_server.stockinfo.model.StockNewsDataModel;

/**
 * Created by Anuj Doiphode on 19-11-2017.
 */

public class StockNewsListAdapter extends ArrayAdapter<StockNewsDataModel> implements View.OnClickListener {
    private ArrayList<StockNewsDataModel> dataSet;
    Context context;

    private static class ViewHolder {
        TextView stockNewsTitle;
        TextView stockNewsAuthor;
        TextView stockNewsPubDate;
    }

    public StockNewsListAdapter(ArrayList<StockNewsDataModel> data, Context c) {
        super(c, R.layout.fragment_stock_news_row, data);
        this.dataSet = data;
        this.context = c;
    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        StockNewsDataModel stockNewsDataModel = (StockNewsDataModel) getItem(position);
        //TODO Open model's link in new tab
        getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stockNewsDataModel.getLink())));
        //System.out.println(stockNewsDataModel.getTitle());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        StockNewsDataModel stockNewsDataModel = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            //Create new view
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.fragment_stock_news_row, parent, false);
            viewHolder.stockNewsTitle = convertView.findViewById(R.id.stock_news_title);
            viewHolder.stockNewsAuthor = convertView.findViewById(R.id.stock_news_author);
            viewHolder.stockNewsPubDate = convertView.findViewById(R.id.stock_news_pub_date);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.stockNewsTitle.setText(stockNewsDataModel.getTitle());
        viewHolder.stockNewsAuthor.setText("Author: " + stockNewsDataModel.getAuthorName());
        viewHolder.stockNewsPubDate.setText("Date: " + stockNewsDataModel.getPubDate());
        return convertView;
    }
}
