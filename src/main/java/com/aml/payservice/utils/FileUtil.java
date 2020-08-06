package com.aml.payservice.utils;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liguiqin
 * @date 2020/8/1
 */
public class FileUtil {
    public static String FILE_PATH = "/py/database/";
    /**
     * 读取文件
     *
     * @param tableName
     * @return
     */
    public static <T> List<T> readFile(String tableName, Class<T> clz)  {
        try {
            String lastName = FILE_PATH + tableName.toLowerCase() + ".txt";
            File file = new File(lastName);
            if (!file.exists()) {
                file=new File(FILE_PATH);
                file.mkdirs();
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
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 写文件
     *
     * @param tableName
     * @return
     */
    public static boolean writeFile(String tableName, List objList) throws IOException {
        String lastName = FILE_PATH + tableName.toLowerCase() + ".txt";
        JSONWriter writer = new JSONWriter(new FileWriter(lastName));
        writer.startArray();
        for (Object bo : objList) {
            writer.writeValue(bo);
        }
        writer.endArray();
        writer.close();
        return true;
    }
}
