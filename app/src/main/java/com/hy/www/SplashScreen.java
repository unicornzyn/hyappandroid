package com.hy.www;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.hy.www.hyapp.MESSAGE";
    public static Runnable runnable;
    public static Handler handler;

    private ImageView imageView;
    private ImageView[] imageViews;
    //包裹点点的LinearLayout
    private ViewGroup group;
    private ViewPager viewPager;
    private ArrayList<View> pageview;
    private boolean misScrolled;
    private static final String SHAREDPREFERENCES_NAME = "my_pref";
    private static final String KEY_GUIDE_ACTIVITY = "guide_activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        boolean isFirst=isFirstEnter(SplashScreen.this,SplashScreen.this.getClass().getName());
        if(isFirst) {
            viewPager=(ViewPager)findViewById(R.id.viewPager);
            //查找布局文件用LayoutInflater.inflate
            /*
            LayoutInflater inflater =getLayoutInflater();
            View view1 = inflater.inflate(R.layout.activity_view_pager1, null);
            View view2 = inflater.inflate(R.layout.activity_view_pager2, null);
            View view3 = inflater.inflate(R.layout.activity_view_pager3, null);
            View view4 = inflater.inflate(R.layout.activity_view_pager4, null);

            //将view装入数组
            pageview =new ArrayList<View>();
            pageview.add(view1);
            pageview.add(view2);
            pageview.add(view3);
            pageview.add(view4);
*/
/*
            group = (ViewGroup)findViewById(R.id.viewGroup);
            //有多少张图就有多少个点点
            imageViews = new ImageView[pageview.size()];
            for(int i =0;i<pageview.size();i++){
                imageView = new ImageView(SplashScreen.this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(20,20));
                imageView.setPadding(20, 0, 20, 0);
                imageViews[i] = imageView;
                //默认第一张图显示为选中状态
                if (i == 0) {
                    imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);
                } else {
                    imageViews[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
                }

                group.addView(imageViews[i]);
            }
            */
            //数据适配器
            PagerAdapter mPagerAdapter = new PagerAdapter(){

                @Override
                //获取当前窗体界面数
                public int getCount() {
                    // TODO Auto-generated method stub
                    return 3;//pageview.size();
                }

                @Override
                //断是否由对象生成界面
                public boolean isViewFromObject(View arg0, Object arg1) {
                    // TODO Auto-generated method stub
                    return arg0==arg1;
                }
                //从ViewGroup中移出当前View
                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    //container.removeView(pageview.get(position));
                    container.removeView((View)object);
                }

                //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    //container.addView(pageview.get(position));
                    //return pageview.get(position);

                    LayoutInflater inflater =getLayoutInflater();
                    int resId=0;
                    switch (position){
                        case 0:resId=R.layout.activity_view_pager1;break;
                        case 1:resId=R.layout.activity_view_pager2;break;
                        case 2:resId=R.layout.activity_view_pager3;break;
                        default:break;
                    }
                    View view = inflater.inflate(resId,null);
                    container.addView(view);
                    return view;

                }

            };

            //绑定适配器
            viewPager.setAdapter(mPagerAdapter);

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
                @Override
                public void onPageSelected(int position) {

                    /*
                    //如果切换了，就把当前的点点设置为选中背景，其他设置未选中背景
                    for(int i=0;i<imageViews.length;i++){
                        imageViews[position].setBackgroundResource(R.drawable.page_indicator_focused);
                        if (position != i) {
                            imageViews[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
                        }
                    }
                    */
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    switch (state) {
                        case ViewPager.SCROLL_STATE_DRAGGING:
                            misScrolled = false;
                            break;
                        case ViewPager.SCROLL_STATE_SETTLING:
                            misScrolled = true;
                            break;
                        case ViewPager.SCROLL_STATE_IDLE:
                            if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !misScrolled) {
                                SharedPreferences sharedPreferences=SplashScreen.this.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE);
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putString(KEY_GUIDE_ACTIVITY,"false");
                                editor.commit();

                                Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
                                mainIntent.putExtra(EXTRA_MESSAGE, "1");
                                SplashScreen.this.startActivity(mainIntent);
                                SplashScreen.this.finish();
                            }
                            misScrolled = true;
                            break;
                    }
                }
            });
        }else {
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.main);
            Resources resources = getBaseContext().getResources();
            relativeLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.splash));

            handler = new Handler();
            runnable = new Runnable() {
                public void run() {
                /* Create an Intent that will start the Main WordPress Activity. */
                    Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
                    mainIntent.putExtra(EXTRA_MESSAGE, "1");
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
                }
            };
            handler.postDelayed(runnable, 2000);
        }
    }

    public void goIndex(View view){
        handler.removeCallbacks(runnable);
        Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
        mainIntent.putExtra(EXTRA_MESSAGE,"1");
        this.startActivity(mainIntent);
        SplashScreen.this.finish();
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
    }
    public void goLogin(View view){
        handler.removeCallbacks(runnable);
        Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
        mainIntent.putExtra(EXTRA_MESSAGE,"0");
        SplashScreen.this.startActivity(mainIntent);
        SplashScreen.this.finish();
    }

    private boolean isFirstEnter(Context context, String className){
        if(context==null || className==null||"".equalsIgnoreCase(className))return false;
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE);
        String mResultStr =
                sharedPreferences.getString(KEY_GUIDE_ACTIVITY, "");//取得所有类名 如 com.my.MainActivity
        if(mResultStr.equalsIgnoreCase("false")) {
            return false;
        }
        else {
            return true;
        }
    }
}
