package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

//https://zoom.us/s/5467158583
//For this class I references Sunshine's widget classes and did my best to make it work for
//my own project

public class QuoteWidgetService extends RemoteViewsService {
    public final String LOG_TAG = QuoteWidgetService.class.getSimpleName();
    private static final String[] QUOTE_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };
    // these indices must match the projection
    static final int INDEX_QUOTE_ID = 0;
    static final int INDEX_QUOTE_SYMBOL = 1;
    static final int INDEX_QUOTE_BIDPRICE = 2;
    static final int INDEX_QUOTE_PERCENT_CHANGE = 3;
    static final int INDEX_QUOTE_CHANGE = 4;
    static final int INDEX_QUOTE_ISUP = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        QUOTE_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                // get the data for this position from the content provider
                String symbol = "";
                String bid = "";
                String change = "";

                if (data.moveToPosition(position)) {
                    symbol = data.getString(
                            data.getColumnIndex(QuoteColumns.SYMBOL));
                    bid = data.getString(
                            data.getColumnIndex(QuoteColumns.BIDPRICE));
                    change = data.getString(
                            data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.quote_widget_item);
                views.setInt(R.id.quote_widget, "setBackgroundResource", R.color.graph_background);
                views.setTextViewText(R.id.widget_stock_symbol, symbol);
                views.setTextColor(R.id.widget_stock_symbol, getResources().getColor(R.color.white));
                views.setTextViewText(R.id.widget_bid_price, bid);

                if (data.getInt(data.getColumnIndex("is_up")) == 1) {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                views.setTextViewText(R.id.widget_change, change);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTE_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}