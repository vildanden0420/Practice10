package com.example.vil.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.icu.text.UnicodeSetSpanner;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    EditText et;
    WebView webView;
    ProgressDialog dialog;
    Animation animTop;
    LinearLayout linear;
    ListView listView;
    ArrayList<Data> data;
    ArrayAdapter<String> adapter;
    ArrayList<String> dataName;
    Boolean sameSite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        et = (EditText)findViewById(R.id.url);
        webView = (WebView)findViewById(R.id.webview);
        dialog = new ProgressDialog(this);
        linear = (LinearLayout)findViewById(R.id.linear);
        listView = (ListView)findViewById(R.id.list_item);
        data = new ArrayList<Data>();
        dataName = new ArrayList<>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataName);
        listView.setAdapter(adapter);


        et.setInputType(0);

        et.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                // TODO Auto-generated method stub

                et.setInputType(1);

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                mgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

            }

        });

        webView.addJavascriptInterface(new JavaScriptMethods(), "MyApp");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.setMessage("Loading...");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                et.setText(url);
            }
        });

        webView.loadUrl("https://www.naver.com");


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress >= 100)
                    dialog.dismiss();
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return super.onJsAlert(view, url, message, result);
            }
        });

        animTop = AnimationUtils.loadAnimation(this, R.anim.translate_top);
        animTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linear.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        linear.setAnimation(animTop);


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(Main2Activity.this);
                dlg.setTitle("즐겨찾기 삭제")
                        .setMessage("삭제하겠습니까?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                data.remove(position);
                                dataName.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("Cancel", null)
                        .show();


                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("http://"+data.get(position).url);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"즐겨찾기 추가");
        menu.add(0,2,0,"즐겨찾기 목록");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            webView.loadUrl("file:///android_asset/www/urladd.html");
            linear.setAnimation(animTop);
            animTop.start();
            listView.setVisibility(View.INVISIBLE);
            linear.setVisibility(View.INVISIBLE);
            webView.setVisibility(View.VISIBLE);
        }else {
            webView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    Handler myhandler = new Handler();
    class JavaScriptMethods {

        @JavascriptInterface
        public void showUrl(){
            myhandler.post(new Runnable() {
                @Override
                public void run() {
                    linear.setVisibility(View.VISIBLE);
                }
            });
        }

        @JavascriptInterface
        public void saveUrl(final String name, final String urlAddr){
            myhandler.post(new Runnable() {
                @Override
                public void run() {
                    sameSite = false;
                    for(int i = 0; i<data.size(); i++){
                        if(data.get(i).url.equals(urlAddr)){
                            sameSite=true;
                        }
                    }

                    if(sameSite){
                        webView.loadUrl("javascript:displayMsg()");
                    }else {
                        data.add(new Data(name, urlAddr));
                        dataName.add("<"+name+"> "+urlAddr);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "추가되었습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    public void onClick(View v){
        webView.loadUrl("http://"+et.getText().toString());
    }

}
