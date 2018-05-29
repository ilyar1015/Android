package com.institute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Toast;


import com.iflytek.voicedemo.R;
import com.yalantis.euclid.library.EuclidActivity;
import com.yalantis.euclid.library.EuclidListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.iflytek.voicedemo.IatDemo;
/**
 * 
 * Created by ilyar on 2017/9/10.
 */

public class Institute  extends EuclidActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(Institute.this, "Oh hi!", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(Institute.this,IatDemo.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected BaseAdapter getAdapter() {
        Map<String, Object> profileMap;
        List<Map<String, Object>> profilesList = new ArrayList<>();

        int[] avatars = {
                R.drawable.wenxue,
                R.drawable.lishi,
                R.drawable.jiaoyu,
                R.drawable.xinli,
                R.drawable.makesi,
                R.drawable.shehui,
                R.drawable.faxue,
                R.drawable.jingji,
                R.drawable.shangxue,
                R.drawable.waiyu,
                R.drawable.yinyue,
                R.drawable.wudao,
                R.drawable.meishu,
                R.drawable.tiyu,
                R.drawable.shuxue,
                R.drawable.jisuanji,
                R.drawable.wuli,
                R.drawable.huaxue,
                R.drawable.shengming,
                R.drawable.dili,
                R.drawable.dunhuang,
                R.drawable.jiaoyujishu,
                R.drawable.chuanmei,
                R.drawable.lvyou,
                R.drawable.guoji,
                R.drawable.yanjiusheng
                };
        String[] names = getResources().getStringArray(R.array.array_names);
        String[] institute_name_short= getResources().getStringArray(R.array.lorem_ipsum_short);
        String[] institute_name_long= getResources().getStringArray(R.array.lorem_ipsum_long);
        for (int i = 0; i < avatars.length; i++) {
            profileMap = new HashMap<>();
            profileMap.put(EuclidListAdapter.KEY_AVATAR, avatars[i]);
            profileMap.put(EuclidListAdapter.KEY_NAME, names[i]);
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, institute_name_short[i]);
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL,institute_name_long[i]);
            profilesList.add(profileMap);
        }

        return new EuclidListAdapter(this, R.layout.list_item, profilesList);
    }
}
