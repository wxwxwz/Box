package com.github.tvbox.osc.wxwz.util;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.tvbox.osc.wxwz.entity.GameLabel;

import java.util.ArrayList;

public class SharedPreferencesUtil {
    private static SharedPreferences.Editor sharedata;
    private static SharedPreferences info;

    //保存String内容
    public static void SaveString(Context context,String data,String saveitem,String saveContext){
        sharedata = context.getSharedPreferences(data,MODE_PRIVATE).edit();
        sharedata.putString(saveitem,saveContext);
        sharedata.commit();
        Log.i(TAG, "Save "+ saveitem +":" +saveContext);
    }

    //保存bool内容
    public static void SaveBool(Context context,String data,String saveitem,Boolean saveContext){
        sharedata = context.getSharedPreferences(data,MODE_PRIVATE).edit();
        sharedata.putBoolean(saveitem,saveContext);
        sharedata.commit();
        Log.i(TAG, "Save "+ saveitem +":" +saveContext);
    }

    public static void SaveStr(Context context,String data,String saveitem,ArrayList<GameLabel> saveContext){

        sharedata = context.getSharedPreferences(data,MODE_PRIVATE).edit();
        if (saveContext==null){
            return;
        }
        String fixData = "";
        int listSize = saveContext.size();
        GameLabel label = new GameLabel();
        label.name = String.valueOf(listSize);
        label.textColor = "FFFFFF";

        saveContext.remove(0);
        saveContext.add(0,label);
        for (int i = 0;i <= listSize - 1;i++){
            if (i == 0){
                fixData = saveContext.get(i).name;
            }else {
                fixData = fixData + "#" + saveContext.get(i).name;
            }


        }
        sharedata.putString(saveitem,fixData);
        sharedata.commit();
    }

    /*public static void SaveStr(Context context,String data,String saveitem,List<MessageInfo> saveContext,int sum){
        sharedata = context.getSharedPreferences(data,MODE_PRIVATE).edit();
        SaveInt(context,"sumbackup","messagenum",sum);
        for (int i = 0;i < sum;i++){
            String message = saveContext.get(i).getMessageContext();
            //Boolean IY = saveContext.get(i).getIY();
            sharedata.putString(saveitem+String.valueOf(i),message);
            Log.i(TAG, "Save "+ saveitem+String.valueOf(i) +":" +message);
           // sharedata.putBoolean(saveitem+"-"+String.valueOf(i),IY);
            //Log.i(TAG, "Save "+ saveitem+"-"+String.valueOf(i) +":" +IY);
        }

        sharedata.commit();
    }*/

    public static void SaveInt(Context context,String data,String saveitem,int saveContext){
        sharedata = context.getSharedPreferences(data,MODE_PRIVATE).edit();
        sharedata.putString(saveitem, String.valueOf(saveContext));
        sharedata.commit();
        Log.i(TAG, "SaveInt "+ saveitem +":" +saveContext);
    }

    //读取保存内容
    public static String readByString(Context context,String data,String saveitem){
        info = context.getSharedPreferences(data,MODE_PRIVATE);
        String saveContext = info.getString(saveitem,"");
        Log.i(TAG, "readByString "+ saveitem +":" +saveContext);
        return saveContext;
    }

    public static ArrayList<GameLabel> readByStr(Context context,String data,String saveitem){
        info = context.getSharedPreferences(data,MODE_PRIVATE);
        ArrayList<GameLabel> list = new ArrayList<>();
        String saveContext = info.getString(saveitem,"");
        String[] s = saveContext.split("#");

        for (int i = 0; i <= Integer.parseInt(s[0]) - 1; i++){
                list.add(new GameLabel(s[i],"FFFFFF"));
        }
        return list;
    }

    /*public static List<MessageInfo> readByStr(Context context,String data,String saveitem){
        info = context.getSharedPreferences(data,MODE_PRIVATE);
        int p = readByInt(context,"sumbackup","messagenum");
        List<MessageInfo> list= new ArrayList<MessageInfo>();

        for (int i = 0;i < p;i++){
            String message = info.getString(saveitem+String.valueOf(i),"");
            Boolean IY = info.getBoolean(saveitem+"-"+String.valueOf(i),false);
            *//*MessageInfo messageInfo = new MessageInfo(message,IY);
            list.add(messageInfo);
            Log.i(TAG, "readByString " +":" +list.get(i).getMessageContext() + "IY:" + list.get(i).isIY());*//*
        }

        return list;
    }*/

    public static int readByInt(Context context,String data,String saveitem){
        info = context.getSharedPreferences(data,MODE_PRIVATE);
        if (info == null){
            return 0;
        }
        int saveContext = 0;
        try {
            saveContext = Integer.parseInt(info.getString(saveitem,null));
            return saveContext;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "readByInt "+ saveitem +":" +saveContext);
        return saveContext;
    }

    //读取保存内容
    public static Boolean readByBool(Context context,String data,String saveitem,Boolean defValue){
        info = context.getSharedPreferences(data,MODE_PRIVATE);
        Boolean saveContext = info.getBoolean(saveitem,defValue);
        Log.i(TAG, "readByBool "+ saveitem +":" +saveContext);
        return saveContext;
    }

    /**
     * 保存数据
     *
     * @param context context
     * @param key     key
     * @param data    data
     */
    public static void saveData(Context context, String FILE_NAME,String key, Object data) {
        String type = data.getClass().getSimpleName();
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        switch (type) {
            case "Integer":
                editor.putInt(key, (Integer) data);
                break;
            case "Boolean":
                editor.putBoolean(key, (Boolean) data);
                break;
            case "String":
                editor.putString(key, (String) data);
                break;
            case "Float":
                editor.putFloat(key, (Float) data);
                break;
            case "Long":
                editor.putLong(key, (Long) data);
                break;
            default:
                break;
        }
        editor.apply();
    }

    /**
     * 获取数据
     *
     * @param context      context
     * @param key          key
     * @param defaultValue defaultValue
     * @return value in SP
     */

    @SuppressWarnings("unchecked")
    public static <T> T getData(Context context, String FILE_NAME,String key, T defaultValue) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (defaultValue instanceof Integer) {
            return (T) (Integer) pref.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return (T) (Boolean) pref.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof String) {
            return (T) pref.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Float) {
            return (T) (Float) pref.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return (T) (Long) pref.getLong(key, (Long) defaultValue);
        } else {
            return defaultValue;
        }
    }
}
