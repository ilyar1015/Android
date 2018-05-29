package com.iflytek.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 *
 * Created by Administrator on 2017/7/9.
 */

public class HttpData extends AsyncTask<String, Void, String> {
    private HttpClient mHttpClient;
    private HttpGet mHttpGet;
    private HttpResponse mHttpResponse;
    private HttpEntity mHttpEntity;
    private InputStream in;
    private HttpGetDataListener listener;

    private String url;
    private String content_str=null;
    private  String keywords;

    public HttpData(String keywords, HttpGetDataListener listener) {



        try {
            this.keywords = URLDecoder.decode(keywords, "utf-8");
//            this.keywords=new String(keywords.getBytes("gbk"),"utf-8");
            this.url="http://202.201.60.10:8983/solr/FAQ/select?indent=on&q=q:"+keywords+"rows=1&wt=json";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        // this.keywords = keywords;
        //this.url="202.201.60.10:8983/solr/FAQ/select?fl=a,q&indent=on&q=q:"+keywords+"%20and%20a:"+keywords+"&rows=1&wt=json";
        this.listener=listener;
        content_str=null;
    }

    @Override
    protected String doInBackground(String... params ){
        content_str=null;
        try{
            mHttpClient = new DefaultHttpClient();
            mHttpGet = new HttpGet(url);
            mHttpResponse = mHttpClient.execute(mHttpGet);
            mHttpEntity = mHttpResponse.getEntity();
            in = mHttpEntity.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line=br.readLine())!=null){
                sb.append(line);
            }
            JSONObject jb = new JSONObject(sb.toString());
            //content_str=jb.getString("text");
            /*JSONObject response = new JSONObject(jb.getString("response").toString());
            String sdocs = response.getString("docs");
            sdocs = sdocs.substring(1,sdocs.length()-1);
            JSONObject docs = new JSONObject(sdocs);
            content_str=docs.getString("a");*/
            content_str= jb.getJSONObject("response").getJSONArray("docs").getJSONObject(0).getString("a");
            return sb.toString();
        }catch (Exception e){
            try {
                this.url = "http://www.tuling123.com/openapi/api?key=7b2b1285eeb944b68a1f5324ec92a6e0&info="+keywords;
                mHttpClient = new DefaultHttpClient();
                mHttpGet = new HttpGet(url);
                mHttpResponse = mHttpClient.execute(mHttpGet);
                mHttpEntity = mHttpResponse.getEntity();
                in = mHttpEntity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;
                StringBuffer sb = new StringBuffer();
                while ((line=br.readLine())!=null){
                    sb.append(line);
                }
                JSONObject jb = new JSONObject(sb.toString());
                content_str=jb.getString("text");
                return sb.toString();
            } catch (Exception e1) {
                content_str ="我是帅帅小易，我爱西北师大。";
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        listener.getDataUrl(s);
        super.onPostExecute(s);
    }

    public String getContent_str() {
        return content_str;
    }
}