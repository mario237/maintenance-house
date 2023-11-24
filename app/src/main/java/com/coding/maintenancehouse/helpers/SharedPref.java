package com.coding.maintenancehouse.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    final SharedPreferences sharedPreferences ;

    public SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences("maintenancehouse", Context.MODE_PRIVATE);
    }

    public void putBoolean(String key , Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key , value);
        editor.apply();
    }

    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key , false);
    }

    public void putString(String key , String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key , value);
        editor.apply();
    }

    public String getString(String key){
        return sharedPreferences.getString(key , null);
    }



}
