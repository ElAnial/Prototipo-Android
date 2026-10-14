package com.example.sqlite;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String PREF_NAME = "SessionInventoryPref";
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_ROLE_ID = "roleId";
    public static final String KEY_ROLE_NAME = "roleName";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int userId, String username, String fullName, int roleId, String roleName) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putInt(KEY_ROLE_ID, roleId);
        editor.putString(KEY_ROLE_NAME, roleName);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public int getRoleId() {
        return pref.getInt(KEY_ROLE_ID, -1);
    }

    public String getRoleName() {
        return pref.getString(KEY_ROLE_NAME, "");
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USER_ID, String.valueOf(pref.getInt(KEY_USER_ID, -1)));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));
        user.put(KEY_FULL_NAME, pref.getString(KEY_FULL_NAME, ""));
        user.put(KEY_ROLE_ID, String.valueOf(pref.getInt(KEY_ROLE_ID, -1)));
        user.put(KEY_ROLE_NAME, pref.getString(KEY_ROLE_NAME, ""));
        return user;
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
