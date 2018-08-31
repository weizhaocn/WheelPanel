# WheelPanel
This is a customized view in Android. It's like a wheel panel. You can finger it around.

![impression drawing](https://github.com/weizhaocn/WheelPanel/blob/master/images/screenshot_01.gif)

## How to use

### layout
```
<com.johnzhao.wheelpanel.widget.WheelPanel
        android:id="@+id/wheelPanel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:radius="100dp"
        app:itemCount="4"
        app:textColor="#000000"
        app:textSize="16sp"
        android:padding="10dp"
        android:background="#cccccc"/>
```

### Activity
```
WheelPanel wheelPanel = findViewById(R.id.wheelPanel);

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
        colors.add(Color.CYAN);
        wheelPanel.setItemBackgroundColors(colors);

        List<String> contents = new ArrayList<>();
        for(int i=0; i<4; i++){
            contents.add("I'm NO."+i);
        }
        wheelPanel.setItemContents(contents);
```

## LICENSE

Copyright 2018 Wei Zhao

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
