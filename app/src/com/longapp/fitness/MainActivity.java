package com.longapp.fitness;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import com.longapp.analysis.AnalysisActivity;
import com.longapp.dao.DBManager;
import com.longapp.dialog.AddFoodDialog;
import com.longapp.diet.DietActivity;
import com.longapp.food.FoodActivity;
import com.longapp.tools.ToolsActivity;
import android.app.ActivityGroup;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends ActivityGroup {
	private TabHost tabHost;// 声明一个TabHost对象
	private DBManager dbmanager;
	NotificationManager mNotificationManager;
	
	//自定义广播接收器
	private IntentFilter intentFilter;
	private MyReceiver myReceiver;
	
	//配置文件路径
	private final String config_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fitness";
	private final String config_FILE = config_PATH + "/" +"fitness.notifity";
	
	//激励语句
	private final String[] tips = {"科学锻炼、合理饮食、规律作息。加油！", "七分靠吃、三分靠练。加油！", 
			"摄入热量 > 消耗热量 => 增重。加油！", "摄入热量 < 消耗热量 => 减重。加油！", "滴水穿石，坚持就是胜利！加油！"};

	// 资源文件
	@SuppressWarnings("rawtypes")
	private Class activitys[] = { DietActivity.class, AnalysisActivity.class, ToolsActivity.class, FoodActivity.class };// 跳转的Activity
	private String title[] = { "饮食", "分析", "工具", "食品库" };// 设置菜单的标题
	private int image[] = { R.drawable.tab_icon1, R.drawable.tab_icon2, R.drawable.tab_icon3, R.drawable.tab_icon4 };// 设置菜单

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);
		dbmanager = new DBManager(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction("com.longapp.broadcast.Notify_BROADCAST");
		myReceiver = new MyReceiver();
		registerReceiver(myReceiver, intentFilter);
		
		initTabView();// 初始化tab标签
		Notify();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}
	
	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Notify();//更新通知栏信息
		}
	}
	
	private void initTabView() {
		// 实例化tabhost
		this.tabHost = (TabHost) findViewById(R.id.mytabhost);
		// 由于继承了ActivityGroup，所以需要在setup方法里加入此参数，若继承TabActivity则可省略
		tabHost.setup(this.getLocalActivityManager());

		// 创建标签
		for (int i = 0; i < activitys.length; i++) {
			// 实例化一个view作为tab标签的布局
			View view = View.inflate(this, R.layout.main_tab_layout, null);

			// 设置imageview
			ImageView imageView = (ImageView) view.findViewById(R.id.image);
			imageView.setImageDrawable(getResources().getDrawable(image[i]));
			// 设置textview
			TextView textView = (TextView) view.findViewById(R.id.title);
			textView.setText(title[i]);
			// 设置跳转activity
			Intent intent = new Intent(this, activitys[i]);

			// 载入view对象并设置跳转的activity
			TabSpec spec = tabHost.newTabSpec(title[i]).setIndicator(view).setContent(intent);

			// 添加到选项卡
			tabHost.addTab(spec);
		}

	}

	//创建选项菜单
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, 0, "饮食提醒");
        menu.add(0, Menu.FIRST+1, 0, "新食物");
        menu.add(0, Menu.FIRST+2, 0, "退出");
        return true;
    }

    // 通过点击了哪个菜单子项来改变Activity的标题 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST: 
			if(checkNotify()) {
				if(setNotify(false)) {
					mNotificationManager.cancel(1);
					Toast toast=Toast.makeText(this, "已取消通知栏提醒", Toast.LENGTH_SHORT); 
					//显示toast信息 
					toast.show();
				}
			} else {
				if(setNotify(true)) {
					Notify();
					Toast toast=Toast.makeText(this, "已开启通知栏提醒", Toast.LENGTH_SHORT); 
					//显示toast信息 
					toast.show();
				}
			}
			break; 
		case Menu.FIRST+1:
			new AddFoodDialog(this, dbmanager.openDatabase()).show();
        	break;
		case Menu.FIRST+2:
			System.exit(0);
        	break;
		}
		return true;
	}
	
	//消息通知栏
	public void Notify() {
		if(checkNotify()) {
			Date date = new Date();
			String time = String.format("%tY%tm%td", date, date, date);
			Cursor cursor_total = dbmanager.openDatabase().rawQuery("select sum(heat) as total from diet where time=?", new String[] { time });
		    cursor_total.moveToFirst();
		    String total = String.format("%.0f", cursor_total.getDouble(0)) + "大卡";
		    
		    //定义通知栏展现的内容信息
		    Notification notification = new Notification(R.drawable.ic_launcher, "今日已摄入热量:" + total, System.currentTimeMillis());
		    notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		    
		    Intent notificationIntent = new Intent(this, MainActivity.class);
		    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		    
		    int i = (int)(Math.random()*5);
		    notification.setLatestEventInfo(this, "今日已摄入热量:" + total, tips[i], contentIntent);
		    mNotificationManager.notify(1, notification);
		}
	}
	
	public boolean checkNotify() {
		File dir = new File(config_PATH);
		if (!dir.exists())
			dir.mkdir();
		File file = new File(config_FILE);
		if(file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setNotify(boolean enable) {
		File file = new File(config_FILE);
		if(enable) {
			if(file.exists())
				return true;
			else {
				try {
					file.createNewFile();
					return true;
				} catch (IOException e) {
					return false;
				}
			}
		} else {
			if(file.exists()) {
				file.delete();
			}
			return true;
		}
	}

}
