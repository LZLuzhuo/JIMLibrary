/* Copyright 2020 Luzhuo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.luzhuo.lib_im.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageLoader {

    protected final Context context;

    private volatile static ImageLoader instance;
    private volatile static Pattern NUMBER_PATTERN = Pattern.compile("[0-9]*");

    public static ImageLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader(context);
                }
            }
        }
        return instance;
    }

    public ImageLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * @param uriStr
     * @param imageView
     * @throws IOException
     */
    public void displayImage(String uriStr, ImageView imageView) throws IOException {
        switch (Scheme.ofUri(uriStr)) {
            case FILE:
                displayImageFromFile(uriStr, imageView);
                return;
            case ASSETS:
                displayImageFromAssets(uriStr, imageView);
                return;
            case DRAWABLE:
                displayImageFromDrawable(uriStr, imageView);
                return;
            case HTTP:
            case HTTPS:
                displayImageFromNetwork(uriStr, imageView);
                return;
            case CONTENT:
                displayImageFromContent(uriStr, imageView);
                return;
            case UNKNOWN:
            default:
                Matcher m = NUMBER_PATTERN.matcher(uriStr);
                if (m.matches()) {
                    displayImageFromResource(Integer.parseInt(uriStr), imageView);
                    return;
                }
                displayImageFromOtherSource(uriStr, imageView);
                return;
        }
    }

    /**
     * From File
     *
     * @param imageUri
     * @param imageView
     * @throws IOException
     */
    protected void displayImageFromFile(String imageUri, ImageView imageView) throws IOException {
        String filePath = Scheme.FILE.crop(imageUri);
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * From Assets
     *
     * @param imageUri
     * @param imageView
     * @throws IOException
     */
    protected void displayImageFromAssets(String imageUri, ImageView imageView) throws IOException {
        String filePath = Scheme.ASSETS.crop(imageUri);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * From Drawable
     *
     * @param imageUri
     * @param imageView
     * @throws IOException
     */
    protected void displayImageFromDrawable(String imageUri, ImageView imageView) {
        String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
        int resID = context.getResources().getIdentifier(drawableIdString, "mipmap", context.getPackageName());
        if (resID <= 0) {
            resID = context.getResources().getIdentifier(drawableIdString, "drawable", context.getPackageName());
        }
        if (resID > 0 && imageView != null) {
            imageView.setImageResource(resID);
        }
    }

    /**
     * From Resource
     *
     * @param resID
     * @param imageView
     */
    protected void displayImageFromResource(int resID, ImageView imageView) {
        if (resID > 0 && imageView != null) {
            imageView.setImageResource(resID);
        }
    }

    /**
     * From Net
     *
     * @param imageUri
     * @param extra
     * @throws IOException
     */
    protected void displayImageFromNetwork(String imageUri, Object extra) throws IOException {
    }


    /**
     * From Content
     *
     * @param imageUri
     * @param imageView
     * @throws IOException
     */
    protected void displayImageFromContent(String imageUri, ImageView imageView) throws FileNotFoundException {
    }

    /**
     * From OtherSource
     *
     * @param imageUri
     * @param imageView
     * @throws IOException
     */
    protected void displayImageFromOtherSource(String imageUri, ImageView imageView) throws IOException {
    }

    public enum Scheme {
        HTTP("http"),
        HTTPS("https"),
        FILE("file"),
        CONTENT("content"),
        ASSETS("assets"),
        DRAWABLE("drawable"),
        UNKNOWN("");

        private String scheme;
        private String uriPrefix;

        Scheme(String scheme) {
            this.scheme = scheme;
            uriPrefix = scheme + "://";
        }

        public static Scheme ofUri(String uri) {
            if (uri != null) {
                for (Scheme s : values()) {
                    if (s.belongsTo(uri)) {
                        return s;
                    }
                }
            }
            return UNKNOWN;
        }

        public String toUri(String path){
            return uriPrefix + path;
        }

        public String crop(String uri) {
            if (!belongsTo(uri)) {
                throw new IllegalArgumentException(String.format("URI [%1$s] doesn't have expected scheme [%2$s]", uri, scheme));
            }
            return uri.substring(uriPrefix.length());
        }

        protected boolean belongsTo(String uri) {
            return uri.toLowerCase(Locale.US).startsWith(uriPrefix);
        }

        public static String cropScheme(String uri) throws IllegalArgumentException {
            return ofUri(uri).crop(uri);
        }
    }
}
