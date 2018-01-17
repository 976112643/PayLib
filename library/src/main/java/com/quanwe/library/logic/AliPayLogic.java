package com.quanwe.library.logic;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * 阿里支付逻辑块
 * Created by WQ on 2017/11/24.
 */

public class AliPayLogic {
    private Activity context;
    public static final int SDK_PAY_FLAG = 1901;
    public static final int SDK_AUTH_FLAG = 1902;
    private Thread payThread;
    private Handler mHandler;
    private String orderInfo, authInfo;
    private EnvUtils.EnvEnum envEnum = EnvUtils.EnvEnum.ONLINE;
    public String TAG=getClass().getName();
    /**
     * @param context  s
     * @param mHandler 用于消息回调
     */
    public AliPayLogic(Activity context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;
    }

    /**
     * 启动支付 -
     *
     * @param orderInfo 拼接好的加签名的订单信息字符串
     * @param envEnum   支付环境-正式/沙箱
     */
    public void startAliPay(final String orderInfo, EnvUtils.EnvEnum envEnum) {
        EnvUtils.setEnv(envEnum);
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(context);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());
                if (payThread == null) return;
                PayResult payResult = new PayResult(result);
                /**
                 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                String resultStatus = payResult.getResultStatus();
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;

                msg.obj = resultStatus;
                mHandler.sendMessage(msg);
            }
        };

        payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 启动支付 - 该方法不会再使用通过builder构建的订单相关参数
     *
     * @param orderInfo 拼接好的加签名的订单信息字符串
     */
    public void startAliPay(final String orderInfo) {
        startAliPay(orderInfo, envEnum);
    }
    /**
     * 启动支付-用于参数通过builder构建,私钥由app端保存,不建议使用
     */
    public void startAliPay(){
        startAliPay(orderInfo,envEnum);
    }
    /**
     * 开始授权-用于参数通过builder构建,私钥由app端保存,不建议使用
     */
    public void startAliAuth() {
        startAliAuth(authInfo);
    }

    /**
     * 开始授权
     * @param authInfo 拼接好的授权信息字符串
     */
    public void startAliAuth(final String authInfo) {
        Runnable authRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造AuthTask 对象
                AuthTask authTask = new AuthTask(context);
                // 调用授权接口，获取授权结果
                Map<String, String> result = authTask.authV2(authInfo, true);
                AuthResult authResult = new AuthResult(result, true);
                String resultStatus = authResult.getResultStatus();
                Log.e(TAG,"authResult "+ String.valueOf(result));
                // 判断resultStatus 为“9000”且result_code
                // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                if (!TextUtils.equals(resultStatus, "9000")) {
                    authResult.setResultStatus("-9000");
                }
                Message msg = new Message();
                msg.what = SDK_AUTH_FLAG;
                msg.obj = authResult;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread authThread = new Thread(authRunnable);
        authThread.start();
    }
    /**
     * 销毁-资源释放
     */
    public void onDestory() {
        if (payThread != null) {
//            payThread.stop();
            payThread = null;
            mHandler.removeMessages(SDK_PAY_FLAG);
            mHandler = null;
            context = null;
        }
    }


    public static class Builder {
        AliPayLogic aliPayLogic;
        String app_id;
        String partner;
        String target_id;
        String total_amount;
        String subject;
        String body;
        String rsa_key;
        String notify_url;
        boolean rsa2 = true;
        private EnvUtils.EnvEnum envEnum= EnvUtils.EnvEnum.ONLINE;

        public Builder(Activity context, Handler mHandler) {
            aliPayLogic = new AliPayLogic(context, mHandler);
        }

        /**
         * 设置appid
         *
         * @param app_id
         * @return
         */
        public Builder setApp_id(String app_id) {
            this.app_id = app_id;
            return this;
        }

        /**
         * 设置支付金额
         *
         * @param total_amount
         * @return
         */
        public Builder setTotal_amount(String total_amount) {
            this.total_amount = total_amount;
            return this;
        }

        /**
         * 设置商品描述
         *
         * @param subject
         * @return
         */
        public Builder setTitle(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * 设置商品标题名称
         *
         * @param body
         * @return
         */
        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        /**
         * 设置是否使用rsa2签名,默认true
         *
         * @param rsa2
         * @return
         */
        public Builder setRsa2(boolean rsa2) {
            this.rsa2 = rsa2;
            return this;
        }

        /**
         * 设置私钥
         *
         * @param rsa_key
         * @return
         */
        public Builder setRsa_key(String rsa_key) {
            this.rsa_key = rsa_key;
            return this;
        }

        /**
         * 设置异步通知地址
         *
         * @param notify_url
         * @return
         */
        public Builder setNotify_url(String notify_url) {
            this.notify_url = notify_url;
            return this;
        }

        /**
         * 设置合作伙伴PID
         * @param partner
         * @return
         */
        public Builder setPartner(String partner) {
            this.partner = partner;
            return this;
        }

        public Builder setTarget_id(String target_id) {
            this.target_id = target_id;
            return this;
        }

        /**
         * 设置支付模式-沙箱/正式环境,默认沙箱
         *
         * @param envEnum 支付模式枚举
         * @return
         */
        public Builder setEnvEnum(EnvUtils.EnvEnum envEnum) {
            this.envEnum = envEnum;
            return this;
        }

        public AliPayLogic createPay() {
            Map<String, String> params = buildOrderParamMap(app_id, total_amount, subject, body, notify_url, rsa2);
            String orderParam = buildOrderParam(params);
            String sign = getSign(params, rsa_key, rsa2);
            aliPayLogic.envEnum = envEnum;
            aliPayLogic.orderInfo = orderParam + "&" + sign;
            return aliPayLogic;
        }

        public AliPayLogic createAuth() {
            Map<String, String> authInfoMap = buildAuthInfoMap(partner, app_id, target_id == null ? partner : target_id, true);
            String info = buildOrderParam(authInfoMap);
            String sign = getSign(authInfoMap, rsa_key, rsa2);
            aliPayLogic.envEnum = envEnum;
            aliPayLogic.authInfo =  info + "&" + sign;
            return aliPayLogic;
        }

        /**
         * 构造支付订单参数列表
         *
         * @param app_id
         * @return
         */
        private static Map<String, String> buildOrderParamMap(String app_id, String total_amount, String subject, String body, String notify_url, boolean rsa2) {
            Map<String, String> keyValues = new HashMap<String, String>();
            keyValues.put("app_id", app_id);
            keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"" + total_amount + "\",\"subject\":\"" + subject + "\",\"body\":\"" + body + "\",\"out_trade_no\":\"" + getOutTradeNo() + "\"}");
            keyValues.put("charset", "utf-8");
            keyValues.put("method", "alipay.trade.app.pay");
            keyValues.put("notify_url", notify_url);
            keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");
            keyValues.put("timestamp", getTimestamp());
            keyValues.put("version", "1.0");
            return keyValues;
        }

        /**
         * 构造授权参数列表
         *
         * @param pid
         * @param app_id
         * @param target_id
         * @return
         */
        private static Map<String, String> buildAuthInfoMap(String pid, String app_id, String target_id, boolean rsa2) {
            Map<String, String> keyValues = new HashMap<String, String>();
            // 商户签约拿到的app_id，如：2013081700024223
            keyValues.put("app_id", app_id);

            // 商户签约拿到的pid，如：2088102123816631
            keyValues.put("pid", pid);

            // 服务接口名称， 固定值
            keyValues.put("apiname", "com.alipay.account.auth");

            // 商户类型标识， 固定值
            keyValues.put("app_name", "mc");

            // 业务类型， 固定值
            keyValues.put("biz_type", "openservice");

            // 产品码， 固定值
            keyValues.put("product_id", "APP_FAST_LOGIN");

            // 授权范围， 固定值
            keyValues.put("scope", "kuaijie");

            // 商户唯一标识，如：kkkkk091125
            keyValues.put("target_id", target_id);

            // 授权类型， 固定值
            keyValues.put("auth_type", "AUTHACCOUNT");

            // 签名类型
            keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");

            return keyValues;
        }

        private static String getTimestamp() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return simpleDateFormat.format(Calendar.getInstance().getTime());
        }

        /**
         * 构造支付订单参数信息
         *
         * @param map 支付订单参数
         * @return
         */
        private static String buildOrderParam(Map<String, String> map) {
            List<String> keys = new ArrayList<String>(map.keySet());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keys.size() - 1; i++) {
                String key = keys.get(i);
                String value = map.get(key);
                sb.append(buildKeyValue(key, value, true));
                sb.append("&");
            }

            String tailKey = keys.get(keys.size() - 1);
            String tailValue = map.get(tailKey);
            sb.append(buildKeyValue(tailKey, tailValue, true));

            return sb.toString();
        }

        /**
         * 拼接键值对
         *
         * @param key
         * @param value
         * @param isEncode
         * @return
         */
        private static String buildKeyValue(String key, String value, boolean isEncode) {
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append("=");
            if (isEncode) {
                try {
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    sb.append(value);
                }
            } else {
                sb.append(value);
            }
            return sb.toString();
        }

        /**
         * 对支付参数信息进行签名
         *
         * @param map 待签名授权信息
         * @return
         */
        private static String getSign(Map<String, String> map, String rsaKey, boolean rsa2) {
            List<String> keys = new ArrayList<String>(map.keySet());
            // key排序
            Collections.sort(keys);

            StringBuilder authInfo = new StringBuilder();
            for (int i = 0; i < keys.size() - 1; i++) {
                String key = keys.get(i);
                String value = map.get(key);
                authInfo.append(buildKeyValue(key, value, false));
                authInfo.append("&");
            }

            String tailKey = keys.get(keys.size() - 1);
            String tailValue = map.get(tailKey);
            authInfo.append(buildKeyValue(tailKey, tailValue, false));

            String oriSign = SignUtils.sign(authInfo.toString(), rsaKey, rsa2);
            String encodedSign = "";

            try {
                encodedSign = URLEncoder.encode(oriSign, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "sign=" + encodedSign;
        }

        /**
         * 要求外部订单号必须唯一。
         *
         * @return
         */
        private static String getOutTradeNo() {
            SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
            Date date = new Date();
            String key = format.format(date);
            Random r = new Random();
            key = key + r.nextInt();
            key = key.substring(0, 15);
            return key;
        }
    }


    /**
     * 支付结果实体
     */
    public class PayResult {
        private String resultStatus;
        private String result;
        private String memo;

        public PayResult(Map<String, String> rawResult) {
            if (rawResult == null) {
                return;
            }

            for (String key : rawResult.keySet()) {
                if (TextUtils.equals(key, "resultStatus")) {
                    resultStatus = rawResult.get(key);
                } else if (TextUtils.equals(key, "result")) {
                    result = rawResult.get(key);
                } else if (TextUtils.equals(key, "memo")) {
                    memo = rawResult.get(key);
                }
            }
        }

        @Override
        public String toString() {
            return "resultStatus={" + resultStatus + "};memo={" + memo
                    + "};result={" + result + "}";
        }

        /**
         * @return the resultStatus
         */
        public String getResultStatus() {
            return resultStatus;
        }

        /**
         * @return the memo
         */
        public String getMemo() {
            return memo;
        }

        /**
         * @return the result
         */
        public String getResult() {
            return result;
        }
    }

    /**
     * 授权结果实体
     */
    public class AuthResult {

        private String resultStatus;
        private String result;
        private String memo;
        private String resultCode;
        private String authCode;
        private String alipayOpenId;
        private String user_id;

        public AuthResult(Map<String, String> rawResult, boolean removeBrackets) {
            if (rawResult == null) {
                return;
            }

            for (String key : rawResult.keySet()) {
                if (TextUtils.equals(key, "resultStatus")) {
                    resultStatus = rawResult.get(key);
                } else if (TextUtils.equals(key, "result")) {
                    result = rawResult.get(key);
                } else if (TextUtils.equals(key, "memo")) {
                    memo = rawResult.get(key);
                }
            }

            String[] resultValue = result.split("&");
            for (String value : resultValue) {
                if (value.startsWith("alipay_open_id")) {
                    alipayOpenId = removeBrackets(getValue("alipay_open_id=", value), removeBrackets);
                    continue;
                }
                if(value.startsWith("user_id")){
                    user_id = removeBrackets(getValue("user_id=", value), removeBrackets);
                }
                if (value.startsWith("auth_code")) {
                    authCode = removeBrackets(getValue("auth_code=", value), removeBrackets);
                    continue;
                }
                if (value.startsWith("result_code")) {
                    resultCode = removeBrackets(getValue("result_code=", value), removeBrackets);
                    continue;
                }
            }

        }

        private String removeBrackets(String str, boolean remove) {
            if (remove) {
                if (!TextUtils.isEmpty(str)) {
                    if (str.startsWith("\"")) {
                        str = str.replaceFirst("\"", "");
                    }
                    if (str.endsWith("\"")) {
                        str = str.substring(0, str.length() - 1);
                    }
                }
            }
            return str;
        }

        @Override
        public String toString() {
            return "resultStatus={" + resultStatus + "};memo={" + memo + "};result={" + result + "}";
        }

        private String getValue(String header, String data) {
            return data.substring(header.length(), data.length());
        }

        /**
         * @return the resultStatus
         */
        public String getResultStatus() {
            return resultStatus;
        }

        /**
         * @return the memo
         */
        public String getMemo() {
            return memo;
        }

        /**
         * @return the result
         */
        public String getResult() {
            return result;
        }

        /**
         * @return the resultCode
         */
        public String getResultCode() {
            return resultCode;
        }

        /**
         * @return the authCode
         */
        public String getAuthCode() {
            return authCode;
        }

        /**
         * @return the alipayOpenId
         */
        public String getAlipayOpenId() {
            return alipayOpenId;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setResultStatus(String resultStatus) {
            this.resultStatus = resultStatus;
        }
    }
    /**
     * 签名工具
     */
    public static class SignUtils {

        private static final String ALGORITHM = "RSA";

        private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

        private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

        private static final String DEFAULT_CHARSET = "UTF-8";

        private static String getAlgorithms(boolean rsa2) {
            return rsa2 ? SIGN_SHA256RSA_ALGORITHMS : SIGN_ALGORITHMS;
        }

        public static String sign(String content, String privateKey, boolean rsa2) {
            try {
                PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
                        Base64.decode(privateKey));
                KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
                PrivateKey priKey = keyf.generatePrivate(priPKCS8);

                java.security.Signature signature = java.security.Signature
                        .getInstance(getAlgorithms(rsa2));

                signature.initSign(priKey);
                signature.update(content.getBytes(DEFAULT_CHARSET));

                byte[] signed = signature.sign();

                return Base64.encode(signed);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    /**
     * base64加密
     */
    public static final class Base64 {

        private static final int BASELENGTH = 128;
        private static final int LOOKUPLENGTH = 64;
        private static final int TWENTYFOURBITGROUP = 24;
        private static final int EIGHTBIT = 8;
        private static final int SIXTEENBIT = 16;
        private static final int FOURBYTE = 4;
        private static final int SIGN = -128;
        private static char PAD = '=';
        private static byte[] base64Alphabet = new byte[BASELENGTH];
        private static char[] lookUpBase64Alphabet = new char[LOOKUPLENGTH];

        static {
            for (int i = 0; i < BASELENGTH; ++i) {
                base64Alphabet[i] = -1;
            }
            for (int i = 'Z'; i >= 'A'; i--) {
                base64Alphabet[i] = (byte) (i - 'A');
            }
            for (int i = 'z'; i >= 'a'; i--) {
                base64Alphabet[i] = (byte) (i - 'a' + 26);
            }

            for (int i = '9'; i >= '0'; i--) {
                base64Alphabet[i] = (byte) (i - '0' + 52);
            }

            base64Alphabet['+'] = 62;
            base64Alphabet['/'] = 63;

            for (int i = 0; i <= 25; i++) {
                lookUpBase64Alphabet[i] = (char) ('A' + i);
            }

            for (int i = 26, j = 0; i <= 51; i++, j++) {
                lookUpBase64Alphabet[i] = (char) ('a' + j);
            }

            for (int i = 52, j = 0; i <= 61; i++, j++) {
                lookUpBase64Alphabet[i] = (char) ('0' + j);
            }
            lookUpBase64Alphabet[62] = (char) '+';
            lookUpBase64Alphabet[63] = (char) '/';

        }

        private static boolean isWhiteSpace(char octect) {
            return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
        }

        private static boolean isPad(char octect) {
            return (octect == PAD);
        }

        private static boolean isData(char octect) {
            return (octect < BASELENGTH && base64Alphabet[octect] != -1);
        }

        /**
         * Encodes hex octects into Base64
         *
         * @param binaryData Array containing binaryData
         * @return Encoded Base64 array
         */
        public static String encode(byte[] binaryData) {

            if (binaryData == null) {
                return null;
            }

            int lengthDataBits = binaryData.length * EIGHTBIT;
            if (lengthDataBits == 0) {
                return "";
            }

            int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
            int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
            int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1
                    : numberTriplets;
            char encodedData[] = null;

            encodedData = new char[numberQuartet * 4];

            byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

            int encodedIndex = 0;
            int dataIndex = 0;

            for (int i = 0; i < numberTriplets; i++) {
                b1 = binaryData[dataIndex++];
                b2 = binaryData[dataIndex++];
                b3 = binaryData[dataIndex++];

                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                        : (byte) ((b1) >> 2 ^ 0xc0);
                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                        : (byte) ((b2) >> 4 ^ 0xf0);
                byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6)
                        : (byte) ((b3) >> 6 ^ 0xfc);

                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
            }

            // form integral number of 6-bit groups
            if (fewerThan24bits == EIGHTBIT) {
                b1 = binaryData[dataIndex];
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                        : (byte) ((b1) >> 2 ^ 0xc0);
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[k << 4];
                encodedData[encodedIndex++] = PAD;
                encodedData[encodedIndex++] = PAD;
            } else if (fewerThan24bits == SIXTEENBIT) {
                b1 = binaryData[dataIndex];
                b2 = binaryData[dataIndex + 1];
                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                        : (byte) ((b1) >> 2 ^ 0xc0);
                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                        : (byte) ((b2) >> 4 ^ 0xf0);

                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2];
                encodedData[encodedIndex++] = PAD;
            }

            return new String(encodedData);
        }

        /**
         * Decodes Base64 data into octects
         *
         * @param encoded string containing Base64 data
         * @return Array containind decoded data.
         */
        public static byte[] decode(String encoded) {

            if (encoded == null) {
                return null;
            }

            char[] base64Data = encoded.toCharArray();
            // remove white spaces
            int len = removeWhiteSpace(base64Data);

            if (len % FOURBYTE != 0) {
                return null;// should be divisible by four
            }

            int numberQuadruple = (len / FOURBYTE);

            if (numberQuadruple == 0) {
                return new byte[0];
            }

            byte decodedData[] = null;
            byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
            char d1 = 0, d2 = 0, d3 = 0, d4 = 0;

            int i = 0;
            int encodedIndex = 0;
            int dataIndex = 0;
            decodedData = new byte[(numberQuadruple) * 3];

            for (; i < numberQuadruple - 1; i++) {

                if (!isData((d1 = base64Data[dataIndex++]))
                        || !isData((d2 = base64Data[dataIndex++]))
                        || !isData((d3 = base64Data[dataIndex++]))
                        || !isData((d4 = base64Data[dataIndex++]))) {
                    return null;
                }// if found "no data" just return null

                b1 = base64Alphabet[d1];
                b2 = base64Alphabet[d2];
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];

                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
            }

            if (!isData((d1 = base64Data[dataIndex++]))
                    || !isData((d2 = base64Data[dataIndex++]))) {
                return null;// if found "no data" just return null
            }

            b1 = base64Alphabet[d1];
            b2 = base64Alphabet[d2];

            d3 = base64Data[dataIndex++];
            d4 = base64Data[dataIndex++];
            if (!isData((d3)) || !isData((d4))) {// Check if they are PAD characters
                if (isPad(d3) && isPad(d4)) {
                    if ((b2 & 0xf) != 0)// last 4 bits should be zero
                    {
                        return null;
                    }
                    byte[] tmp = new byte[i * 3 + 1];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 3);
                    tmp[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                    return tmp;
                } else if (!isPad(d3) && isPad(d4)) {
                    b3 = base64Alphabet[d3];
                    if ((b3 & 0x3) != 0)// last 2 bits should be zero
                    {
                        return null;
                    }
                    byte[] tmp = new byte[i * 3 + 2];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 3);
                    tmp[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                    tmp[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                    return tmp;
                } else {
                    return null;
                }
            } else { // No PAD e.g 3cQl
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];
                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);

            }

            return decodedData;
        }

        /**
         * remove WhiteSpace from MIME containing encoded Base64 data.
         *
         * @param data the byte array of base64 data (with WS)
         * @return the new length
         */
        private static int removeWhiteSpace(char[] data) {
            if (data == null) {
                return 0;
            }

            // count characters that's not whitespace
            int newSize = 0;
            int len = data.length;
            for (int i = 0; i < len; i++) {
                if (!isWhiteSpace(data[i])) {
                    data[newSize++] = data[i];
                }
            }
            return newSize;
        }
    }


}
