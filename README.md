# AutoInputAuthCode 自动填写验证码
打开你的app module中的build.gradle,添加依赖：
```
dependencies {
   ...
   compile 'tech.michaelx.authcode:library:1.0.1' // 只有这行是被添加的依赖
   ...
}
```
这是一个帮助Android开发者快速实现自动填写验证码的类库，只需要如下如下一行代码：
```java
AuthCode.getInstance().with(Context).config(CodeConfig).into(EditText);
```
1. 通过单例获取一个AuthCode对象;
2. 提供一个上下文对象给AuthCode，放心，我会妥善处理你的上下文;
3. 提供一个你的验证码特征描述;
4. 告诉AuthCode你想将验证码写入哪个EditText.

搞定，收工！

# 效果图
// TODO


# 注意事项
自动填写验证码需要读取短信权限，请在清单中添加权限：
```xml
<uses-permission android:name="android.permission.READ_SMS"/>
<uses-permission android:name="android.permission.READ_SMS"/>
```

由于读取短信在API 23（Android 6.0）上权限级别是**dangerous**。所以还需要动态申请权限，但是申请权限需要依赖于Activity或者Fragment中的onRequestPermissionsResult()回调，所以需要开发者自己实现。可参考中sample的代码。
