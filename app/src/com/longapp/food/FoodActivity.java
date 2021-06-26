package com.longapp.food;

import java.util.ArrayList;

import com.longapp.dao.DBManager;
import com.longapp.dialog.AddDietDialog;
import com.longapp.dialog.AddFoodDialog;
import com.longapp.fitness.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class FoodActivity extends Activity implements TextWatcher {

	private SQLiteDatabase database;
	private AutoCompleteTextView actvFood;
	private Button searchBtn;
	private Button addfoodBtn;
	
	private Button result_adddietBtn;
	private TextView result_foodName;
	private ListView result_nutrientList;
	
	private TextView nearlyTips;
	private Button[] nearlys;
	
	private AddFoodDialog addfoodDialog;
	private AddDietDialog adddietDialog;
	
	private ViewPager pager = null;
	private ArrayList<View> viewContainter = new ArrayList<View>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.food_activity);

		DBManager dbmanager = new DBManager(this);
		database = dbmanager.openDatabase();

		actvFood = (AutoCompleteTextView) findViewById(R.id.actvFood);
		actvFood.addTextChangedListener(this);

		searchBtn = (Button) findViewById(R.id.searchBtn);
		searchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchFood();
			}
		});

		addfoodBtn = (Button) findViewById(R.id.addfoodBtn);
		addfoodBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addFood();
			}
		});

		pager = (ViewPager) this.findViewById(R.id.tools_viewpager);
		View view1 = LayoutInflater.from(this).inflate(R.layout.food_nearly_page, null);
		View view2 = LayoutInflater.from(this).inflate(R.layout.food_result_page, null);
		
		result_foodName = (TextView) view2.findViewById(R.id.nutrientResult);
		result_adddietBtn = (Button) view2.findViewById(R.id.adddietBtn);
		result_adddietBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addDiet();
			}
		});
		result_nutrientList = (ListView) view2.findViewById(R.id.nutrientList);
		setListLongClick();//长按食物的任一营养条目弹出删除食物菜单
		
		nearlyTips = (TextView)view1.findViewById(R.id.food_nearly_tips);
		nearlys = new Button[10];
		for(int i = 0; i < 10; i++) {
			nearlys[i] = (Button)view1.findViewById(R.id.food_nearly_1 + i);
			nearlys[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Button vv = (Button)v;
					String name = vv.getText().toString();
					if (name.isEmpty())
						return;
					String sql = "select nutrient from food where name=?";
					Cursor cursor = database.rawQuery(sql, new String[] {name});
					
					if (cursor.getCount() > 0) {
						// 必须使用moveToFirst方法将记录指针移动到第1条记录的位置
						cursor.moveToFirst();
						String nutrient = cursor.getString(cursor.getColumnIndex("nutrient"));
						String heat = nutrient.split(";")[0].split(":")[1];
						String protein = nutrient.split(";")[1].split(":")[1];
						String fat = nutrient.split(";")[2].split(":")[1];
						String car = nutrient.split(";")[3].split(":")[1];
						showAddDietDialog(name, heat+";"+protein+";"+fat+";"+car);
					}
				}
			});
		}
		Cursor cursor = database.rawQuery("select distinct name from diet order by id desc limit 10", new String[]{});
		if (cursor.getCount() > 0) {
			nearlyTips.setVisibility(View.VISIBLE);
			cursor.moveToFirst();
			int i = 0;
			for(i = 0; i < cursor.getCount(); i++) {
				nearlys[i].setText(cursor.getString(cursor.getColumnIndex("name")));
				nearlys[i].setVisibility(View.VISIBLE);
				cursor.moveToNext();
			}
			for(; i < 10; i++) {
				nearlys[i].setVisibility(View.INVISIBLE);
			}
		} else {
			nearlyTips.setVisibility(View.INVISIBLE);
		}
		
		//viewpager开始添加view
		viewContainter.add(view1);
		viewContainter.add(view2);
		
		pager.setAdapter(new PagerAdapter() {
			//viewpager中的组件数量
			@Override
			public int getCount() {
				return viewContainter.size();
			}
			//滑动切换的时候销毁当前的组件
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				((ViewPager) container).removeView(viewContainter.get(position));
			}
			//每次滑动的时候生成的组件
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				((ViewPager) container).addView(viewContainter.get(position));
				return viewContainter.get(position);
			}
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			@Override
			public int getItemPosition(Object object) {
				return super.getItemPosition(object);
			}
		});
	}
	
	private void setListLongClick() {
		this.result_nutrientList.setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
	        	
	                new AlertDialog.Builder(FoodActivity.this)
	                        .setItems(R.array.menu_food, new DialogInterface.OnClickListener() {
	                                public void onClick(DialogInterface dialog, int which) {
	                                        String[] PK = getResources().getStringArray(R.array.menu_food);
	                                        if(PK[which].equals("删除该食物信息")) {
	                                        	showDeleteDialog();
	                                        }
	                                }
	                        }).show();
	                return true;
	       }
		});
	}
	
	private void searchFood() {
		String sql = "select name,type,nutrient from food where name=?";
		if (actvFood.getText().toString().isEmpty())
			return;
		Cursor cursor = database.rawQuery(sql, new String[] { actvFood.getText().toString() });
		String result = "未找到该食品";
		if (cursor.getCount() > 0) {
			// 必须使用moveToFirst方法将记录指针移动到第1条记录的位置
			cursor.moveToFirst();
			result = cursor.getString(cursor.getColumnIndex("name")) + "-";
			result = result + cursor.getString(cursor.getColumnIndex("type"));
			String nutrient = cursor.getString(cursor.getColumnIndex("nutrient"));
			NutrientAdapter nutrientAdapter = new NutrientAdapter();
			result_nutrientList.setAdapter(nutrientAdapter.getAdapter(this, nutrient));
			result_adddietBtn.setVisibility(View.VISIBLE);
		} else {
			result_nutrientList.setAdapter(null);
			result_adddietBtn.setVisibility(View.INVISIBLE);
		}
		result_foodName.setText(result);
		pager.setCurrentItem(1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		pager.setCurrentItem(0);
	}
	
	private void addFood() {
		addfoodDialog = new AddFoodDialog(this, database);
		addfoodDialog.show();
	}
	
	private void addDiet() {
		String name = result_foodName.getText().toString().split("-")[0];
		TextView heat_tv = (TextView)result_nutrientList.getChildAt(0).findViewById(R.id.nutrient_content);
		TextView protein_tv = (TextView)result_nutrientList.getChildAt(1).findViewById(R.id.nutrient_content);
		TextView fat_tv = (TextView)result_nutrientList.getChildAt(2).findViewById(R.id.nutrient_content);
		TextView carbohydrate_tv = (TextView)result_nutrientList.getChildAt(3).findViewById(R.id.nutrient_content);
		String nutrient = heat_tv.getText().toString() + ";" + protein_tv.getText().toString() + ";" + fat_tv.getText().toString() + ";" + carbohydrate_tv.getText().toString();
		showAddDietDialog(name, nutrient);
	}

	private void showAddDietDialog(String name, String nutrient) {
		adddietDialog = new AddDietDialog(this, database, name, nutrient);
		adddietDialog.show();
	}
	
	//提示是否删除食物
	private void showDeleteDialog() {
		new AlertDialog.Builder(this).setTitle("提示").setMessage("确定删除该食物数据?")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						deleteFood();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				}).show();
	}

	//处理菜单按钮删除食物信息
	private void deleteFood() {
		try{
			String sql = "delete from food where name=?";
			String name = result_foodName.getText().toString().split("-")[0];
			database.execSQL(sql, new String[] { name });
			result_adddietBtn.setVisibility(View.INVISIBLE);
			
			Toast toast=Toast.makeText(this, "成功删除食品数据", Toast.LENGTH_SHORT); 
			//显示toast信息 
			toast.show();
		} catch(Exception e) {
			//Log.i("exception:", ""+e);
			Toast toast=Toast.makeText(this, "删除出错啦", Toast.LENGTH_SHORT); 
			//显示toast信息 
			toast.show();
		}
	}
		
	// AutoCompleteTextView的重写函数3个
	@Override
	public void afterTextChanged(Editable s) {
		Cursor cursor = database.rawQuery("select name as _id from food where name like ?",
				new String[] { "%" + s.toString() + "%" });
		FoodNameAdapter myAdapter = new FoodNameAdapter(this, cursor, true);
		actvFood.setAdapter(myAdapter);
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
		if(actvFood.getAdapter() == null) {
			Cursor cursor = database.rawQuery("select name as _id from food where name like ?",
					new String[] { "%" + s.toString() + "%" });
			FoodNameAdapter myAdapter = new FoodNameAdapter(this, cursor, true);
			actvFood.setAdapter(myAdapter);
		}
	}
	
}
