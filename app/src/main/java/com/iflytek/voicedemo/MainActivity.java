package com.iflytek.voicedemo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.institute.Institute;
import com.notice.WebNoticePage;
import com.school.SchoolNotice;
import com.know.ActiveKnow;

public class   MainActivity extends Activity {
	private Intent intent;
	private int i=0;
	private ImageView img;
	private boolean flag=true;
	final int bannerBar[]={R.drawable.banner1,R.drawable.banner2,R.drawable.banner3,
						R.drawable.banner4,R.drawable.banner5};
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		img=findViewById(R.id.logo_banner);
		banner();
	}

	public void banner(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while(flag){
//					System.out.println("Thread go");
//					i++;
//					img.setImageResource(bannerBar[i]);
//					if(i==5) i=0;
//					try {
//						Thread.sleep(1000);
//					}catch (Exception e){
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
	}

	public void onClick_Home(View v){
		switch (v.getId()){
			case R.id.talk_robot:
				intent=new Intent(MainActivity.this,IatDemo.class);
				startActivity(intent);
				break;
			case R.id.about_school:
				intent=new Intent(MainActivity.this,SchoolNotice.class);
				startActivity(intent);
				break;
			case R.id.about_institute:
				intent=new Intent(MainActivity.this,Institute.class);
				startActivity(intent);
				break;
			case R.id.school_notice:
				intent=new Intent(MainActivity.this,WebNoticePage.class);
				startActivity(intent);
				break;
			case R.id.about_me:
				intent=new Intent(MainActivity.this,ActiveKnow.class);
				startActivity(intent);
				break;
			case R.id.logo_banner:
				img.setImageResource(bannerBar[i]);
				i++;
				if(i==5) i=0;
				break;
		}
	}
}
