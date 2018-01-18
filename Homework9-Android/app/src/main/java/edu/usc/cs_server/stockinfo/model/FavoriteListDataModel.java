package edu.usc.cs_server.stockinfo.model;

import java.util.Comparator;

/**
 * Created by Anuj Doiphode on 24-11-2017.
 */

public class FavoriteListDataModel {
    private String ticker;
    private String price;
    private String change;
    private String changePercent;

    public FavoriteListDataModel(String ticker, String price, String change, String changePercent) {
        this.ticker = ticker;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
    }

    public String getTicker() {
        return ticker;
    }

    public String getPrice() {
        return price;
    }

    public String getChange() {
        return change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public static Comparator<FavoriteListDataModel> tickerComparator = new Comparator<FavoriteListDataModel>() {
        @Override
        public int compare(FavoriteListDataModel t1, FavoriteListDataModel t2) {
            String t1Ticker = t1.getTicker();
            String t2Ticker = t2.getTicker();

            return t1Ticker.compareTo(t2Ticker);
        }
    };

    public static Comparator<FavoriteListDataModel> priceComparator = new Comparator<FavoriteListDataModel>() {
        @Override
        public int compare(FavoriteListDataModel t1, FavoriteListDataModel t2) {
            Float diff = Float.parseFloat(t1.getPrice()) - Float.parseFloat(t2.getPrice());
            if (diff > 0.0) {
                return 1;
            } else if (diff < 0.0) {
                return -1;
            }
            return 0;
        }
    };
    public static Comparator<FavoriteListDataModel> changeComparator = new Comparator<FavoriteListDataModel>() {
        @Override
        public int compare(FavoriteListDataModel t1, FavoriteListDataModel t2) {
            Float diff = Float.parseFloat(t1.getChange()) - Float.parseFloat(t2.getChange());
            if (diff > 0.0) {
                return 1;
            } else if (diff < 0.0) {
                return -1;
            }
            return 0;
        }
    };
    public static Comparator<FavoriteListDataModel> changePercentComparator = new Comparator<FavoriteListDataModel>() {
        @Override
        public int compare(FavoriteListDataModel t1, FavoriteListDataModel t2) {
            Float diff = Float.parseFloat(t1.getChangePercent()) - Float.parseFloat(t2.getChangePercent());
            if (diff > 0.0) {
                return 1;
            } else if (diff < 0.0) {
                return -1;
            }
            return 0;
        }
    };
}
