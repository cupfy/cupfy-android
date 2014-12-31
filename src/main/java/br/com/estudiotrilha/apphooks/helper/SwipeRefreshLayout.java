package br.com.estudiotrilha.apphooks.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by mauricio on 12/30/14.
 */
public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {
    private ListView listView;

    public SwipeRefreshLayout(Context context) {
        super(context);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public boolean canChildScrollUp() {
        int pos = 0;

        try {
            pos = listView.getChildAt(0).getTop();
        } catch(Exception e) {}

        return pos < 0;
    }
}