package com.longapp.dialog;

import com.longapp.fitness.R;

import android.app.Activity;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddFoodDialog extends Dialog implements View.OnClickListener {

	Activity context;
	private Button addfood_save;
	private SQLiteDatabase database;
	public EditText addfood_name;
	public EditText addfood_protein;
	public EditText addfood_fat;
	public EditText addfood_carbohydrate;

	public AddFoodDialog(Activity context) {
		super(context);
		this.context = context;
	}

	public AddFoodDialog(Activity context, SQLiteDatabase database) {
		super(context);
		this.context = context;
		this.database = database;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.food_add_dialog);
		addfood_name = (EditText) findViewById(R.id.addfood_name);
		addfood_protein = (EditText) findViewById(R.id.addfood_protein);
		addfood_protein.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		addfood_fat = (EditText) findViewById(R.id.addfood_fat);
		addfood_fat.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		addfood_carbohydrate = (EditText) findViewById(R.id.addfood_carbohydrate);
		addfood_carbohydrate.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		/*
		 * * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
		 * * 对象,这样这可以以同样的方式改变这个Activity的属性.
		 */
		Window dialogWindow = this.getWindow();
		WindowManager m = context.getWindowManager();
		Display d = m.getDefaultDisplay();
		// 获取屏幕宽、高用
		WindowManager.LayoutParams p = dialogWindow.getAttributes();
		// 获取对话框当前的参数值
		p.height = (int) (d.getHeight() * 0.6);
		// 高度设置为屏幕的0.6
		p.width = (int) (d.getWidth() * 0.8);
		// 宽度设置为屏幕的0.8
		dialogWindow.setAttributes(p);
		// 根据id在布局中找到控件对象
		addfood_save = (Button) findViewById(R.id.addfood_save);
		// 为按钮绑定点击事件监听器
		addfood_save.setOnClickListener(this);
		this.setCancelable(true);
	}

	@SuppressWarnings("finally")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addfood_save:
			try{
				String sql = "insert into food values(?, ?, ?)";
				String name = this.addfood_name.getText().toString().trim();
				String protein = this.addfood_protein.getText().toString().trim();
				String fat = this.addfood_fat.getText().toString().trim();
				String carbohydrate = this.addfood_carbohydrate.getText().toString().trim();
				if (name.isEmpty() || protein.isEmpty() || fat.isEmpty() || carbohydrate.isEmpty())
					break;
				double d_protein = Double.parseDouble(protein);
				double d_fat = Double.parseDouble(fat);
				double d_carbohydrate = Double.parseDouble(carbohydrate);
				double heat = d_protein*4 + d_fat*9 + d_carbohydrate*4; 
				String nutrient = "热量(千卡):"+ String.format("%.0f", heat) + ";蛋白质(克):" + protein + ";脂肪(克):" + fat + ";碳水化合物(克):" + carbohydrate;
				database.execSQL(sql, new String[] { name, "自定义", nutrient });
				
				Toast toast=Toast.makeText(context, "成功添加食品数据", Toast.LENGTH_SHORT); 
				//显示toast信息 
				toast.show();

			} catch(Exception e) {
				Toast toast=Toast.makeText(context, "食品信息已存在", Toast.LENGTH_SHORT); 
				//显示toast信息 
				toast.show();
			} finally {
				this.dismiss();
				break;
			}
		}
	}
}
