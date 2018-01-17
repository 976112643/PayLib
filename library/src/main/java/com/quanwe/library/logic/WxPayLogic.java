package com.quanwe.library.logic;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

/**
 * 微信支付逻辑
 * Created by WQ on 2017/12/6.
 */

public class WxPayLogic {
    private Activity context;
    public static final int SDK_PAY_FLAG = 1801;
    private Thread payThread;
    private Handler mHandler;
    private IWXAPI api;
    public static  final int STATE_DEF=100,STATE_PAYING=101,STATE_SUCCESS=102;//,STATE_FAIL=2;
    public static int pay_status=STATE_DEF;//未初始化, 0 起调 1 成功 2 失败
    public WxPayLogic(Activity context, String appId, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;
        api = WXAPIFactory.createWXAPI(context, null);
        api.registerApp(appId);
        pay_status=STATE_DEF;
    }

    /**
     * 启动支付 -
     *
     * @param json jsonObject对象
     */
    public void startWxPay(JSONObject json) {
        pay_status=STATE_PAYING;//
        Runnable payRunnable=new Runnable() {
            @Override
            public void run() {
                while (pay_status==STATE_PAYING){//等待支付结果
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(pay_status!=STATE_DEF) {
                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = pay_status == STATE_SUCCESS ? "9000" : String.valueOf(pay_status);//这里支付成功的结果跟支付宝保持一致
                    mHandler.sendMessage(msg);
                }
            }
        };
        payThread=new Thread(payRunnable);
        payThread.start();
        PayReq req = new PayReq();
        req.appId			= json.optString("appid");
        req.partnerId		= json.optString("partnerid");
        req.prepayId		= json.optString("prepayid");
        req.nonceStr		= json.optString("noncestr");
        req.timeStamp		= json.optString("timestamp");
        req.packageValue	= json.optString("package");
        req.sign			= json.optString("sign");
        req.extData			= "app data"; // optional
//        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }

    public static  void setStatus(int status){
        pay_status=status;
    }

    /**
     * 销毁-资源释放
     */
    public void onDestory() {
        if (payThread != null) {
            payThread = null;
            mHandler.removeMessages(SDK_PAY_FLAG);
            mHandler = null;
            context = null;
        }
    }
}
