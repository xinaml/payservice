package com.aml.payservice.model;

/**
 * @author liguiqin
 * @date 2020/8/28
 */
public class OldOrderModel {
    public OldOrderModel( ) {
    }
    public OldOrderModel(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    private String outTradeNo;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }
}
