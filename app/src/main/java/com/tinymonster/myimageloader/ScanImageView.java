package com.tinymonster.myimageloader;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScanImageView {
    private int startPosition = 0;
    private int currentPosition = 0;
    private List<String> urls;
    private Dialog dialog;
    private MyPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private ImageView backImage;
    private TextView dialog_scan_page;
    private List<View> views;
    private Activity activity;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "ImageLoader#" + mCount.getAndIncrement());
        }
    };
    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>(), THREAD_FACTORY);

    public ScanImageView(Activity activity) {
        this.activity = activity;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        RelativeLayout relativeLayout = (RelativeLayout)activity.getLayoutInflater().inflate(R.layout.dialog_scan_pic,null);
        viewPager = relativeLayout.findViewById(R.id.dialog_scan_viewpager);
        backImage = relativeLayout.findViewById(R.id.dialog_scan_finish);
        dialog_scan_page = relativeLayout.findViewById(R.id.dialog_scan_page);
        dialog = new Dialog(activity, R.style.DialogFullScreen);
        dialog.setContentView(relativeLayout);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /**
         * 滑动监听，设置1/3
         */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                Log.e("ScanImageView", "onPageSelected:" + i);
                currentPosition = i;
                StringBuilder text = new StringBuilder();
                text.append(++i);
                text.append("/").append(views.size());
                dialog_scan_page.setText(text);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void setUrl(List<String> picUrls) {
        if (urls == null) {
            urls = new ArrayList<>();
        } else {
            urls.clear();
        }
        urls.addAll(picUrls);
    }

    public void create(int startPosition) throws ExecutionException, InterruptedException {
        this.startPosition = startPosition;
        this.currentPosition = startPosition;
        if (views == null) {
            views = new ArrayList<View>();
        } else {
            views.clear();
        }
        pagerAdapter = new MyPagerAdapter(views);
        for (final String url:urls) {
            FrameLayout frameLayout = (FrameLayout)activity.getLayoutInflater().inflate(R.layout.dialog_image_scale,null);
            final SubsamplingScaleImageView imageView = frameLayout.findViewById(R.id.dialog_image_scale_img);
            if (url.startsWith("http:") || url.startsWith("https:")) {
                THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //这个方法是同步方法
                            final File file = Glide.with(activity).load(url).downloadOnly(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL).get();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImage(ImageSource.uri((Uri.fromFile(file))));
                                }
                            });
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                File file = new File(url);
                imageView.setImage(ImageSource.uri((Uri.fromFile(file))));
            }
            views.add(frameLayout);
        }
        dialog_scan_page.setText((startPosition + 1) + "/" + views.size());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(startPosition);
        dialog.show();
    }

    /**
     * 自定义的PagerAdapter,管理view
     */
    private class MyPagerAdapter extends PagerAdapter {
        private List<View> views;

        MyPagerAdapter(List<View> views) {
            this.views = views;
        }
        /**
         * 1.将给定位置的view添加到viewPager中，创建并显示出来
         * 2.返回一个代表新增页面的key，通常是直接返回view本身就可以了（可以自定义key，但是key要和view一一对应）
         * @param container
         * @param position
         * @return
         */
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        /**
         * 移除指定view
         * @param container
         * @param position
         * @param object
         */
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View)object;
            container.removeView(view);
        }

        /**
         * 获得viewPager中有多少个view
         * @return
         */
        @Override
        public int getCount() {
            return views.size();
        }

        /**
         * 判断instantiateItem返回的key和view是否对应
         * @param view
         * @param o
         * @return
         */
        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }

    public void finish() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        activity = null;
        views.clear();
    }
}
