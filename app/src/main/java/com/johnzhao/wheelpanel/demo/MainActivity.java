package com.johnzhao.wheelpanel.demo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.johnzhao.wheelpanel.widget.WheelPanel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WheelPanel wheelPanel = findViewById(R.id.wheelPanel);

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
        colors.add(Color.CYAN);
        wheelPanel.setItemBackgroundColors(colors);
        //wheelPanel.setItemContent(0, "我是第0个");
        List<String> contents = new ArrayList<>();
        for(int i=0; i<4; i++){
            contents.add("I'm NO."+i);
        }
        wheelPanel.setItemContents(contents);

    }
}
