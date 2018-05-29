package com.iflytek.voicedemo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ContactManager.ContactListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.http.HttpGetDataListener;
import  com.iflytek.http.HttpData;

public class IatDemo extends Activity implements HttpGetDataListener,OnClickListener {
	private static String TAG = IatDemo.class.getSimpleName();
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mIatDialog;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

	private EditText mResultText;
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	//对话框部分
	private List<ListData> lists = new ArrayList<ListData>();;
	private TextAdapter adapter;
	private ListView listview;
	private InputMethodManager input;
	private Button btn_right;
	private Button btn_left;

	//语音合成部分
	// 语音合成对象
	private SpeechSynthesizer mTts;
	// 默认发音人
	private String voicer = "xiaoyan";
	private String[] mCloudVoicersEntries;
	private String[] mCloudVoicersValue ;
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;

	// 引擎类型
	//private String mEngineType = SpeechConstant.TYPE_CLOUD;
	//private Toast mToast;
	private SharedPreferences mSharedPreferences2;

	//对话字符串
	String msg ="";
	String temp=null;

	private HttpData httpData;

	//欢迎语数组
	private String[] welcome_array;

	//时间变量
	private double currentTime;
	private double oldTime= 0;



	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);


		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(IatDemo.this, mInitListener);
		
		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		mIatDialog = new RecognizerDialog(IatDemo.this, mInitListener);
		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
				Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		//对话框部分
		mResultText = ((EditText) findViewById(R.id.sendText));
		mResultText.setOnClickListener(this);





		input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		//语音合成部分
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(IatDemo.this, mTtsInitListener);

		// 云端发音人名称列表
		mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
		mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

		mSharedPreferences2 = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);

		initLayout();
	}
    //停止语音
	public void voidDemo(View v){
		mTts.stopSpeaking();
	}
	/**
	 * 初始化Layout。
	 */
	private void initLayout() {

		listview = (ListView) findViewById(R.id.lv);
		btn_right = (Button) findViewById(R.id.send_btn);
		btn_right.setOnClickListener(this);



		btn_left = (Button) findViewById(R.id.send_speak);
		btn_left.setOnClickListener(this);
		adapter = new TextAdapter(lists,this);
		listview.setAdapter(adapter);
		while(temp==null){
			temp = getRondomWelcomeTips();
		}
		ListData listData;
		listData = new ListData(temp,ListData.RECEIVER,getTime());
		lists.add(listData);
		TTS(temp);
	}


	private String getRondomWelcomeTips(){
		String welcome_tip=null;
		welcome_array=this.getResources().getStringArray(R.array.welcome_tips);
		int index = (int)(Math.random()*(welcome_array.length-1));
		welcome_tip = welcome_array[index];
		return welcome_tip;
	}

	int ret = 0; // 函数调用返回值

	@Override
	public void onClick(View view) {
		if( null == mIat ){
			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
			this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
			return;
		}

		getTime();
		msg = mResultText.getText().toString().trim();
		switch (view.getId()) {

		// 进入参数设置页面
		// 开始听写
		// 如何判断一次听写结束：OnResult isLast=true 或者 onError
			case R.id.lv:
				mTts.pauseSpeaking();
				mTts.stopSpeaking();
				break;
		case R.id.send_speak:
			STT();
			//CHAT();
			mResultText.setText("");
			break;
		case R.id.send_btn:
			if(mResultText.getText().toString().replace(" ","").equals("")){
				break;
			}
			CHAT();
			//adapter.addDataToAdapter(new  MsgInfo(msg,null));
			//adapter.notifyDataSetChanged();
			mResultText.setText("");
			break;

		}
		listview.smoothScrollToPosition(listview.getCount() - 1);
//		mResultText.setText("");
	}
	//对话框事件调用函数
//
	private void CHAT()
	{
		String temp_str=null;
		msg = mResultText.getText().toString().trim();
		String dropk = msg.replace(" ","");
		String droph = dropk.replace("\n","");

		if(lists.size()>30){
			for(int i=0;i<lists.size();i++)
				lists.remove(i);
		}

		ListData listData;
		listData = new ListData(msg,ListData.SEND,getTime());
		lists.add(listData);
		adapter.notifyDataSetChanged();
		  /*if(msg.indexOf("西北师范大学")!=-1 || msg.indexOf("西北师大")!=-1){
			if(msg.indexOf("哪里")!=-1){
				msg="西北师范大学位于甘肃省兰州市，是甘肃省人民政府和教育部共建的重点大学。前身为国立北平师范大学，发端于1902年建立的京师大学堂师范馆，1912年改为“国立北京高等师范学校”，1923年改为“国立北平师范大学”。学校校本部占地面积960亩，新校区占地面积729亩、定点绿化用地1300亩，各类教学科研仪器设备总值25013.14万元，各类图书文献资料370.9万余册（盘）；设27个二级学院，1个独立学院，3个孔子学院，开办76个本科专业；有教职工2187人，有各类学生37969人，其中普通本科生19031人，博士研究生338人，硕士研究生6976人，留学生324人，继续教育本专科生11300人。";
				listData = new ListData(msg,ListData.RECEIVER,getTime());
				lists.add(listData);
				adapter.notifyDataSetChanged();
				TTS(msg);
			}else if(msg.indexOf("专业")!=-1){
				msg="西北师范大学专业：哲学、经济学、经济统计学、金融学、国际经济与贸易\n" +
						"法学、社会工作、思想政治教育、教育学、教育技术、学前教育、特殊教育、体育教育、运动训练、汉语言文学、英语、俄语、阿拉伯语、日语、翻译、新闻学、历史学、数学与应用数学、信息与计算科学、物理学、化学、地理科学、人文地理与城乡规划、生物科学、生物技术、应用心理学、材料科学与工程、材料物理、电子信息工程、计算机科学与技术、物联网工程、化学工程与工艺、制药工程、环境科学与工程类、信息管理与信息系统 、工商管理、会计学、人力资源管理、行政管理、劳动与社会保障、旅游管理、酒店管理、音乐表演、音乐学、舞蹈表演、舞蹈学、广播电视编导、播音与主持艺术、动画、美术学类、美术学、绘画、视觉传达设计、环境设计、数字媒体艺术。";
				listData = new ListData(msg,ListData.RECEIVER,getTime());
				lists.add(listData);
				adapter.notifyDataSetChanged();
				TTS(msg);
			}else{

				//httpData = (HttpData) new HttpData("http://www.tuling123.com/openapi/api?key=7b2b1285eeb944b68a1f5324ec92a6e0&info="+droph,this).execute();
				//httpData = (HttpData) new HttpData("http://172.21.249.2:8983/solr/Test/select?fl=id_mysql,answer_mysql&indent=on&q=quest_mysql:"+droph+" and answer_mysql:"+droph+"&rows=1&wt=json",this).execute();
				httpData = (HttpData) new HttpData("http://172.21.249.2:8983/solr/Test/select?indent=on&q=quest_mysql:西北师范大学&wt=json",this).execute();

				while(httpData.getContent_str() == null){}
				temp_str =httpData.getContent_str();
//				listData = new ListData(msg,ListData.RECEIVER,getTime());
//				lists.add(listData);
				adapter.notifyDataSetChanged();
				TTS(temp_str);
			}

		} else
		{

			//httpData = (HttpData) new HttpData("http://www.tuling123.com/openapi/api?key=7b2b1285eeb944b68a1f5324ec92a6e0&info="+droph,this).execute();
			httpData = (HttpData) new HttpData("http://localhost:8983/solr/Test/select?fl=id_mysql,answer_mysql&indent=on&q=quest_mysql:"+droph+" and answer_mysql:"+droph+"&rows=1&wt=json",this).execute();

			while(httpData.getContent_str() == null){}
			temp_str =httpData.getContent_str();
//			listData = new ListData(msg,ListData.RECEIVER,getTime());
//			lists.add(listData);
			adapter.notifyDataSetChanged();
			TTS(temp_str);
		}
		//et_meg.setFocusable(false);*/
		//httpData = (HttpData) new HttpData("http://www.tuling123.com/openapi/api?key=7b2b1285eeb944b68a1f5324ec92a6e0&info="+droph,this).execute();
		//httpData = (HttpData) new HttpData("http://localhost:8983/solr/Test/select?fl=id_mysql,answer_mysql&indent=on&q=quest_mysql:"+droph+" and answer_mysql:"+droph+"&rows=1&wt=json",this).execute();
		httpData = (HttpData) new HttpData(droph,this).execute();

		while(httpData.getContent_str() == null){}
		temp_str =httpData.getContent_str();
		listData = new ListData(temp_str,ListData.RECEIVER,getTime());
		lists.add(listData);
		adapter.notifyDataSetChanged();
		TTS(temp_str);
		if (input.isActive()) {
			input.hideSoftInputFromWindow(mResultText.getWindowToken(), 0);
		}else {
//			listData = new ListData(msg,ListData.RECEIVER,getTime());
//			lists.add(listData);
//			adapter.notifyDataSetChanged();
		}

	}

	//语音识别事件调用函数
	private void STT()
	{
		// 移动数据分析，收集开始听写事件
		FlowerCollector.onEvent(IatDemo.this, "iat_recognize");

		mResultText.setText(null);// 清空显示内容
		mIatResults.clear();
		// 设置参数
		setParam();
		boolean isShowDialog = mSharedPreferences.getBoolean(
				getString(R.string.pref_key_iat_show), true);
		if (isShowDialog) {
			// 显示听写对话框
			mIatDialog.setListener(mRecognizerDialogListener);
			mIatDialog.show();
			showTip(getString(R.string.text_begin));
		} else {
			// 不显示听写对话框
			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
				showTip("听写失败,错误码：" + ret);
			} else {
				showTip(getString(R.string.text_begin));
			}
		}
	}


	//语音合成事件调用函数
	private void TTS(String text){
		// 移动数据分析，收集开始合成事件
		FlowerCollector.onEvent(IatDemo.this, "tts_play");
		//String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
		// 设置参数
		setParam2();
		int code = mTts.startSpeaking(text, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
				//未安装则跳转到提示安装页面
				//mInstaller.install();
			}else {
				showTip("语音合成失败,错误码: " + code);
			}
		}
		//adapter.notifyDataSetChanged();
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};

	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码："+code);
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}
		}
	};



	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			showTip("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			showTip("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());
			printResult(results);

			if (isLast) {
				// TODO 最后的结果
			}

		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

	};

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}
		//此处写文件操作，去掉了后面的句号
		mResultText.setText(resultBuffer.toString().substring(0,resultBuffer.toString().length()-1));
		mResultText.setSelection(mResultText.length());

	}

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			printResult(results);
			/*msg = mResultText.getText().toString().trim();
			CHAT();*/

		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));

		}

	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			//showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			//showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			//showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
									 String info) {
			// 合成进度
			mPercentForBuffering = percent;
			/*showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));*/
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
			/*showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));*/
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				//showTip("播放完成");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * 参数设置
	 * 
	 * @param
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = mSharedPreferences.getString("iat_language_preference",
				"mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
			// 设置语言		} else {

			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}


	private void setParam2(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
			//设置合成语速
			mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences2.getString("speed_preference", "50"));
			//设置合成音调
			mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences2.getString("pitch_preference", "50"));
			//设置合成音量
			mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences2.getString("volume_preference", "50"));
		}else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");
			/**
			 * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
			 * 开发者如需自定义参数，请参考在线合成参数设置
			 */
		}
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences2.getString("stream_preference", "3"));
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if( null != mIat ){
			// 退出时释放连接
			mIat.cancel();
			mIat.destroy();
		}
		if( null != mTts ){
			mTts.stopSpeaking();
			// 退出时释放连接
			mTts.destroy();
		}
	}

	@Override
	protected void onResume() {
		// 开放统计 移动数据统计分析
		FlowerCollector.onResume(IatDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 开放统计 移动数据统计分析
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(IatDemo.this);
		super.onPause();
	}

	@Override
	public void getDataUrl(String data) {
		//parseText(data);
	}

	public void parseText(String str){


		try{
			JSONObject jb = new JSONObject(str);
//            System.out.println(jb.getString("code"));
//            System.out.println(jb.getString("text"));
			ListData listData;
			listData = new ListData(jb.getString("text"),ListData.RECEIVER,getTime());
			lists.add(listData);
			adapter.notifyDataSetChanged();
		}catch (JSONException e){
			e.printStackTrace();
		}
	}

	private String getTime(){
		currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date();
		String str = format.format(curDate);
		if(currentTime - oldTime >= 5*60*1000){
			oldTime = currentTime;
			return str;
		}else{
			return "";
		}
	}
}
