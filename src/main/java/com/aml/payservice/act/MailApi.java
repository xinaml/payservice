package com.aml.payservice.act;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aml.payservice.common.Constant;
import com.aml.payservice.common.Result;
import com.aml.payservice.model.MailModel;
import com.aml.payservice.utils.DateUtil;
import com.aml.payservice.utils.JsonFileUtil;
import com.aml.payservice.vo.MailModelVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author liguiqin
 * @date 2022/12/9
 */
@RestController
@RequestMapping("/api")
@Log4j2
public class MailApi {
    private final String TABLE = "t_mail";

    @PostMapping("/addMail")
    public Result addMail(@RequestBody JSONObject ob) {
        String mail=ob.getString("mail");
        if (StringUtils.isBlank(mail)) {
            return Result.failure("邮箱不能为空");
        }
        List<MailModel> list = JsonFileUtil.readFile(TABLE, MailModel.class);
        for (MailModel bo : list) {
            if (bo.getMail().equals(mail)) {
                return Result.failure("邮箱已存在");
            }
        }
        MailModel mailModel = new MailModel();
        mailModel.setStartTime(new Date());
        mailModel.setEndTime(DateUtil.getMaxDate());
        mailModel.setMail(mail);
        mailModel.setIsValid(Constant.isValid.valid);
        mailModel.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        list.add(mailModel);
        JsonFileUtil.writeFile(TABLE, list);
        return Result.success();
    }

    @PostMapping("/delMail")
    public Result delMail(@RequestBody JSONObject ob) {
        String mail=ob.getString("mail");
        if (StringUtils.isBlank(mail)) {
            return Result.failure("邮箱不能为空");
        }
        List<MailModel> list = JsonFileUtil.readFile(TABLE, MailModel.class);
        MailModel mailModel = null;
        for (MailModel bo : list) {
            if (bo.getMail().equals(mail)) {
                mailModel = bo;
                break;
            }
        }
        if (mailModel == null) {
            return Result.failure("邮箱不存在");
        }
        list.remove(mailModel);
        JsonFileUtil.writeFile(TABLE,list);
        return Result.success();
    }

    @GetMapping("/listMail")
    public Result listMail() {
        List<MailModel> list = JsonFileUtil.readFile(TABLE, MailModel.class);
        List<MailModelVO> voList=new ArrayList<>();
        for(MailModel mailModel:list){
            MailModelVO vo=new MailModelVO();
            vo.setMail(mailModel.getMail());
            vo.setStartTime(DateUtil.formatDateTime(mailModel.getStartTime()));
            vo.setEndTime(DateUtil.formatDateTime(mailModel.getEndTime()));
            vo.setId(mailModel.getId());
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @PostMapping("/sync")
    public Result sync(@RequestBody JSONObject jsonObject) {
        sendMail(JSON.toJSONString(jsonObject));
        return Result.success();
    }

    private void sendMail(String content) {
        MailAccount account = new MailAccount();
        account.setFrom("testmail6688@163.com");
        account.setPass("AMDZLEMHXEAWTGGW");
        account.setUser("testmail6688@163.com");
        List<MailModel> list = JsonFileUtil.readFile(TABLE, MailModel.class);
        for (MailModel bo : list) {
            MailUtil.send(account, bo.getMail(), "通知", content, false);
        }
    }
}
