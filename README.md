# SmartDoor_Client

智能门禁 | 用户移动端 | Android APP | 可视化门铃 | WebRTC | 视频通话 | 远程开门

## 应用特点及食用指北

1. 用户注册及登录后端采用**Redis**实时性数据库管理，中台采用Python搭建，相关代码见[config/api.py](https://github.com/zys91/SmartDoor_Client/blob/main/config/api.py)需自行配置相关参数；

2. 用户注册中手机号短信验证操作采用**MobSDK**，需自行申请API_Key进行配置，教程详见<http://www.mob.com/wiki/detailed?wiki=SMSSDK_for_Android_kuaisujicheng>；

3. 视频通话采用WebRTC技术，使用的是即构的**Express-Video SDK**进行搭建，教程详见<https://doc-zh.zego.im/article/5416>

