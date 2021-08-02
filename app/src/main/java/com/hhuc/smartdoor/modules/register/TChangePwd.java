package com.hhuc.smartdoor.modules.register;

import android.content.Context;
import android.util.Log;

import com.hhuc.smartdoor.R;
import com.hhuc.smartdoor.modules.utils.HttpUtil;

public class TChangePwd {
    private final static String TAG = "TChangePwd";

    public static int ChangeUserPwd(Context context, String uid, String pwd, String newpwd) {
        int res = 0;
        try {
            String url = context.getResources().getString(R.string.apiUrl);
            url += "/userChangePwd?";
            url += "uid=" + uid;
            url += "&pwd=" + pwd;
            url += "&newpwd=" + newpwd;
            String resp = HttpUtil.httpGet(url);
            if (resp.contains("\"status\": \"1\""))
                res = 1;
            else if (resp.contains("\"status\": \"2\""))
                res = 2;
            else if (resp.contains("\"status\": \"3\""))
                res = 3;
        } catch (Exception er) {
            Log.e(TAG, "registerUser: " + er.getMessage());
        }
        return res;
    }
}
