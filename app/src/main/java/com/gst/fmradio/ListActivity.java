package com.gst.fmradio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.gst.fmradio.model.Channel;
import com.gst.fmradio.utils.DBHelper;

/**
 * Created by yyshang on 5/21/15.
 */
public class ListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private int listPosition = 0;   //标识列表位置
    private ListView listView;
    private DbAdapter adapter;
    private int id,channelnum,collectStatus;
    private String  name;

    DBHelper dbHelper;
    List<Channel> channelList = new ArrayList<Channel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        // 获取listView
        listView = (ListView) findViewById(R.id.channel);
        channelList = queryData();
        adapter = new DbAdapter(this, channelList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    //查询数据库，将每一行的数据封装成一个Channel 对象，然后将对象添加到List中
    private List<Channel> queryData() {
        List<Channel> list = new ArrayList<Channel>();
        dbHelper = new DBHelper(this);
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
             id = c.getInt(c.getColumnIndex("id"));
            name = c.getString(c.getColumnIndex("name"));
            channelnum = c.getInt(c.getColumnIndex("channelnum"));
            collectStatus = c.getInt(c.getColumnIndex("collectStatus"));
            //用一个Channel对象来封装查询出来的数据
            Channel ch = new Channel();
            ch.setId(id);
            ch.setName(name);
            ch.setChannelnum(channelnum);
            ch.setCol(collectStatus);
            list.add(ch);
            Log.e("shang", "id: " + id + " /name: " + name + " /channelnum: " + channelnum + " /collectStatus: " + collectStatus);
        }
        c.close();
        return list;


    }


    public class DbAdapter extends BaseAdapter {
        private List<Channel> list;
        private Context context;
        private LayoutInflater layoutInflater;

        public DbAdapter(Context context, List<Channel> list) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.list = list;
        }

        //刷新适配器
        public void refresh(List<Channel> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        //获得指定位置要显示的View
        public View getView(int position, View convertView, ViewGroup parent) {


            Channel c = list.get(position);
            //定义一个内部静态类
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_item, null, false);
                holder.channelnum = (TextView) convertView.findViewById(R.id.textview2);
                holder.name = (TextView) convertView.findViewById(R.id.textview1);
                holder.collectStatus = (ImageView) convertView.findViewById(R.id.image1);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(c.getName());
            holder.channelnum.setText(String.valueOf(Float.valueOf(c.getChannelnum()/10.0f)));
            if (3 == c.getCol()) {
                holder.collectStatus.setImageResource(R.drawable.ic_action_not_start);
            } else {
                holder.collectStatus.setImageResource(R.drawable.ic_action_start);
            }
            return convertView;
        }


        public class ViewHolder {
            public TextView name;
            public TextView channelnum;
            public ImageView collectStatus;
            public TextView id;
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //获取单击是item的位置
        Channel c = channelList.get(position);
        /*给上一个Activity返回结果*/
        Intent intent = new Intent(this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("channelnum",c.getChannelnum());
        bundle.putInt("collectStatus",c.getCol());
        intent.putExtras(bundle);
        Log.e("return    :","channelnum: "+c.getChannelnum()+" /collectStatus: "+c.getCol());
        setResult(RESULT_OK, intent);
        ListActivity.this.finish();

        dbHelper.close();
    }

    public boolean onKeyDown(int keyCode,KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent();
            Log.e("return2    :","channelnum: "+"/collectStatus: ");
            setResult(RESULT_CANCELED,intent);
            this.finish();
        }
        return true;
    }
}



