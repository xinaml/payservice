package com.aml.payservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author liguiqin
 * @date 2022/12/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MailModelVO {
    private String id;
    private String mail;
    private String startTime;
    private String endTime;

}
