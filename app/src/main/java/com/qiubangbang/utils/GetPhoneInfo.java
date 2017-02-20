package com.qiubangbang.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.Log;

import com.qiubangbang.getphoneinfo.PhoneInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qiubangbang on 2017/2/17.
 * 通过内容提供者获取系统短信，通讯录一般步骤
 * 1 获取内容提供者 contentresolver
 * 2 创建读取的内容的uri
 * 3 query--》获取游标cursor，遍历游标，关闭游标
 */

public class GetPhoneInfo {

    private static final String[] PHONES_PROJECTION = new String[]{
            Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_ID, Phone.CONTACT_ID
    };
    private static final String TAG = "GetPhoneInfo";
    private static LocationManager locationManager;
    private static String provider;

    /**
     * 获取手机通讯录
     */
    public static List<PhoneInfo> getContants(Context context) {
        List<PhoneInfo> phoneInfoList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        //获取手机联系人
        Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
                PHONES_PROJECTION, null, null, null);
        PhoneInfo phoneInfo;
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                phoneInfo = new PhoneInfo();
                //得到手机号码
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
                //当手机号码为空或者空字段，跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;
                //得到联系人名称
                String phoneName = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.DISPLAY_NAME));
                //得到联系人ID
                Long contactid = phoneCursor.getLong(phoneCursor.getColumnIndex(Phone.CONTACT_ID));
                phoneInfo.setContactid(contactid);
                phoneInfo.setPhoneNumber(phoneNumber);
                phoneInfo.setPhoneName(phoneName);
                phoneInfoList.add(phoneInfo);

                Log.d(TAG, "getContants: " + phoneName);
            }
            phoneCursor.close();
        }
        return phoneInfoList;

    }

    /**
     * 获取手机内所以短消息
     */
    public static List<PhoneInfo> getSmsInPhone(Context context) {
        List<PhoneInfo> phoneInfoList = new ArrayList<>();
        final String SMS_URI_ALL = "content://sms/";
        /*final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_SEND  = "content://sms/sent";
        final String SMS_URI_DRAFT = "content://sms/draft";  */

        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = new String[]{"_id", "address", "person",
                    "body", "date", "type"};
            Uri uri = Uri.parse(SMS_URI_ALL);
            Cursor cur = cr.query(uri, projection, null, null, "date desc");

            if (cur.moveToFirst()) {
                String name;
                String phoneNumber;
                String smsbody;
                String date;
                String type;

                //    int nameColumn = cur.getColumnIndex("person");
                int phoneNumberColumn = cur.getColumnIndex("address");
                int smsbodyColumn = cur.getColumnIndex("body");
                int dateColumn = cur.getColumnIndex("date");
                int typeColumn = cur.getColumnIndex("type");

                PhoneInfo phoneInfo;
                do {
                    phoneInfo = new PhoneInfo();
                    phoneNumber = cur.getString(phoneNumberColumn);
                    //    name = cur.getString(nameColumn);    这样获取的联系认为空，所以我改用下面的方法获取
                    name = getPeopleNameFromPerson(phoneNumber, context);
                    smsbody = cur.getString(smsbodyColumn);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date d = new Date(Long.parseLong(cur.getString(dateColumn)));
                    date = dateFormat.format(d);

                    int typeId = cur.getInt(typeColumn);
                    if (typeId == 1) {
                        type = "接收";
                    } else if (typeId == 2) {
                        type = "发送";
                    } else {
                        type = "草稿";
                    }

                    phoneInfo.setPhoneNumber(phoneNumber);
                    phoneInfo.setMessageContent(smsbody);
//                    title.add(type + " " + date + '\n' + phoneNumber);
//                    text.add(name + '\n' + smsbody);
                    phoneInfoList.add(phoneInfo);
                    if (smsbody == null) smsbody = "";
                } while (cur.moveToNext());
            }
            cur.close();
            cur = null;
        } catch (SQLiteException ex) {
            Log.e("SQLiteException", ex.getMessage());
        }
        return phoneInfoList;

    }

    /**
     * 获取手机位置信息
     */
    public static Map<Integer, String> getPhoneLocation(Context context) {
        HashMap<Integer, String> map = new HashMap<>();
        //获得位置服务
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        provider = judgeProvider(locationManager);
        if (provider != null) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                String latitude = location.getLatitude() + "";
                String longitude = location.getLongitude() + "";
                Log.d(TAG, "getPhoneLocation: latitude " + latitude);
                Log.d(TAG, "getPhoneLocation: longitude " + longitude);
                map.put(0, latitude);
                map.put(1, longitude);
            } else {
                Log.e(TAG, "getPhoneLocation: 无法获取当前位置");
            }
        }
        return map;
    }


    /**
     * 通过address手机号关联Contacts联系人的显示名字
     *
     * @param address
     * @return
     */
    private static String getPeopleNameFromPerson(String address, Context context) {
        if (address == null || address == "") {
            return null;
        }

        String strPerson = "null";
        String[] projection = new String[]{Phone.DISPLAY_NAME, Phone.NUMBER};

        Uri uri_Person = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, address);  // address 手机号过滤
        Cursor cursor = context.getContentResolver().query(uri_Person, projection, null, null, null);

        if (cursor.moveToFirst()) {
            int index_PeopleName = cursor.getColumnIndex(Phone.DISPLAY_NAME);
            String strPeopleName = cursor.getString(index_PeopleName);
            strPerson = strPeopleName;
        } else {
            strPerson = address;
        }
        cursor.close();
        cursor = null;
        return strPerson;
    }

    /**
     * 判断是否有内容提供器
     *
     * @return 不存在返回null
     */
    private static String judgeProvider(LocationManager locationManager) {
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        } else {
            Log.e(TAG, "judgeProvider: 没有可用的位置提供器");
        }
        return null;
    }

}
