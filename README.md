# PayLib [![](https://www.jitpack.io/v/976112643/PayLib.svg)](https://www.jitpack.io/#976112643/PayLib)

微信和支付宝支付sdk的封装,主要是简化微信配置以及统一起调逻辑.

**接入步骤:**

1. 在工程build.gradle中加入jitpack仓库

```
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

2. 添加依赖

```
dependencies {
	compile 'com.github.976112643:PayLib:1.0'
}
```

**使用说明:**
1. 发起支付动作

```java
    Intent intent = new Intent(this, WePaymentActivity.class);
    intent.putExtra(PAY_TYPE, PAY_TYPE_ALIPAY);//支付类型
    intent.putExtra(PAY_ORDER_CONTENT, aliOrderInfo);//支付信息
    startActivityForResult(intent, 0x09);
 ```

2. 监听结果回调

```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 0x09) {
            int intExtra = data.getIntExtra(RESULT_STATUS, 0);
            Toast.makeText(this, data.getStringExtra(RESULT_MSG), Toast.LENGTH_SHORT).show();
            if (intExtra == PAY_SUCCESS) {
                Log.d("WePayDemoActivity", "成功");
            } else {
                Log.d("WePayDemoActivity", "失败");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
```

3. 参数说明

`PAY_TYPE` 支付类型.目前可用的有: 支付宝支付 `PAY_TYPE_ALIPAY`,微信支付 `PAY_TYPE_WXPAY`

`PAY_ORDER_CONTENT` 支付信息. 订单信息拼接加签名后的那段数据.

`RESULT_STATUS` 支付状态. `PAY_SUCCESS` 支付成功, `PAY_FAILD` 支付失败.

`RESULT_MSG` 支付结果提示信息. 主要是支付宝的一些不同状态的提示信息.没有特别要求的话可以直接作为支付提示信息.内容有这几种: 支付成功,支付失败,支付取消,支付结果处理中,网络异常,支付失败



