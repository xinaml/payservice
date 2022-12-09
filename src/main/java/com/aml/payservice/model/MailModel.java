package com.aml.payservice.model;

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
public class MailModel {
    private String id;
    private String mail;
    private Date startTime;
    private Date endTime;
    private Integer isValid;

}
