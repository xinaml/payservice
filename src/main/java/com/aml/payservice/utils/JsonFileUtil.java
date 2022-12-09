package com.aml.payservice.utils;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhouwenwei
 * @createTime: 2020-01-19 17:08
 * @description:
 */
@Log4j2
public class JsonFileUtil {
    public static String FILE_PATH = "/home/file_database/";

    /**
     * 写文件
     *
     * @param tableName
     * @return
     */
    public static boolean writeFile(String tableName, List objList)  {
        String lastName = FILE_PATH + tableName.toLowerCase() + ".txt";
        JSONWriter writer = null;
        try {
            File file=new File(FILE_PATH);
            if(!file.exists()){
                file.mkdirs();
            }
            writer = new JSONWriter(new FileWriter(lastName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.startArray();
        for (Object bo : objList) {
            writer.writeValue(bo);
        }
        writer.endArray();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean flushFile(String tableName) throws IOException {
        String lastName = FILE_PATH + tableName.toLowerCase() + ".txt";
        JSONWriter writer = new JSONWriter(new FileWriter(lastName));
        writer.flush();
        writer.close();
        return true;
    }


    /**
     * 读取文件
     *
     * @param tableName
     * @return
     */
    public static <T> List<T> readFile(String tableName, Class<T> clz)   {

        try {
            String lastName = FILE_PATH + tableName.toLowerCase() + ".txt";
            File file = new File(lastName);
            if (!file.exists()) {
                log.info("原文件不存在 表名:" + tableName);
                return new ArrayList<>();
            }
            JSONReader reader = new JSONReader(new FileReader(lastName));
            List<T> objList = new ArrayList<>();
            reader.startArray();
            while (reader.hasNext()) {
                T model = reader.readObject(clz);
                objList.add(model);
            }
            reader.endArray();
            reader.close();
            return objList;
        } catch (Exception e) {
            log.error("读取原文件失败, 表名:" + tableName, e);
            return new ArrayList<>();
        }
    }

}
