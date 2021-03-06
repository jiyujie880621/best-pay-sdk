package com.lly835.bestpay.service.impl;

import com.lly835.bestpay.config.AlipayConfig;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.Signature;
import com.lly835.bestpay.service.impl.signatrue.AlipayAppSignatrueImpl;
import com.lly835.bestpay.utils.JsonUtil;
import com.lly835.bestpay.utils.MapUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝app支付
 * Created by null on 2017/2/14.
 */
public class AlipayAppServiceImpl implements BestPayService{

    private final static Logger logger = LoggerFactory.getLogger(AlipayAppServiceImpl.class);

    public PayResponse pay(PayRequest request) {

        logger.info("【支付宝PC端支付】request={}", JsonUtil.toJson(request));
        PayResponse response =  new PayResponse();
        response.setOrderId(request.getOrderId());
        response.setOrderAmount(request.getOrderAmount());

        //1. 封装参数
        Map<String, String> parameterMap = new HashMap<>();
        Map<String, Object> bizContentMap = new HashMap<>();

        bizContentMap.put("subject", request.getOrderName());
        bizContentMap.put("out_trade_no", request.getOrderId());
        bizContentMap.put("timeout_express", "30m");
        bizContentMap.put("total_amount", request.getOrderAmount());
        bizContentMap.put("product_code", "QUICK_MSECURITY_PAY");
        bizContentMap.put("body", "");

        parameterMap.put("app_id", AlipayConfig.getAppId());
        parameterMap.put("method", "alipay.trade.app.pay");
        parameterMap.put("charset", AlipayConfig.getInputCharset());
        parameterMap.put("sign_type", AlipayConfig.getSignType());
        parameterMap.put("timestamp", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        parameterMap.put("version", "1.0");
        parameterMap.put("notify_url", request.getNotifyUrl());
        parameterMap.put("biz_content", JsonUtil.toJson(bizContentMap));

        //2. 签名
        Signature signature = new AlipayAppSignatrueImpl();
        String sign = signature.sign(parameterMap);
        parameterMap.put("sign", sign);
        logger.debug("【支付宝Wap端支付】构造好的完整参数={}", JsonUtil.toJson(parameterMap));

        //3. 返回的结果
        String prePayParams = MapUtil.toUrlWithSortAndEncode(parameterMap) + "&sign=" + URLEncoder.encode(sign);
        response.setPrePayParams(prePayParams);
        logger.debug("【支付宝APP端支付】response={}", JsonUtil.toJson(response));
        logger.debug("prePayParams={}", prePayParams);

        return response;
    }

    @Override
    public PayResponse syncNotify(HttpServletRequest request) {

        //构造返回对象
        PayResponse response = new PayResponse();
        response.setOrderId(request.getParameter("out_trade_no"));
        response.setTradeNo(request.getParameter("trade_no"));

        return response;
    }
}
