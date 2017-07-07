# AutoInputAuthCode
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[ ![Download](https://api.bintray.com/packages/xiong-it/AndroidRepo/AutoInputAuthCode/images/download.svg) ](https://bintray.com/xiong-it/AndroidRepo/AutoInputAuthCode/_latestVersion)  
Android开发中自动填写验证码功能库。  
相关博客：  
[AutoInpuAuthCode使用介绍](http://blog.csdn.net/xiong_it/article/details/71451922)
[Android开发:实现APP自动填写注册验证码功能](http://blog.csdn.net/xiong_it/article/details/50997084)

# Compile
打开你的app module中的build.gradle,添加依赖：
```groovy
compile 'tech.michaelx.authcode:authcode:1.0.1'
```

# Sample
示例代码如下：
```java
CodeConfig config = new CodeConfig.Builder()
                        .codeLength(4) // 设置验证码长度
                        .smsFromStart(133) // 设置验证码发送号码前几位数字
                        //.smsFrom(1690123456789) // 如果验证码发送号码固定，则可以设置验证码发送完整号码
                        .smsBodyStartWith("百度科技") // 设置验证码短信开头文字
                        .smsBodyContains("验证码") // 设置验证码短信内容包含文字
                        .build();
AuthCode.getInstance().with(context).config(config).into(EditText);

@Override
protected void onDestroy() {
    super.onDestroy();
    // 防止未读取到验证码导致内存泄露，手动回收内存
    AuthCode.getInstance().onDestroy();
}
```
1. 通过单例获取一个AuthCode对象;
2. 提供一个上下文对象给AuthCode，放心，我会妥善处理你的上下文;
3. 提供一个你的验证码特征描述;
4. 告诉AuthCode你想将验证码写入哪个EditText.

搞定，收工！

# 效果图
这里模拟演示了一个自动填写验证码的过程
1. 点击获取验证码(使用另一个手机发送一个仿真验证码短信到该手机)
2. 接收到验证码
3. AutoInputAuthCode替你自动填写验证码  
![自动填写验证码演示](http://oler3nq5z.bkt.clouddn.com/authcode2.gif)


# 注意事项
自动填写验证码需要读取短信权限，请在清单中添加权限：
```xml
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
<uses-permission android:name="android.permission.READ_SMS"/>
```

由于读取短信在API 23（Android 6.0）上权限级别是**dangerous**。所以还需要动态申请权限，但是申请权限需要依赖于Activity或者Fragment中的onRequestPermissionsResult()回调，所以需要开发者自己实现。  
动态申请权限可参考中sample的代码。

# changelog
v1.0.1  
添加一个共有api防止未读取到验证码导致内存泄露  
完善demo  

v1.0.0  
实现短信验证码读取功能  
实现演示demo  
