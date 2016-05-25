package com.baofeng.aone.volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.utils.Utils;
import com.google.gson.Gson;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class ResourceManager extends AndroidManager {

    private static final String TAG = ResourceManager.class.getSimpleName();

    public static ResourceManager mRequestManager;

    public static AndroidManager getAndroidManager() {
        return getRequestManager();
    }

    public static synchronized ResourceManager getRequestManager() {
        if (mRequestManager == null) {
            mRequestManager = new ResourceManager();
        }
        return mRequestManager;
    }

    public void imageloaderRequestVolley(final String url,
            final ResourceCallback mResourceCallback) {

        LauncherApplication.getInstance().getImageLoader()
                .get(url, new ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mResourceCallback.onResultFail(url, error.getMessage());
                    }

                    @Override
                    public void onResponse(ImageContainer response,
                            boolean isImmediate) {
                        Bitmap mBitmap = response.getBitmap();
                        if (mBitmap != null) {
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                    output);
                            byte[] result = output.toByteArray();
                            Gson gson = new Gson();
                            mResourceCallback.onImageBytes(url, new ReadData(result));
                        } else {
                            mResourceCallback.onImageBytes(url, null);
                        }
                    }
                });
    }

    public void stringRequestVolley(final String url,
            final ResourceCallback mResourceCallback) {
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            mResourceCallback.onStringResult(url, response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mResourceCallback.onResultFail(url, error.getMessage());
                    }
                });

        LauncherApplication.getInstance().addToRequestQueue(stringRequest);
    }

    public void imageRequestVolley(final String url,
            final ResourceCallback mResourceCallback) {
        String path = Utils.getBitmapWithMD5(LauncherApplication.getInstance(),
                url);
        if (path != null) {
            mResourceCallback.onImagePath(url, path);

        } else {
            ImageRequest imageRequest = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            String path = Utils.savedBitmapWithMD5(
                                    LauncherApplication.getInstance(), url,
                                    response);
                            mResourceCallback.onImagePath(url, path);
                        }
                    }, 0, 0, Config.ARGB_8888, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mResourceCallback.onResultFail(url,
                                    error.getMessage());
                        }
                    });
            LauncherApplication.getInstance().addToRequestQueue(imageRequest);
        }

    }

    // POST
    public void stringRequestVolley(final String url,
            HashMap<String, String> params,
            final ResourceCallback mResourceCallback) {

        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(
                params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mResourceCallback.onJsonResult(url, response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mResourceCallback.onResultFail(url, error.getMessage());
            }
        });

        // add the request object to the queue to be executed
        LauncherApplication.getInstance().addToRequestQueue(req);
    }

    // GET
    /*
     * public void jsonRequestVolley(final String url, final ResourceCallback
     * mResourceCallback) { // pass second argument as "null" for GET requests
     * JsonObjectRequest req = new JsonObjectRequest(url, null, new
     * Response.Listener<JSONObject>() {
     * 
     * @Override public void onResponse(JSONObject response) {
     * mResourceCallback.onJsonResult(url, response.toString()); } }, new
     * Response.ErrorListener() {
     * 
     * @Override public void onErrorResponse(VolleyError error) {
     * mResourceCallback.onResultFail(url, error.getMessage()); } }); // add the
     * request object to the queue to be executed
     * LauncherApplication.getInstance().addToRequestQueue(req); }
     */
}
