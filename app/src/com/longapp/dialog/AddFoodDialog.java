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
		 * * ��ȡʥ����Ĵ��ڶ��󼰲����������޸ĶԻ���Ĳ�������, ����ֱ�ӵ���getWindow(),��ʾ������Activity��Window
		 * * ����,�����������ͬ���ķ�ʽ�ı����Activity������.
		 */
		Window dialogWindow = this.getWindow();
		WindowManager m = context.getWindowManager();
		Display d = m.getDefaultDisplay();
		// ��ȡ��Ļ������
		WindowManager.LayoutParams p = dialogWindow.getAttributes();
		// ��ȡ�Ի���ǰ�Ĳ���ֵ
		p.height = (int) (d.getHeight() * 0.6);
		// �߶�����Ϊ��Ļ��0.6
		p.width = (int) (d.getWidth() * 0.8);
		// �������Ϊ��Ļ��0.8
		dialogWindow.setAttributes(p);
		// ����id�ڲ������ҵ��ؼ�����
		addfood_save = (Button) findViewById(R.id.addfood_save);
		// Ϊ��ť�󶨵���¼�������
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
				String nutrient = "����(ǧ��):"+ String.format("%.0f", heat) + ";������(��):" + protein + ";֬��(��):" + fat + ";̼ˮ������(��):" + carbohydrate;
				database.execSQL(sql, new String[] { name, "�Զ���", nutrient });
				
				Toast toast=Toast.makeText(context, "�ɹ����ʳƷ����", Toast.LENGTH_SHORT); 
				//��ʾtoast��Ϣ 
				toast.show();

			} catch(Exception e) {
				Toast toast=Toast.makeText(context, "ʳƷ��Ϣ�Ѵ���", Toast.LENGTH_SHORT); 
				//��ʾtoast��Ϣ 
				toast.show();
			} finally {
				this.dismiss();
				break;
			}
		}
	}
}
