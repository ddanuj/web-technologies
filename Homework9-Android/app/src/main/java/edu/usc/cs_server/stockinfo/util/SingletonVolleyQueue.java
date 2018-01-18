package edu.usc.cs_server.stockinfo.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Anuj Doiphode on 16-11-2017.
 */

public class SingletonVolleyQueue {
    private static SingletonVolleyQueue singletonVolleyQueueInstance;
    private static RequestQueue requestQueue;
    private static Context context;

    private SingletonVolleyQueue(Context mContext) {
        context = mContext;
        requestQueue = getRequestQueue();
    }

    public static synchronized SingletonVolleyQueue getInstance(Context mContext) {
        if (singletonVolleyQueueInstance == null) {
            singletonVolleyQueueInstance = new SingletonVolleyQueue(mContext);
        }
        return singletonVolleyQueueInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
