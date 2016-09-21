package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String>dataList = new ArrayList<String>() ;
	
	
	

	
	/**
	 *  省 列 表 
	 */
	private List<Province> provinceList;
	
	
	
	/**
	 * 市 列 表
	 */
	private List<City> cityList;
	
	
	/**
	 * 县 列 表
	 */
	private List<County> countyList;
	
	
	/*
	 * 选 中 的 省 份
	 */
	private Province selectedProvince;
	
	
	/**
	 * 选 中 的 城 市 
	 */
	private City selectedCity;

	
	
	
	
	/**
	 * 当 前 选 中 的 级 别
	 */
	private int currentLevel;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter= new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1 , dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick (AdapterView<?>arg0 ,View view,int index ,long arg3){
				if (currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity= cityList.get(index);
					queryCounties();
				}
				
			}
			
		});
		queryProvinces();    //加 载 省 级 数 据
	}

	


	/**
	 * 查 询 全 国 所 有 的 省 ， 优 先 从 数 据 库 查 询 ， 如 果 没 有 查 询 到 再 去 服 务 器 上 查 询
	 */

	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0){
			dataList.clear();
			for(Province province :provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中 国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null , "province");
		}
	}






	/**
	 * 查 询 选 中 省 内 所 有 的 市 ， 优 先 从 数 据 库 查 询 ， 如 果 没 有 查 询 到 再 去 服 务 器 上 查  询
	 */
	private void queryCities(){
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size() > 0 ){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
			
	}
	

	
	
	/**
	 * 查 询 选 中 市 内 所 有 的 县 ， 优 先 从 数 据 库 查 询 ， 如 果 没 有 查 询 到 再 去 服 务 器 上 查  询
	 */
	
	private void queryCounties(){
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0 ){
			dataList.clear();
			for(County county: countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	
	
	
	
	
	/**
	 * 根 据 传 入 的 代 号 和 类 型 从 服 务 器 上 查 询 省 市 县 数 据 
	 */
	private void queryFromServer(final String code ,final String type){
		String address;
		if (!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml" ;
		}else {
			address = "http://www.weather.com.cn/data/list3/city.xml" ;
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			@Override
			public void onFinish(String response){
				boolean result = false;
				if ("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if(result) {
					runOnUiThread(new Runnable() {
						@Override
						public void run(){
							closeProgressDialog();
							if ("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if ("county".equals(type)){
								queryCounties();
							}
						}
					});
					
				}
			}
			
			@Override
			public void onError (Exception e){
				// 通 过 runOnUiThread() 方 法 回 到 主 线  程 处 理  逻 辑
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加 载 失 败", Toast.LENGTH_SHORT).show();
					}
				});
					
				
			}
		});
		
	}
	
	
	
	
	
	/**
	 * 显 示 进 度 对 话 框
	 */
	private void showProgressDialog(){
	
		if (progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正 在 加 载 。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	
	
	
	/**
	 * 关 闭 进 度 对 话 框 
	 */
	private void closeProgressDialog(){
		if (progressDialog != null){
			progressDialog.dismiss();
		}
		
	}
	
	
	
	
	/**
	 * 捕 获  BACK 键 ， 判 断 应 该 返 回 市 列 表 ， 省 列 表 还 是 直 接 退 出 
	 */
	@Override
	public void onBackPressed(){
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else {
			finish();
		}
	}
}





















