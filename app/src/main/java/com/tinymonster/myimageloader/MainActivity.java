package com.tinymonster.myimageloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<String> urls = new ArrayList<String>();
        urls.add("https://t1.huanqiucdn.cn/9e58ebe2d0ab903e2f2ea56cd1c674f2.jpg");
        urls.add("https://t1.huanqiucdn.cn/99ca1d4ba858cbea7a6fe35204e20c52.jpg");
        urls.add("https://t1.huanqiucdn.cn/e3bd06a533de25786ecb4b7bfb85768c.jpg");
        ScanImageView scanImageView = new ScanImageView(this);
        scanImageView.setUrl(urls);
        try {
            scanImageView.create(0);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
