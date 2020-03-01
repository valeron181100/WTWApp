package com.valeron.wtwapp.models;

import android.graphics.Bitmap;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

public class ImageCache extends LruCache<Integer, Bitmap> {
    private static ImageCache instance;

    private static int maxSize = 100 * 1024 * 1024;

    private ImageCache(int maximumSize) {
        super(maximumSize);
    }

    public static synchronized ImageCache getInstance() {
        if(instance == null)
            instance = new ImageCache(maxSize);
        return instance;
    }



    @Override
    protected int sizeOf(@NonNull Integer key, @NonNull Bitmap value) {
        return value.getByteCount();
    }
}
