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
	private TabHost tabHost;// ����һ��TabHost����
	private DBManager dbmanager;
	NotificationManager mNotificationManager;
	
	//�Զ���㲥������
	private IntentFilter intentFilter;
	private MyReceiver myReceiver;
	
	//�����ļ�·��
	private final String config_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fitness";
	private final String config_FILE = config_PATH + "/" +"fitness.notifity";
	
	//�������
	private final String[] tips = {"��ѧ������������ʳ��������Ϣ�����ͣ�", "�߷ֿ��ԡ����ֿ��������ͣ�", 
			"�������� > �������� => ���ء����ͣ�", "�������� < �������� => ���ء����ͣ�", "��ˮ��ʯ����־���ʤ�������ͣ�"};

	// ��Դ�ļ�
	@SuppressWarnings("rawtypes")
	private Class activitys[] = { DietActivity.class, AnalysisActivity.class, ToolsActivity.class, FoodActivity.class };// ��ת��Activity
	private String title[] = { "��ʳ", "����", "����", "ʳƷ��" };// ���ò˵��ı���
	private int image[] = { R.drawable.tab_icon1, R.drawable.tab_icon2, R.drawable.tab_icon3, R.drawable.tab_icon4 };// ���ò˵�

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
		
		initTabView();// ��ʼ��tab��ǩ
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
			Notify();//����֪ͨ����Ϣ
		}
	}
	
	private void initTabView() {
		// ʵ����tabhost
		this.tabHost = (TabHost) findViewById(R.id.mytabhost);
		// ���ڼ̳���ActivityGroup��������Ҫ��setup���������˲��������̳�TabActivity���ʡ��
		tabHost.setup(this.getLocalActivityManager());

		// ������ǩ
		for (int i = 0; i < activitys.length; i++) {
			// ʵ����һ��view��Ϊtab��ǩ�Ĳ���
			View view = View.inflate(this, R.layout.main_tab_layout, null);

			// ����imageview
			ImageView imageView = (ImageView) view.findViewById(R.id.image);
			imageView.setImageDrawable(getResources().getDrawable(image[i]));
			// ����textview
			TextView textView = (TextView) view.findViewById(R.id.title);
			textView.setText(title[i]);
			// ������תactivity
			Intent intent = new Intent(this, activitys[i]);

			// ����view����������ת��activity
			TabSpec spec = tabHost.newTabSpec(title[i]).setIndicator(view).setContent(intent);

			// ��ӵ�ѡ�
			tabHost.addTab(spec);
		}

	}

	//����ѡ��˵�
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, 0, "��ʳ����");
        menu.add(0, Menu.FIRST+1, 0, "��ʳ��");
        menu.add(0, Menu.FIRST+2, 0, "�˳�");
        return true;
    }

    // ͨ��������ĸ��˵��������ı�Activity�ı��� 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST: 
			if(checkNotify()) {
				if(setNotify(false)) {
					mNotificationManager.cancel(1);
					Toast toast=Toast.makeText(this, "��ȡ��֪ͨ������", Toast.LENGTH_SHORT); 
					//��ʾtoast��Ϣ 
					toast.show();
				}
			} else {
				if(setNotify(true)) {
					Notify();
					Toast toast=Toast.makeText(this, "�ѿ���֪ͨ������", Toast.LENGTH_SHORT); 
					//��ʾtoast��Ϣ 
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
	
	//��Ϣ֪ͨ��
	public void Notify() {
		if(checkNotify()) {
			Date date = new Date();
			String time = String.format("%tY%tm%td", date, date, date);
			Cursor cursor_total = dbmanager.openDatabase().rawQuery("select sum(heat) as total from diet where time=?", new String[] { time });
		    cursor_total.moveToFirst();
		    String total = String.format("%.0f", cursor_total.getDouble(0)) + "��";
		    
		    //����֪ͨ��չ�ֵ�������Ϣ
		    Notification notification = new Notification(R.drawable.ic_launcher, "��������������:" + total, System.currentTimeMillis());
		    notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		    
		    Intent notificationIntent = new Intent(this, MainActivity.class);
		    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		    
		    int i = (int)(Math.random()*5);
		    notification.setLatestEventInfo(this, "��������������:" + total, tips[i], contentIntent);
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
