package com.gst.fmradio;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gst.fmradio.bean.Contans;
import com.gst.fmradio.model.Channel;
import com.gst.fmradio.service.FMService;
import com.gst.fmradio.utils.Blur;
import com.gst.fmradio.utils.DBHelper;
import com.gst.fmradio.utils.FixedSpeedScroller;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    //请求码
    final static int REQUSET = 1;
    //边界值
    private final static int MINCHANNELNUM = 875;
    private final static int MAXCHANNELNUM = 1080;
    ProgressDialog m_pDialog;
    List<Channel> list = new ArrayList<Channel>();
    int index = 0;
    int collectStatus, channelnum;
    int backgroundcurrent = 0;
    private DBHelper dbHelper = new DBHelper(this);
    //定义两个文本框
    private TextView symbol, mChannelnum;
    //定义五个ImageButton
    private ImageButton original, previous, next, latter, search;
    private ImageView collectedStatus, progress;
    //
    private int fmId, fmCollectStatus, fmChannelnum;
    private String fmName;
    private int currentFq = 0;
    private int needleStatus = 0;
    private FrameLayout mFrameLayout;
    private ViewPager mPlayView;
    private PageFragment mFragmentPage;
    private FragmentPagerAdapter mAdapter;
    private Animation animatonCollectStatus;
    //唱针
    private ImageView mNeedle;
    private FixedSpeedScroller mScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findId();
        list = queryValue();
        initView();

    }

    private void findId() {
        //根据id获取到相应的控件
        mFrameLayout = (FrameLayout) findViewById(R.id.layout_fm_view);
        symbol = (TextView) findViewById(R.id.symbol);
        mChannelnum = (TextView) findViewById(R.id.FMChannelnum);
        original = (ImageButton) findViewById(R.id.imageButton);
        previous = (ImageButton) findViewById(R.id.imageButton2);
        next = (ImageButton) findViewById(R.id.imageButton3);
        latter = (ImageButton) findViewById(R.id.imageButton4);
        collectedStatus = (ImageView) findViewById(R.id.collect);
        progress = (ImageView) findViewById(R.id.progress);
        search = (ImageButton) findViewById(R.id.action_search);

        mPlayView = (ViewPager) findViewById(R.id.layout_media_play_view);
        mNeedle = (ImageView) findViewById(R.id.needle);

        original.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        latter.setOnClickListener(this);
        collectedStatus.setOnClickListener(this);
        search.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        int op = Contans.MEDIA_BASE;
        animatonCollectStatus = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_collectstatus);
        Intent intent = new Intent("com.gst.fmradio.service.FMService");
        intent.setClass(this, FMService.class);
        switch (v.getId()) {
            case R.id.imageButton:
                //按钮设置点击监听事件：前一个有效的FM
                op = Contans.MEDIA_ORIGINAL;
                backgroundcurrent = backgroundcurrent - 1;
                if (backgroundcurrent < 0) {
                    backgroundcurrent = 4;
                }
                mPlayView.setCurrentItem(backgroundcurrent);
                setOriginal();
                break;
            case R.id.imageButton2:
                //按钮设置点击监听事件：FM减少0.1MHz
                op = Contans.MEDIA_PREVIOUS;
                progress.clearAnimation();
                backgroundcurrent = backgroundcurrent - 1;
                if (backgroundcurrent < 0) {
                    backgroundcurrent = 4;
                }
                mPlayView.setCurrentItem(backgroundcurrent);
                setPrevious();
                break;
            case R.id.imageButton3:
                //按钮设置点击监听事件：FM增加0.1MHz
                op = Contans.MEDIA_NEXT;
                progress.clearAnimation();
                backgroundcurrent = backgroundcurrent + 1;
                if (backgroundcurrent > 4) {
                    backgroundcurrent = 0;
                }
                mPlayView.setCurrentItem(backgroundcurrent);
                setNext();
                break;
            case R.id.imageButton4:
                //按钮设置点击监听事件：后一个有效的FM
                op = Contans.MEDIA_LATTER;
                backgroundcurrent = backgroundcurrent + 1;
                if (backgroundcurrent > 4) {
                    backgroundcurrent = 0;
                }
                mPlayView.setCurrentItem(backgroundcurrent);
                setLater();
                break;
            case R.id.collect:
                setStatus();
                collectedStatus.startAnimation(animatonCollectStatus);
                break;
            case R.id.action_search:
                data();
                search();
            default:
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("op", op);
        intent.putExtras(bundle);
        intent.setClass(this, FMService.class);
        startService(intent);
    }


    //在onResume里初始化和接受数据
    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent("com.gst.fmradio.service.FMService");
        intent.setClass(this, FMService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("op", Contans.MEDIA_PREVIOUS);
        intent.putExtras(bundle);
        startService(intent);
        list = queryValue();
        setAnimatNeedle();

        Bitmap b = Blur.drawableToBitmap(getResources().getDrawable(MyAdapter.TITLES[backgroundcurrent]));
        if (b == null)
            return;
        Bitmap bm = Blur.apply(this, b);
        Drawable drawable = new BitmapDrawable(bm);
        mFrameLayout.setBackgroundDrawable(drawable);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPlayView.setAdapter(mAdapter);
        mPlayView.setOnPageChangeListener(new MyPageChangeListener());

    }

    //初始化界面
    public void initView() {
        if (list.size() == 0) {
            mChannelnum.setText("87.5");
        } else {
            Cursor cursor = dbHelper.getReadableDatabase().query(
                    "channels",
                    new String[]{"channelnum,collectStatus"},
                    "id == 1",
                    null,
                    null,
                    null,
                    null,
                    null
            );
            if (cursor.moveToNext()) {
                currentFq = cursor.getInt(cursor.getColumnIndex("channelnum"));
                mChannelnum.setText(String.valueOf(Float.valueOf(currentFq) / 10.0f));
                collectStatus = cursor.getInt(cursor.getColumnIndex("collectStatus"));
                if (3 == collectStatus) {
                    collectedStatus.setImageResource(R.drawable.ic_action_not_start);
                } else {
                    collectedStatus.setImageResource(R.drawable.ic_action_start);
                }
            }
            for (int i = 0; i < list.size(); i++) {
                if (currentFq == list.get(i).getChannelnum()) {
                    index = i;
                    break;
                }
            }
        }
    }

    //查询数据库，将每一行的数据封装成一个Channel 对象，然后将对象添加到List中
    private List<Channel> queryValue() {

        //调用query()获取Cursor
        Cursor c = dbHelper.getReadableDatabase().query(
                "channels",
                new String[]{"id,name,channelnum,collectStatus"},
                "id > 4",
                null,
                null,
                null,
                "channelnum  asc",
                null
        );
        //拿到每一行的id ，name,channelunm,collectStatus的值
        while (c.moveToNext()) {
            fmId = c.getInt(c.getColumnIndex("id"));
            fmName = c.getString(c.getColumnIndex("name"));
            fmChannelnum = c.getInt(c.getColumnIndex("channelnum"));
            fmCollectStatus = c.getInt(c.getColumnIndex("collectStatus"));
            Channel chValue = new Channel();
            chValue.setId(fmId);
            chValue.setName(fmName);
            chValue.setChannelnum(fmChannelnum);
            chValue.setCol(fmCollectStatus);
            list.add(chValue);

        }

        return list;

    }

    public void setOriginal() {
        int mIndex = 0, idx = 0;
        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), "没有频道，请先搜索！", Toast.LENGTH_SHORT).show();
        } else {
            if (currentFq <= list.get(0).getChannelnum() || currentFq > list.get(list.size() - 1).getChannelnum()) {
                mIndex = list.size() - 1;
            } else if (currentFq == list.get(list.size() - 1).getChannelnum()) {
                mIndex = list.size() - 2;
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (currentFq >= list.get(i).getChannelnum() && currentFq < list.get(i + 1).getChannelnum()) {
                        idx = i;
                        break;
                    }
                }
                if (currentFq == list.get(idx).getChannelnum()) {
                    mIndex = idx - 1;

                } else {
                    if (currentFq > list.get(idx).getChannelnum() && currentFq <= list.get(idx + 1).getChannelnum()) {
                        mIndex = idx;
                    }
                }
            }
            currentFq = list.get(mIndex).getChannelnum();
            mChannelnum.setText(String.valueOf(Float.valueOf(currentFq / 10.0f)));
            if (3 == list.get(mIndex).getCol()) {
                collectedStatus.setImageResource(R.drawable.ic_action_not_start);
            } else {
                collectedStatus.setImageResource(R.drawable.ic_action_start);
            }
        }
    }

    public void setPrevious() {
        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), "没有频道，请先搜索！", Toast.LENGTH_SHORT).show();
        } else {
            if (currentFq == MINCHANNELNUM) {
                currentFq = MAXCHANNELNUM;
            } else {
                currentFq = currentFq - 1;
            }
            mChannelnum.setText(String.valueOf(Float.valueOf(currentFq / 10.0f)));
            Cursor c = dbHelper.getReadableDatabase().query(
                    "channels",
                    new String[]{"collectStatus"},
                    "channelnum==?",
                    new String[]{String.valueOf(currentFq)},
                    null,
                    null,
                    null,
                    null
            );

            if (c.moveToNext()) {
                collectStatus = c.getInt(c.getColumnIndex("collectStatus"));
                if (3 == collectStatus) {
                    collectedStatus.setImageResource(R.drawable.ic_action_not_start);
                } else {
                    collectedStatus.setImageResource(R.drawable.ic_action_start);
                }
            } else {
                collectedStatus.setImageResource(R.drawable.ic_action_not_start);
            }
            c.close();


        }
    }

    public void setNext() {
        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), "没有频道，请先搜索！", Toast.LENGTH_SHORT).show();
        } else {
            if (currentFq == MAXCHANNELNUM) {
                currentFq = MINCHANNELNUM;
            } else {
                currentFq = currentFq + 1;
            }
            mChannelnum.setText(String.valueOf(Float.valueOf(currentFq / 10.0f)));
            Cursor c = dbHelper.getReadableDatabase().query(
                    "channels",
                    new String[]{"collectStatus"},
                    "channelnum==?",
                    new String[]{String.valueOf(currentFq)},
                    null,
                    null,
                    null,
                    null
            );

            if (c.moveToNext()) {
                collectStatus = c.getInt(c.getColumnIndex("collectStatus"));
                if (3 == collectStatus) {
                    collectedStatus.setImageResource(R.drawable.ic_action_not_start);
                } else {
                    collectedStatus.setImageResource(R.drawable.ic_action_start);
                }
            } else {
                collectedStatus.setImageResource(R.drawable.ic_action_not_start);
            }
            c.close();
        }
    }

    public void setLater() {
        int mIndex = 0, idx = 0;
        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), "没有频道，请先搜索！", Toast.LENGTH_SHORT).show();
        } else {
            if (currentFq < list.get(0).getChannelnum() || currentFq >= list.get(list.size() - 1).getChannelnum()) {
                mIndex = 0;
            } else if (currentFq == list.get(0).getChannelnum()) {
                mIndex = 1;
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (currentFq >= list.get(i).getChannelnum() && currentFq < list.get(i + 1).getChannelnum()) {
                        idx = i;
                        break;
                    }
                }
                if (currentFq == list.get(idx).getChannelnum()) {
                    mIndex = idx + 1;

                } else {
                    if (currentFq >= list.get(idx).getChannelnum() && currentFq < list.get(idx + 1).getChannelnum()) {
                        mIndex = idx + 1;
                    }
                }
            }
            currentFq = list.get(mIndex).getChannelnum();
            mChannelnum.setText(String.valueOf(Float.valueOf(currentFq / 10.0f)));
            if (3 == list.get(mIndex).getCol()) {
                collectedStatus.setImageResource(R.drawable.ic_action_not_start);
            } else {
                collectedStatus.setImageResource(R.drawable.ic_action_start);
            }
        }
    }

    public void setStatus() {
        ContentValues contentValues = new ContentValues();
        Cursor c = dbHelper.getReadableDatabase().query(
                "channels",
                new String[]{"collectStatus"},
                "channelnum==?",
                new String[]{String.valueOf(currentFq)},
                null,
                null,
                null,
                null);
        if (c.moveToNext()) {
            collectStatus = c.getInt(c.getColumnIndex("collectStatus"));
            if (3 == collectStatus) {
                collectedStatus.setImageResource(R.drawable.ic_action_start);
                Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                contentValues.put("collectStatus", 2);
                dbHelper.getWritableDatabase().update(
                        "channels",
                        contentValues,
                        "channelnum==?",
                        new String[]{String.valueOf(currentFq)}
                );

            } else {
                collectedStatus.setImageResource(R.drawable.ic_action_not_start);
                Toast.makeText(getApplicationContext(), "取消收藏", Toast.LENGTH_SHORT).show();
                contentValues.put("collectStatus", 3);
                dbHelper.getWritableDatabase().update(
                        "channels",
                        contentValues,
                        "channelnum==?",
                        new String[]{String.valueOf(currentFq)}
                );

            }
            c.close();
        } else {
            collectedStatus.setImageResource(R.drawable.ic_action_start);
            contentValues.put("name", "新频道");
            contentValues.put("channelnum", currentFq);
            contentValues.put("collectStatus", 2);
            dbHelper.insert(contentValues);

        }
        list.clear();
        list = queryValue();
        for (int i = 0; i < list.size(); i++) {
            if (currentFq == list.get(i).getChannelnum()) {
                index = i;
                break;
            }
        }
    }

    //背景切换动画
    public void setBackgroudBitmap(int backgroundcurrent) {

        Bitmap b = Blur.drawableToBitmap(getResources().getDrawable(MyAdapter.TITLES[backgroundcurrent]));
        Bitmap bm = Blur.apply(MainActivity.this, b);
        ImageView imageView = (ImageView) findViewById(R.id.background_ground_floor);
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha);
        imageView.startAnimation(animation);
        Drawable drawable = new BitmapDrawable(bm);
        imageView.setBackground(drawable);

    }

    //指针动画
    public void setAnimatNeedle() {
        Animation anim;
        if (needleStatus == 0) {
            anim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        } else {
            anim = AnimationUtils.loadAnimation(this, R.anim.rotate_end);
        }
        LinearInterpolator lir = new LinearInterpolator();
        anim.setInterpolator(lir);
        anim.setDuration(650);
        mNeedle.startAnimation(anim);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                data();
                search();
                break;

            case R.id.action_list:
                channellist();

                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void search() {
        m_pDialog = new ProgressDialog(this);
        m_pDialog.setMessage("Search Channel");
        m_pDialog.setIndeterminate(false);
        m_pDialog.setCancelable(true);
        m_pDialog.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                dialog.cancel();
            }
        });
        m_pDialog.show();
    }

    private void channellist() {
        setTitle("Channel list");
        Intent intent = new Intent(this, ListActivity.class);
        startActivityForResult(intent, REQUSET);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                Bundle bundle = data.getExtras();
                int channelnumValue = bundle.getInt("channelnum");
                int collectStatusValue = bundle.getInt("collectStatus");
                currentFq = channelnumValue;
                collectStatus = collectStatusValue;
                mChannelnum.setText(String.valueOf(Float.valueOf(currentFq) / 10.0f));
                if (3 == collectStatus) {
                    collectedStatus.setImageResource(R.drawable.ic_action_not_start);
                } else {
                    collectedStatus.setImageResource(R.drawable.ic_action_start);
                }
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }
    }

    public void data() {
        dbHelper.getWritableDatabase().delete("channels", "collectStatus=3 and id>4", null);
        Random random = new Random();
        ContentValues values = new ContentValues();
        for (int a = 0; a < 6; a++) {
            int channelnumRadom = random.nextInt(1080) % (1080 - 875 + 1) + 875;
            values.put("name", "新频道");
            values.put("channelnum", channelnumRadom);
            values.put("collectStatus", 3);
            Cursor c = dbHelper.getWritableDatabase().query(
                    "channels",
                    new String[]{"channelnum"},
                    "channelnum =? and collectStatus == 2",
                    new String[]{String.valueOf(channelnumRadom)},
                    null,
                    null,
                    null,
                    null
            );
            if (c.moveToNext()) {

            } else {
                dbHelper.insert(values);
            }
        }
        list.clear();
        list = queryValue();
        currentFq = list.get(0).getChannelnum();
        mChannelnum.setText(String.valueOf(Float.valueOf(currentFq / 10.0f)));
        collectStatus = list.get(0).getCol();
        if (3 == collectStatus) {
            collectedStatus.setImageResource(R.drawable.ic_action_not_start);
        } else {
            collectedStatus.setImageResource(R.drawable.ic_action_start);
        }


    }

    public void leaveUpdate() {
        Cursor c = dbHelper.getReadableDatabase().query(
                "channels",
                new String[]{"collectStatus"},
                "channelnum == ?",
                new String[]{String.valueOf(currentFq)},
                null,
                null,
                null,
                null
        );
        if (c.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("channelnum", currentFq);
            collectStatus = c.getInt(c.getColumnIndex("collectStatus"));
            contentValues.put("collectStatus", collectStatus);
            dbHelper.getWritableDatabase().update(
                    "channels",
                    contentValues,
                    "id == 1",
                    null
            );
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            leaveUpdate();
            stopService(new Intent(this, FMService.class));
            finish();
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        public void onPageScrollStateChanged(int arg0) {
            PageFragment mPageFragment = new PageFragment();
            if (arg0 == 0) {
                needleStatus = 0;
                setAnimatNeedle();
            } else if (arg0 == 1) {
                needleStatus = 1;
                setAnimatNeedle();
                Bitmap b = Blur.drawableToBitmap(getResources().getDrawable(MyAdapter.TITLES[mPlayView.getCurrentItem()]));
                Bitmap bm = Blur.apply(MainActivity.this, b);
                Drawable drawable = new BitmapDrawable(bm);
                mFrameLayout.setBackgroundDrawable(drawable);
                try {
                    Field mField = ViewPager.class.getDeclaredField("mScroller");
                    mField.setAccessible(true);
                    mScroller = new FixedSpeedScroller(mPlayView.getContext(), new AccelerateInterpolator());
                    mField.set(mPlayView, mScroller);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (arg0 == 2) {

            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            int op = Contans.MEDIA_BASE;
            Bitmap b = Blur.drawableToBitmap(getResources().getDrawable(MyAdapter.TITLES[position]));
            Bitmap bm = Blur.apply(MainActivity.this, b);
            ImageView imageView = (ImageView) findViewById(R.id.background_ground_floor);
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha);
            imageView.startAnimation(animation);
            Drawable drawable = new BitmapDrawable(bm);
            imageView.setBackground(drawable);
            if (position > backgroundcurrent) {
                setLater();
                op = Contans.MEDIA_LATTER;
            } else if (position < backgroundcurrent) {
                setOriginal();
                op = Contans.MEDIA_ORIGINAL;
            }
            backgroundcurrent = position;
            Intent intent = new Intent("com.gst.fmradio.service.FMService");
            Bundle bundle = new Bundle();
            bundle.putInt("op", op);
            intent.putExtras(bundle);
            intent.setClass(MainActivity.this, FMService.class);
            startService(intent);

        }
    }

}