package com.quanwe.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.quanwe.library.*;
import com.quanwe.library.BuildConfig;
import com.quanwe.library.ui.WePaymentActivity;

import static com.quanwe.library.ui.WePaymentActivity.PAY_ORDER_CONTENT;
import static com.quanwe.library.ui.WePaymentActivity.PAY_SUCCESS;
import static com.quanwe.library.ui.WePaymentActivity.PAY_TYPE;
import static com.quanwe.library.ui.WePaymentActivity.PAY_TYPE_ALIPAY;
import static com.quanwe.library.ui.WePaymentActivity.PAY_TYPE_WXPAY;
import static com.quanwe.library.ui.WePaymentActivity.RESULT_MSG;
import static com.quanwe.library.ui.WePaymentActivity.RESULT_STATUS;


/**
 * 支付功能demo ,
 * Created by WQ on 2018/1/17.
 */

public class WePayDemoActivity extends Activity implements View.OnClickListener {
    /**
     * 测试订单信息,因为订单有过期时间
     * 运行demo时,applicationId 和订单信息更换成自己的.
     */
    String aliOrderInfo = "alipay_sdk=alipay-sdk-php-20161101&app_id=2016042901347956&biz_content=%7B%22body%22%3A%22%22%2C%22subject%22%3A%22%5Cu751f%5Cu6d3b%5Cu7f34%5Cu8d39-%5Cu6c34%5Cu8d39-%5Cu65b0%5Cu90fd%5Cu4f1a2701+1014236996%22%2C%22out_trade_no%22%3A%222018011749511015%22%2C%22timeout_express%22%3A%2230m%22%2C%22total_amount%22%3A%220.01%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%7D&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=https%3A%2F%2Fwww.runbong.com%2FApi%2FNotify%2FAlipay%2Flife&sign_type=RSA2&timestamp=2018-01-17+13%3A52%3A49&version=1.0&sign=KcG83MmctgNY9FEDnJmck%2FftRkO7hJRmaqeYEQGPeX8jfARIVMJ1Zx3X57HJDJmXvtWdT16%2FNbshfnIvBP4At%2BUwSw7RqqjHE8hM6HukFZmldOcDxZZ7GTBtS9uV5TjVYG5wE9km0xmKRfoesiea0wtKz8jFFkIdM3r8MHXD95rATmIJ1DWCKGNPRU0x8CMPiR9g5nmwjPZuFsp3KjaCAYGI%2BfWr%2BRuMzcQIqDMcf4dwaPS%2B9Nn4MVsxETkfkNUKQNtyLtne1L49NKJue3siGK2YxnxCC6uMtf4SuJRHhWFSI32sp6J3U0NSaAr2D1wHyY2vVvy2p7Gd3LJAxiYcGg%3D%3D";
    String wxOrderInfo = "{\"appid\":\"wx1175598699555fcc\",\"partnerid\":\"1491532512\",\"package\":\"Sign=WXPay\",\"noncestr\":\"vcp42e391igi6ra5mcww3trfj5adbz1r\",\"timestamp\":1516171437,\"prepayid\":\"wx201801171443589ca8b3db560534732467\",\"sign\":\"2DA161C3216B943CA950CCD9EBEFF701\"}";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wepay);
        findViewById(R.id.btnAliPay).setOnClickListener(this);
        findViewById(R.id.btnWxPay).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, WePaymentActivity.class);
        switch (view.getId()) {
            case R.id.btnAliPay:
                //设置支付类型和支付信息,启动libary中的付款类,WePaymentActivity
                intent.putExtra(PAY_TYPE, PAY_TYPE_ALIPAY);
                intent.putExtra(PAY_ORDER_CONTENT, aliOrderInfo);
                startActivityForResult(intent, 0x09);
                break;
            case R.id.btnWxPay:
                intent.putExtra(PAY_TYPE, PAY_TYPE_WXPAY);
                intent.putExtra(PAY_ORDER_CONTENT, wxOrderInfo);
                startActivityForResult(intent, 0x09);
                break;
        }
    }

    //接收支付结果回调和结果信息
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
}
