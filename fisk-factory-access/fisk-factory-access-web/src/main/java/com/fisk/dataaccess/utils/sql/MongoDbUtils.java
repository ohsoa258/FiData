package com.fisk.dataaccess.utils.sql;

import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbUtils {


    public List<TablePyhNameDTO> getTrueTableNameList(MongoClient mongoClient, String conDbname) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        //库名
        MongoDatabase database = mongoClient.getDatabase(conDbname);
        //获取库下的所有集合
        MongoIterable mongoIterable = database.listCollectionNames();
        MongoCursor table = mongoIterable.iterator();
        while (table.hasNext()) {
            //获取集合名（表名）
            String tableName = table.next().toString();
            //根据collection名获取collection
            MongoCollection<Document> collection = database.getCollection(tableName);
            //查找collection中的所有数据
            FindIterable findIterable = collection.find();
            MongoCursor cursor = findIterable.iterator();
            List<TableStructureDTO> tb_columns = new ArrayList<>();
            while (cursor.hasNext()) {
                String str = cursor.next().toString();
                str = str.substring(9, str.length() - 1);
                str = str.replaceAll("[{]", "{\"");
                str = str.replaceAll("[}]", "\"}");
                str = str.replaceAll("=", "\":\"");
                str = str.replaceAll(",", "\",\"");
                str = str.replaceAll(" ", "");
                JSONObject jsonObject = JSONObject.parseObject(str);
                for (String fieldName : jsonObject.keySet()) {
                    int mark = 0;
                    for (TableStructureDTO tc : tb_columns) {
                        //避免字段重复
                        if (fieldName.equals(tc.fieldName))
                            mark = 1;
                    }
                    if (mark == 0) {
                        TableStructureDTO dto = new TableStructureDTO();
                        // 获取字段名称
                        dto.fieldName = fieldName;
                        // 获取字段类型
                        dto.fieldType = "STRING";
                        dto.sourceTblName = tableName;
                        dto.sourceDbName = conDbname;
                        tb_columns.add(dto);
                    }
                }
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(conDbname + "." + tableName);
                tablePyhNameDTO.setFields(tb_columns);
                list.add(tablePyhNameDTO);
            }

        }

        return list;
    }

    public List<TableStructureDTO> getTrueTableNameListForOneTbl(MongoClient mongoClient, String conDbname, String tblName) {
        //库名
        MongoDatabase database = mongoClient.getDatabase(conDbname);
        //根据集合名（表名) 获取collection
        MongoCollection<Document> collection = database.getCollection(tblName);
        //查找collection中的所有数据
        FindIterable findIterable = collection.find();
        MongoCursor cursor = findIterable.iterator();
        List<TableStructureDTO> tb_columns = new ArrayList<>();
        while (cursor.hasNext()) {
            String str = cursor.next().toString();
            str = str.substring(9, str.length() - 1);
            str = str.replaceAll("[{]", "{\"");
            str = str.replaceAll("[}]", "\"}");
            str = str.replaceAll("=", "\":\"");
            str = str.replaceAll(",", "\",\"");
            str = str.replaceAll(" ", "");
            JSONObject jsonObject = JSONObject.parseObject(str);
            for (String fieldName : jsonObject.keySet()) {
                int mark = 0;
                for (TableStructureDTO tc : tb_columns) {
                    //避免字段重复
                    if (fieldName.equals(tc.fieldName))
                        mark = 1;
                }
                if (mark == 0) {
                    TableStructureDTO dto = new TableStructureDTO();
                    // 获取字段名称
                    dto.fieldName = fieldName;
                    // 获取字段类型
                    dto.fieldType = "STRING";
                    dto.sourceTblName = tblName;
                    dto.sourceDbName = conDbname;
                    tb_columns.add(dto);
                }
            }
            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
            tablePyhNameDTO.setTableName(conDbname + "." + tblName);
            tablePyhNameDTO.setFields(tb_columns);
        }
        return tb_columns;
    }
}
