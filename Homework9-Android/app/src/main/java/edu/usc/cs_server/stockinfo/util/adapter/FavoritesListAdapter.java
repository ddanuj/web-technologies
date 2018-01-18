package edu.usc.cs_server.stockinfo.util.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import edu.usc.cs_server.stockinfo.R;
import edu.usc.cs_server.stockinfo.model.FavoriteListDataModel;

import static edu.usc.cs_server.stockinfo.MainActivity.FAVORITE_LIST;

/**
 * Created by Anuj Doiphode on 24-11-2017.
 */

public class FavoritesListAdapter extends ArrayAdapter<FavoriteListDataModel> {
    Context context;

    private static class ViewHolder {
        TextView favoritesListTicker;
        TextView favoritesListPrice;
        TextView favoritesListChange;
    }

    public FavoritesListAdapter(Context c){
        super(c, R.layout.favorites_list_view_row, FAVORITE_LIST);
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FavoriteListDataModel favoriteListDataModel = getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.favorites_list_view_row, parent, false);
            viewHolder.favoritesListTicker = convertView.findViewById(R.id.favorites_list_ticker);
            viewHolder.favoritesListPrice = convertView.findViewById(R.id.favorites_list_price);
            viewHolder.favoritesListChange = convertView.findViewById(R.id.favorites_list_change);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.favoritesListTicker.setText(favoriteListDataModel.getTicker());
        viewHolder.favoritesListPrice.setText(favoriteListDataModel.getPrice());
        viewHolder.favoritesListChange.setText(favoriteListDataModel.getChange()
                + " (" + favoriteListDataModel.getChangePercent() + "%)");
        if (Float.parseFloat(favoriteListDataModel.getChange()) > 0.0){
            viewHolder.favoritesListChange.setTextColor(Color.parseColor("#00ff00"));
        }else if(Float.parseFloat(favoriteListDataModel.getChange()) < 0.0){
            viewHolder.favoritesListChange.setTextColor(Color.parseColor("#ff0000"));
        }
        convertView.setBackgroundColor(getContext().getResources().getColor(R.color.favoriteListItemBgColor));
        return convertView;
    }
}
