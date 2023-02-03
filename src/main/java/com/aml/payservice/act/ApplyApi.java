package com.aml.payservice.act;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aml.payservice.common.Result;
import com.aml.payservice.model.ApplyModel;
import com.aml.payservice.utils.JsonFileUtil;
import com.aml.payservice.vo.ApplyModelVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author liguiqin
 * @date 2023/2/3
 */
@RestController
@RequestMapping("/api")
@Log4j2
public class ApplyApi {
    private final String TABLE = "t_apply";

    @PostMapping("/addUrl")
    public Result addUrl(@RequestBody JSONObject ob) {
        String url = ob.getString("url");
        if (StringUtils.isBlank(url)) {
            return Result.failure("url不能为空");
        }
        List<ApplyModel> list = JsonFileUtil.readFile(TABLE, ApplyModel.class);
        for (ApplyModel bo : list) {
            if (bo.getUrl().equals(url)) {
                return Result.failure("url已存在");
            }
        }
        ApplyModel applyModel = new ApplyModel();
        applyModel.setUrl(url);
        applyModel.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        list.add(applyModel);
        JsonFileUtil.writeFile(TABLE, list);
        return Result.success();
    }

    @PostMapping("/delUrl")
    public Result delUrl(@RequestBody JSONObject ob) {
        String url = ob.getString("url");
        if (StringUtils.isBlank(url)) {
            return Result.failure("url不能为空");
        }
        List<ApplyModel> list = JsonFileUtil.readFile(TABLE, ApplyModel.class);
        ApplyModel applyModel = null;
        for (ApplyModel bo : list) {
            if (bo.getUrl().equals(url)) {
                applyModel = bo;
                break;
            }
        }
        if (applyModel == null) {
            return Result.failure("url不存在");
        }
        list.remove(applyModel);
        JsonFileUtil.writeFile(TABLE, list);
        return Result.success();
    }

    @GetMapping("/listUrl")
    public Result listUrl() {
        List<ApplyModel> list = JsonFileUtil.readFile(TABLE, ApplyModel.class);
        List<ApplyModelVO> voList = new ArrayList<>();
        for (ApplyModel mailModel : list) {
            ApplyModelVO vo = new ApplyModelVO();
            vo.setId(mailModel.getId());
            vo.setUrl(mailModel.getUrl());
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @PostMapping("/send")
    public Result sync(@RequestBody JSONObject jsonObject) {
        Map<String, String> resultMap = new HashMap<>();
        List<ApplyModel> list = JsonFileUtil.readFile(TABLE, ApplyModel.class);
        String content=JSON.toJSONString(jsonObject);
        content=content.replaceAll(" ","");
        log.info("接受到转发的参数为:"+ content);
        for (ApplyModel mailModel : list) {
            try {
                String result = HttpUtil.post(mailModel.getUrl(), content);
                resultMap.put(mailModel.getUrl(), result);
                log.info("请求结果:"+result);
            }catch (Exception e){
                log.error("请求失败:"+mailModel.getUrl());

            }
        }
        return Result.success(resultMap);
    }

    public static void main(String[] args) {
        String content="{\"posSide\":\"short\",\"instId\":\"RVNUSDT.P-USDT-SWAP\",\"cprice\":0.02944,\"oprice\":0.02944}";
        String result = HttpUtil.post("http://284730cl8.goho.co:34590/api/OKX/TradingViewMsg/Order", content);
        String result2 = HttpUtil.post("http://119.29.121.235:8008/api/OKX/TradingViewMsg/Order", content);
        System.out.println(result);
        System.out.println(result2);
    }
}
