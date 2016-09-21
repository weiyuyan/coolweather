package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {

	/**
	 * 解 析 和 处 理 服 务 器 返 回 的 省 级 数 据 
	 */
	public synchronized static  boolean handleProvincesResponse (CoolWeatherDB coolWeatherDB , String response) {
		if  (!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces != null && allProvinces.length > 0){
				for  (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolWeatherDB.saveProvince(province);
							
				}
				return true;
			}
		}
		return false;
	}
	
	
	
	

	/**
	 * 解 析 和 处 理 服 务 器 返 回 的 市 级 数 据 
	 */
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB , String response , int provinceId) {
		if (!TextUtils.isEmpty(response) ){
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0 ) {
				for (String c : allCities) {
					String [] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//将 解 析 出 来 的 数 据 存 储 到 City 表
					coolWeatherDB.saveCity(city);
				}
			return true;
			}
			
		}
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * 解 析 和 处 理 服 务 器 返 回 的 县 级 数 据 
	 */
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB , String response , int cityId){
		if (!TextUtils.isEmpty(response)){
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0){
				for (String c :allCounties) {
					String [] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					//将 解 析 出 来 的 数 据 存 储 到 County 表
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
					
		}
		return false ;
	}
	
	
	
	
	
	
	
	
	
}





















