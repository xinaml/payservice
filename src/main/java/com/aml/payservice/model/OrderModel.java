package com.aml.payservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author liguiqin
 * @date 2020/8/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OrderModel {
    private String appId;
    private String bankType;
    private BigDecimal cashFee;
    private BigDecimal totalFee;
    private String openId;
    private String outTradeNo;
    private String timeEnd;
    private String tradeType;
    private String transactionId;
    private String returnCode;

}
