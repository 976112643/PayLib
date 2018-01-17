package com.quanwe.library.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quanwe.library.logic.WxPayLogic;
import com.quanwe.library.utils.Config;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import static com.quanwe.library.logic.WxPayLogic.STATE_SUCCESS;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	// APP_ID 替换为你的应用从官方网站申请到的合法appId
	private static final String TAG = "WXPayEntryActivity";
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	api = WXAPIFactory.createWXAPI(this, Config.getInstance().getAppID(this));
		if (!api.handleIntent(getIntent(), this)) {
			finish();
		}
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (!api.handleIntent(intent, this)) {
			finish();
		}
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
		WxPayLogic.setStatus(resp.errCode==0?STATE_SUCCESS:resp.errCode);
		finish();
	}
}