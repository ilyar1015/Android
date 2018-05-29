package com.school;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.iflytek.voicedemo.R;

/**
 *
 * Created by ilyar on 2017/9/10.
 */

public class SchoolNotice extends Activity {
    private TextView text;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.school_notice);

        text=findViewById(R.id.school_text_notice);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
}
