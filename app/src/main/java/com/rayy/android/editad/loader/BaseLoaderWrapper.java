package com.rayy.android.editad.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * wrapper loader for more fin-grained control
 *
 * @Author yangw
 * @Date 31/8/15 2:20 PM.
 */
public class BaseLoaderWrapper extends CursorLoader {
    private Context context;

    public BaseLoaderWrapper(Context context) {
        super(context);

        this.context = context;
    }

    /**
     * Creates a fully-specified CursorLoader.
     *
     * @param context
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     */
    public BaseLoaderWrapper(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);

        this.context = context;
    }

    @Override
    public Cursor loadInBackground() {
        try {
            return super.loadInBackground();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }
}
