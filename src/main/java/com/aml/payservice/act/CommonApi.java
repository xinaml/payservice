package com.aml.payservice.act;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aml.payservice.model.OrderModel;
import com.aml.payservice.utils.FileUtil;
import com.aml.payservice.utils.MD5;
import com.aml.payservice.utils.QRcodeUtil;
import com.aml.payservice.utils.wx.MyConfig;
import com.aml.payservice.utils.wx.WXPay;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author liguiqin
 * @date 2020/8/1
 */
@Controller
@RequestMapping("api")
public class CommonApi {
    private Logger log = LoggerFactory.getLogger(CommonApi.class);
    private String tableName="order";
    @Value("${key}")
    private String key;
    @Value("${appId}")
    private String appId;
    @Value("${mchId}")
    private String mchId;
    @Value("${notifyUrl}")
    private String notifyUrl="http://re.luzhiai.com:8088/api/payNotify";
    @Value("${createIp}")
    private String createIp="http://re.luzhiai.com:8088/api/payNotify";
    /**
     * 微信统一下单
     * @param fee
     * @param body
     * @param outTradeNo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "pay")
    @ResponseBody
    public Map<String, String> pay(BigDecimal fee,String body,String outTradeNo)throws Exception{
        if(outTradeNo==null ||outTradeNo.length()==0){
            throw new Exception("fee订单金额不能为空") ;
        }
        if(body==null ||body.length()==0){
            throw new Exception( "body说明不能为空");
        }
        if(body==null ||body.length()==0){
            throw new Exception( "outTradeNo外部订单号不能为空");
        }
        MyConfig config = new MyConfig(appId,mchId,key);
        WXPay wxpay = new WXPay(config);
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("body", body);
        data.put("out_trade_no", outTradeNo);
        data.put("fee_type", "CNY");
        data.put("total_fee", fee.toString()); // 分单位
        data.put("spbill_create_ip", createIp);
        data.put("notify_url",notifyUrl );
        data.put("trade_type", "NATIVE");  // "JSAPI"
        data.put("product_id", "1");
        try {
            String path=ClassUtils.getDefaultClassLoader().getResource("static").getPath();
            String name=UUID.randomUUID().toString()+".png";
            String targetPath="/py/images/"+UUID.randomUUID().toString()+".png";
            String logoPath=path+"/logo.png";
            Map<String, String> resp = wxpay.unifiedOrder(data);
            QRcodeUtil.encode(resp.get("code_url"), 300, 300, logoPath,targetPath);
            resp.put("imagePath", "http://re.luzhiai.com:8088/"+name);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 微信支付结果回调通知
     *
     * @throws Exception
     */
    @RequestMapping(value = "payNotify")
    @ResponseBody
    public String payNotify(HttpServletRequest request) throws Exception {
        Map<String, String> wxBackData = new HashMap<String, String>();
        InputStream is = request.getInputStream();
        String xml = inputStream2String(is, "UTF-8");
        // 后面把xml转成Map根据数据作逻辑处理
        Map<String, String> map = new HashMap<>();
        if(null==xml ||xml.length()==0){
            return "数据解析错误";
        }
        map = xmlToMap(xml);
        log.info("支付结果回调返回信息: " + JSONObject.toJSONString(map));
        String sign = "";
        //微信支付校验
        // 1.验证是否来自微信
        SortedMap<String, String> signMap = new TreeMap<>();
        sign = map.get("sign");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            signMap.put(entry.getKey(), entry.getValue());
        }
        signMap.remove("sign");
        //获取mchKey
        String mchKey = "";
        String checkSign = this.sign(signMap, mchKey, false);
        log.info("微信回调支付, 本地验证checkSign:" + checkSign);
        if (!sign.equalsIgnoreCase(checkSign)) {
            wxBackData.put("return_code", "SUCCESS");
            wxBackData.put("return_msg", "参数验证失败");
            log.error("参数验证失败");
            return mapToXml(wxBackData);
        }
        // 如果支付失败，则返回
        if (!"SUCCESS".equals(map.get("return_code"))) {
            wxBackData.put("return_code", "SUCCESS");
            wxBackData.put("return_msg", "支付失败");
            log.error("支付失败");
            return mapToXml(wxBackData);
        }
        //参数赋值
        String appId = map.get("appid");//服务商的APPID
        String bankType = map.get("bank_type");//付款银行
        String cashFee = map.get("cash_fee");//现金支付金额
        String openId = map.get("openid");//商户号
        String outTradeNo = map.get("out_trade_no");//商户订单号
        String timeEnd = map.get("time_end");//支付完成时间
        String totalFee = map.get("total_fee");//总金额
        String tradeType = map.get("trade_type");//交易类型
        String transactionId = map.get("transaction_id");//微信支付订单号
        //读取文件
        List<OrderModel> modelList =FileUtil.readFile(tableName, OrderModel.class);
        boolean exists=false;//是否已经支付成功,且回调过了
        for(OrderModel om:modelList){
            if(om.getTransactionId().equals(transactionId)){
                exists=true ;
            }
        }
        if(exists){
            return "";
        }
        //把回调结果写入文件
        OrderModel om =new OrderModel();
        om.setOpenId(openId);
        om.setOutTradeNo(outTradeNo);
        om.setTotalFee(new BigDecimal(totalFee));
        om.setTimeEnd(timeEnd);
        om.setTradeType(tradeType);
        om.setCashFee(new BigDecimal(cashFee));
        om.setAppId(appId);
        om.setBankType(bankType);
        modelList.add(om);
        FileUtil.writeFile(tableName,modelList);
        return "";
    }

    @ResponseBody
    @GetMapping("get")
    public OrderModel get(String outTradeNo)throws Exception{
        OrderModel om=null;
        if(null==outTradeNo ||"".equals(outTradeNo)){
            log.warn("订单号不能为空");
            return new OrderModel();
        }
        List<OrderModel> modelList =FileUtil.readFile(tableName, OrderModel.class);
        if(modelList==null||modelList.size()==0){
            FileUtil.writeFile(tableName,new ArrayList());
        }
        for(OrderModel model:modelList){
            if(model.getOutTradeNo().equals(outTradeNo)){
               return model;
            }
        }
        return new OrderModel();
    }

    @ResponseBody
    @RequestMapping("del")
    public String  del(String outTradeNo )throws Exception{
        if(null==outTradeNo ||"".equals(outTradeNo)){
            log.warn("订单号不能为空");
            return "订单号不能为空";
        }
        List<OrderModel> modelList =FileUtil.readFile(tableName, OrderModel.class);
        Iterator<OrderModel> it =modelList.iterator();
        while (it.hasNext()){
            OrderModel om=it.next();
            if(om.getTransactionId().equals(outTradeNo)){
                it.remove();
            }
        }
        FileUtil.writeFile(tableName,modelList);
        return "SUCCESS";
    }
    /**
     *  * InputStream流转换成String字符串
     *  * @param inStream InputStream流
     *  * @param encoding 编码格式
     *  * @return String字符串
     *  
     */
    private static String inputStream2String(InputStream inStream, String encoding) {
        String result = null;
        try {
            if (inStream != null) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] tempBytes = new byte[1024];
                int count = -1;
                while ((count = inStream.read(tempBytes, 0, 1024)) != -1) {
                    outStream.write(tempBytes, 0, count);
                }
                outStream.flush();
                result = new String(outStream.toByteArray(), encoding);
            }
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data Map类型数据
     * @return XML格式的字符串
     * @throws Exception
     */
    public static String mapToXml(Map<String, String> data) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        org.w3c.dom.Document document = documentBuilder.newDocument();
        org.w3c.dom.Element root = document.createElement("xml");
        document.appendChild(root);
        for (String key : data.keySet()) {
            String value = data.get(key);
            if (value == null) {
                value = "";
            }
            value = value.trim();
            org.w3c.dom.Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        String output = writer.getBuffer().toString(); //.replaceAll("\n|\r", "");
        try {
            writer.close();
        } catch (Exception ex) {
        }
        return output;
    }

    /**
     * XML格式字符串转换为Map
     *
     * @param strXML XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        String FEATURE = "";
        try {
            Map<String, String> data = new HashMap<String, String>();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
            documentBuilderFactory.setFeature(FEATURE, true);  //防止可能的SSRF
            FEATURE = "http://xml.org/sax/features/external-parameter-entities";
            documentBuilderFactory.setFeature(FEATURE, false);
            FEATURE = "http://xml.org/sax/features/external-general-entities";
            documentBuilderFactory.setFeature(FEATURE, false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return data;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 微信统一下单
     *
     * @param map
     * @param mchKey
     * @param isSha
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public String sign(SortedMap<String, String> map, String mchKey, boolean isSha) throws InvalidKeyException, NoSuchAlgorithmException {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> objectObjectEntry : map.entrySet()) {
            stringBuffer.append(objectObjectEntry.getKey()).append("=").append(objectObjectEntry.getValue()).append("&");
        }
        stringBuffer = stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.append("&key=").append(mchKey);
        String s = MD5.MD5Encode(stringBuffer.toString()).toUpperCase();
        if (isSha) {
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(mchKey.getBytes(), "HmacSHA256");
            sha256.init(secretKeySpec);
            byte[] bytes = sha256.doFinal(s.getBytes());
            return Hex.encodeHexString(bytes).toUpperCase();
        }

        return s;
    }
}
