package com.hhuc.smartdoor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hhuc.smartdoor.modules.login.*;
import com.hhuc.smartdoor.modules.register.TRegister;
import com.google.gson.Gson;
import com.mob.MobSDK;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";
    public final static int What_RegisterSuccess = 1000;
    public final static int What_RegisterFailed = 1010;
    public final static int What_RegisterClash = 1015;
    public final static int What_LoginSuccess = 1020;
    public final static int What_LoginUidErr = 1030;
    public final static int What_LoginPwdErr = 1035;
    public final static int What_NetworkErr = 1040;
    public final static int What_CodeSendSuccess = 2000;
    public final static int What_CodeSendFailed = 2010;
    public final static int What_CodeVerifySuccess = 2020;
    public final static int What_CodeVerifyFailed = 2030;
    int mViewStatus = 0;  //0-login ; 1-register;
    private boolean ready;//判断短信验证码服务注册事件是否成功
    private long mExitTime;//声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private final mHandler mhandler = new mHandler(this);
    EditText mEditLoginUid;
    EditText mEditLoginPwd;
    EditText mEditRegisterUid;
    EditText mEditRegisterHid;
    EditText mEditRegisterPwd0;
    EditText mEditRegisterPwd1;
    EditText mEditRegisterTel;
    EditText mEditRegisterCheckCode;

    Button mBtnRegister;
    Button mBtnLogin;
    Button mBtnRegister2Login;
    Button mBtnLogin2Register;
    Button mBtnGetCode;

    LinearLayout frmLogin;
    LinearLayout frmRegister;

    TLoginVideo video_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initActivity();
    }

    //返回重启加载
    @Override
    protected void onRestart() {
        super.onRestart();
        initVideoView();
    }

    //防止锁屏或者切出的时候，音乐在播放
    @Override
    protected void onStop() {
        super.onStop();
        video_view.stopPlayback();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (ready) {
            // 销毁回调监听接口
            SMSSDK.unregisterAllEventHandler();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static class mHandler extends Handler {
        WeakReference<MainActivity> weakReference;

        mHandler(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity != null) {
                if (msg.what == What_RegisterSuccess) {
                    Toast.makeText(mainActivity, "注册成功!", Toast.LENGTH_SHORT).show();
                    mainActivity.setViewStatus(0);
                } else if (msg.what == What_RegisterClash) {
                    Toast.makeText(mainActivity, "用户名已被注册，请更换再试!", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_RegisterFailed) {
                    Toast.makeText(mainActivity, "注册失败,请检查网络连接!", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_LoginSuccess) {
                    //@SuppressWarnings("unchecked")
                    //Map<String, String> loginResults = (Map<String, String>) msg.obj;
                    Toast.makeText(mainActivity, "登录成功!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mainActivity, NavigationActivity.class);
                    String userInfoJson = new Gson().toJson(msg.obj);
                    intent.putExtra("userInfo", userInfoJson);
                    mainActivity.startActivity(intent);
                    mainActivity.finish();
                } else if (msg.what == What_LoginPwdErr) {
                    Toast.makeText(mainActivity, "密码错误！", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_LoginUidErr) {
                    Toast.makeText(mainActivity, "用户名不存在, 请先注册！", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_NetworkErr) {
                    Toast.makeText(mainActivity, "网络连接失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_CodeSendSuccess) {
                    Toast.makeText(mainActivity, "验证码已发送，请勿重复点击!", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_CodeSendFailed) {
                    Toast.makeText(mainActivity, "验证码获取失败，请检查网络连接!", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_CodeVerifySuccess) {
                    Toast.makeText(mainActivity, "验证码正确!", Toast.LENGTH_SHORT).show();
                } else if (msg.what == What_CodeVerifyFailed) {
                    Toast.makeText(mainActivity, "验证码错误，若多次错误请重新获取!", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(MainActivity.this, "Handler:" + msg.what, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerSDK() {
        MobSDK.init(this);
        MobSDK.submitPolicyGrantResult(true, null);
        EventHandler eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == 2)
                        msg.what = What_CodeSendSuccess;
                    else if (event == 3) {
                        msg.what = What_CodeVerifySuccess;
                        acRegister();
                    }
                } else {
                    if (event == 2)
                        msg.what = What_CodeSendFailed;
                    else if (event == 3)
                        msg.what = What_CodeVerifyFailed;
                    ((Throwable) data).printStackTrace();
                }
                mhandler.sendMessage(msg);
            }
        };
        // 注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
        ready = true;
    }

    void initActivity() {
        try {
            mEditLoginUid = this.findViewById(R.id.editLoginUid);
            mEditLoginPwd = this.findViewById(R.id.editLoginPwd);
            mEditRegisterUid = this.findViewById(R.id.editRegisterUid);
            mEditRegisterHid = this.findViewById(R.id.editRegisterHid);
            mEditRegisterPwd0 = this.findViewById(R.id.editRegisterPwd0);
            mEditRegisterPwd1 = this.findViewById(R.id.editRegisterPwd1);
            mEditRegisterTel = this.findViewById(R.id.editRegisterTel);
            mEditRegisterCheckCode = this.findViewById(R.id.editRegisterCheck);
            mBtnRegister = this.findViewById(R.id.btnRegister);
            mBtnLogin = this.findViewById(R.id.btnLogin);
            mBtnRegister2Login = this.findViewById(R.id.btnRegister2Login);
            mBtnLogin2Register = this.findViewById(R.id.btnLogin2Register);
            mBtnGetCode = this.findViewById(R.id.btnGetCode);
            frmLogin = this.findViewById(R.id.frameLogin);
            frmRegister = this.findViewById(R.id.frameRegister);
            initVideoView();
            mBtnRegister.setOnClickListener(this::onBtnClick);
            mBtnLogin.setOnClickListener(this::onBtnClick);
            mBtnLogin2Register.setOnClickListener(this::onBtnClick);
            mBtnRegister2Login.setOnClickListener(this::onBtnClick);
            mBtnGetCode.setOnClickListener(this::onBtnClick);
            registerSDK();
        } catch (Exception er) {
            Log.e(TAG, "init: " + er.getMessage());
        }
    }

    void initVideoView() {
        //找VideoView控件
        video_view = findViewById(R.id.login_video);
        //加载视频文件
        video_view.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        //播放
        video_view.start();
        //循环播放
        video_view.setOnCompletionListener(mediaPlayer -> video_view.start());
    }

    void onBtnClick(View v) {
        try {
            int vId = v.getId();
            if (vId == R.id.btnRegister) {
                //Enable CheckCodeVerify:
                final String uid = this.mEditRegisterUid.getText().toString();
                final String hid = this.mEditRegisterHid.getText().toString();
                final String pwd0 = this.mEditRegisterPwd0.getText().toString();
                final String pwd1 = this.mEditRegisterPwd1.getText().toString();
                final String tel = this.mEditRegisterTel.getText().toString();
                final String checkCode = this.mEditRegisterCheckCode.getText().toString();
                if (uid.isEmpty())
                    Toast.makeText(this, "用户名不能为空!", Toast.LENGTH_SHORT).show();
                else if (hid.isEmpty())
                    Toast.makeText(this, "户号不能为空!", Toast.LENGTH_SHORT).show();
                else if (pwd0.isEmpty())
                    Toast.makeText(this, "密码不能为空!", Toast.LENGTH_SHORT).show();
                else if (!pwd1.equals(pwd0))
                    Toast.makeText(this, "两次输入密码不一致!", Toast.LENGTH_SHORT).show();
                else if (tel.isEmpty())
                    Toast.makeText(this, "手机号码不能为空!", Toast.LENGTH_SHORT).show();
                else if (checkCode.isEmpty())
                    Toast.makeText(this, "验证码不能为空!", Toast.LENGTH_SHORT).show();
                else
                    SMSSDK.submitVerificationCode("86", tel, checkCode);
                //Enable CheckCodeVerify：SMSSDK.submitVerificationCode("86", tel, checkCode);
                //Disable CheckCodeVerify: acRegister();
            } else if (vId == R.id.btnLogin) {
                acLogin();
            } else if (vId == R.id.btnRegister2Login) {
                setViewStatus(0);
            } else if (vId == R.id.btnLogin2Register) {
                setViewStatus(1);
            } else if (vId == R.id.btnGetCode) {
                final String tel = this.mEditRegisterTel.getText().toString();
                if (tel.isEmpty())
                    Toast.makeText(this, "手机号码不能为空!", Toast.LENGTH_SHORT).show();
                else {
                    //定义需要匹配的正则表达式的规则
                    String REGEX_MOBILE_SIMPLE = "[1][358]\\d{9}";
                    //把正则表达式的规则编译成模板
                    Pattern pattern = Pattern.compile(REGEX_MOBILE_SIMPLE);
                    //把需要匹配的字符给模板匹配，获得匹配器
                    Matcher matcher = pattern.matcher(tel);
                    // 通过匹配器查找是否有该字符，不可重复调用重复调用matcher.find()
                    if (matcher.find()) {//匹配手机号是否存在
                        Thread aGetCodeThread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                SMSSDK.getVerificationCode("86", tel);
                            }
                        };
                        aGetCodeThread.setDaemon(true);
                        aGetCodeThread.start();

                    } else {
                        Toast.makeText(this, "手机号格式错误!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception er) {
            Log.e(TAG, "onBtnClick: " + er.getMessage());
        }
    }

    void acLogin() {
        try {
            final String uid = this.mEditLoginUid.getText().toString();
            final String pwd = this.mEditLoginPwd.getText().toString();

            if (uid.isEmpty())
                Toast.makeText(this, "用户名不能为空!", Toast.LENGTH_SHORT).show();
            else if (pwd.isEmpty())
                Toast.makeText(this, "密码不能为空!", Toast.LENGTH_SHORT).show();
            else {
                Thread aLoginThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> loginRes = TLogin.loginUser(MainActivity.this, uid, pwd);
                        Message msg = new Message();
                        if (loginRes != null)
                            if (Objects.equals(loginRes.get("status"), "0")) {
                                msg.what = What_LoginUidErr;
                            } else if (Objects.equals(loginRes.get("status"), "1")) {
                                msg.what = What_LoginSuccess;
                                msg.obj = loginRes;
                            } else if (Objects.equals(loginRes.get("status"), "2")) {
                                msg.what = What_LoginPwdErr;
                            } else
                                msg.what = What_NetworkErr;
                        MainActivity.this.mhandler.sendMessage(msg);
                    }
                };
                aLoginThread.setDaemon(true);
                aLoginThread.start();
            }
        } catch (Exception er) {
            Log.e(TAG, "acRegister: " + er.getMessage());
        }
    }

    void acRegister() {
        try {
            final String uid = this.mEditRegisterUid.getText().toString();
            final String hid = this.mEditRegisterHid.getText().toString();
            final String pwd0 = this.mEditRegisterPwd0.getText().toString();
            final String tel = this.mEditRegisterTel.getText().toString();
            Thread aRegisterThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    int registerRes = TRegister.registerUser(MainActivity.this, uid, hid, pwd0, tel);
                    Message msg = new Message();
                    if (registerRes == 0)
                        msg.what = What_RegisterFailed;
                    else if (registerRes == 1)
                        msg.what = What_RegisterSuccess;
                    else if (registerRes == 2)
                        msg.what = What_RegisterClash;
                    MainActivity.this.mhandler.sendMessage(msg);
                }
            };
            aRegisterThread.setDaemon(true);
            aRegisterThread.start();
        } catch (Exception er) {
            Log.e(TAG, "acRegister: " + er.getMessage());
        }
    }

    void setViewStatus(int viewStatus) {
        if (viewStatus != mViewStatus) {
            mViewStatus = viewStatus;
            if (mViewStatus == 0) {
                frmLogin.setVisibility(View.VISIBLE);
                frmRegister.setVisibility(View.GONE);
            } else {
                frmLogin.setVisibility(View.GONE);
                frmRegister.setVisibility(View.VISIBLE);
            }
        }
    }
}

