package com.quanwe.library.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.quanwe.library.R;
import com.quanwe.library.logic.AliPayLogic;
import com.quanwe.library.logic.WxPayLogic;
import com.quanwe.library.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 支付页
 * Created by WQ on 2018/1/17.
 */

public class WePaymentActivity extends Activity implements Handler.Callback {
    /**
     * 支付类型
     */
    public final static String PAY_TYPE = "com.quanwe.library.paytype";
    /**
     * 微信支付
     */
    public final static String PAY_TYPE_WXPAY = "com.quanwe.library.wxpay";
    /**
     * 支付宝支付
     */
    public final static String PAY_TYPE_ALIPAY = "com.quanwe.library.alipay";
    /**
     * 订单内容
     */
    public final static String PAY_ORDER_CONTENT = "com.quanwe.library.order.content";

    /**
     * 结果状态
     */
    public final static String RESULT_STATUS = "com.quanwe.library.result.staus";
    /**
     * 结果信息
     */
    public final static String RESULT_MSG = "com.quanwe.library.result.msg";

    /**
     * 支付成功
     */
    public final static int PAY_SUCCESS = 0x100;
    /**
     * 支付失败
     */
    public final static int PAY_FAILD = 0x101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String payType = getIntent().getStringExtra(PAY_TYPE);
        String orderContent = getIntent().getStringExtra(PAY_ORDER_CONTENT);
        if (TextUtils.isEmpty(payType) || TextUtils.isEmpty(orderContent)) {
            Toast.makeText(this, R.string.toast_argment_err, Toast.LENGTH_SHORT).show();
            return;
        }
        Handler handler = new Handler(this);
        if (PAY_TYPE_WXPAY.equals(payType)) {
            JSONObject json = createJsonObject(orderContent);
            Config.getInstance().setAPP_ID(json.optString("appid"));
            WxPayLogic wxPayLogic = new WxPayLogic(this, Config.getInstance().getAppID(this), handler);
            wxPayLogic.startWxPay(json);
        } else if(PAY_TYPE_ALIPAY.equals(payType)){
            AliPayLogic aliPayLogic = new AliPayLogic(this, handler);
            aliPayLogic.startAliPay(orderContent);
        }
    }

    JSONObject createJsonObject(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    @Override
    public boolean handleMessage(Message msg) {
        Intent result = new Intent();
        switch (msg.what) {
            case AliPayLogic.SDK_PAY_FLAG:
//                9000 	订单支付成功
//                8000 	正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
//                4000 	订单支付失败
//                5000 	重复请求
//                6001 	用户中途取消
//                6002 	网络连接出错
//                6004 	支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                if ("9000".equals(msg.obj)) {
                    result.putExtra(RESULT_STATUS, PAY_SUCCESS);
                    result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_success));
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                } else {
                    String resultStatus = msg.obj.toString();
                    switch (resultStatus) {
                        case "8000":
                        case "6004":
                            result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_paying));
                            break;
                        case "6001":
                            result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_cancel));
                            break;
                        case "6002":
                            result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_neterr));
                            break;
                            default:
                                result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_faild));
                                break;
                    }
                    result.putExtra(RESULT_STATUS, PAY_FAILD);

                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                }
                setResult(RESULT_OK,result);
                finish();
                break;
            case WxPayLogic.SDK_PAY_FLAG:
                if ("9000".equals(msg.obj)) {
                    result.putExtra(RESULT_STATUS, PAY_SUCCESS);
                    result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_success));
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                } else {
//                    -1 	错误 	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
//                    -2 	用户取消 	无需处理。发生场景：用户不支付了，点击取消，返回APP。
                    String resultStatus = msg.obj.toString();
                    switch (resultStatus) {
                        case "-2":
                            result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_cancel));
                            break;
                        default:
                            result.putExtra(RESULT_MSG, getResources().getString(R.string.toast_pay_faild));
                            break;
                    }
                    result.putExtra(RESULT_STATUS, PAY_FAILD);
                }
                setResult(RESULT_OK,result);
                finish();
                break;

        }
        return false;
    }
}
