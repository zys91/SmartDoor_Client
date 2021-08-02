package com.hhuc.smartdoor.ui.account;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hhuc.smartdoor.MainApplication;
import com.hhuc.smartdoor.R;
import com.hhuc.smartdoor.modules.register.TChangePwd;
import com.hhuc.smartdoor.modules.widget.ChangePwdDialog;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.hhuc.smartdoor.MainApplication.app_hid;
import static com.hhuc.smartdoor.MainApplication.app_tel;
import static com.hhuc.smartdoor.MainApplication.app_uid;

public class Account extends Fragment {
    public static final int REQUEST_CODE = 0x002;
    private ImageView mHBack;
    private ImageView mHHead;
    private ItemView mHouseId;
    private ItemView mPass;
    private ItemView mPhone;
    private ItemView mAbout;
    private View root;
    public final static int What_ChangeSuccess = 3000;
    public final static int What_ChangeFailed = 3010;
    public final static int What_ConnectFailed = 3015;
    public final static int What_UserError = 3020;
    private static final String TAG = "Account";
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == What_ChangeSuccess) {
                Toast.makeText(getActivity(), "密码修改成功!", Toast.LENGTH_SHORT).show();
            } else if (msg.what == What_ChangeFailed) {
                Toast.makeText(getActivity(), "修改失败！原密码错误！", Toast.LENGTH_SHORT).show();
            } else if (msg.what == What_ConnectFailed) {
                Toast.makeText(getActivity(), "网络连接失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
            } else if (msg.what == What_UserError) {
                Toast.makeText(getActivity(), "用户状态异常，请重新登录！", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private ChangePwdDialog changePwdDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_account, container, false);
        initView();
        setData();
        return root;
    }

    private void setData() {
        //设置背景磨砂效果
        Glide.with(this).load(R.drawable.backpic)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 3)).centerCrop())
                .into(mHBack);
        //设置圆形图像
        Glide.with(this).load(R.drawable.headpic)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.headpic))
                .into(mHHead);

        //设置整个item的点击事件
        mHouseId.setItemClickListener(new ItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
        mPass.setItemClickListener(new ItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                //Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                changePwdDialog = new ChangePwdDialog();
                changePwdDialog.setTargetFragment(Account.this, REQUEST_CODE);
                changePwdDialog.show(getFragmentManager(), "ChangePwd");
            }
        });
        mPhone.setItemClickListener(new ItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
        mAbout.setItemClickListener(new ItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initView() {
        //顶部头像控件
        mHBack = root.findViewById(R.id.h_back);
        mHHead = root.findViewById(R.id.h_head);
        TextView mUserName = root.findViewById(R.id.user_name);
        TextView mUserVal = root.findViewById(R.id.user_val);
        //下面item控件
        mHouseId = root.findViewById(R.id.user_hid);
        mPass = root.findViewById(R.id.user_pass);
        mPhone = root.findViewById(R.id.user_phone);
        mAbout = root.findViewById(R.id.about);
        mUserName.setText(app_uid);
        mUserVal.setText(app_tel);
        mHouseId.setRightDesc(app_hid);
        mPhone.setRightDesc(app_tel);
        mAbout.setRightDesc(getLocalVersion(root.getContext()));
    }

    public static String getLocalVersion(Context ctx) {
        String localVersion = "0.0.0";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
            Log.d("TAG", "当前版本：" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            try {
                String originPwd = data.getStringExtra("OriginPassword");
                String newPwd = data.getStringExtra("NewPassword");
                changePwdDialog.dismiss();
                Thread aLoginThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        int ChangeRes = TChangePwd.ChangeUserPwd(getActivity(), MainApplication.app_uid, originPwd, newPwd);
                        Message msg = new Message();
                        if (ChangeRes == 0)
                            msg.what = What_ConnectFailed;
                        else if (ChangeRes == 1)
                            msg.what = What_ChangeSuccess;
                        else if (ChangeRes == 2)
                            msg.what = What_ChangeFailed;
                        else if (ChangeRes == 3)
                            msg.what = What_UserError;
                        mHandler.sendMessage(msg);
                    }
                };
                aLoginThread.setDaemon(true);
                aLoginThread.start();
            } catch (Exception er) {
                Log.e(TAG, "acLogin: " + er.getMessage());
            }

        }
    }
}
