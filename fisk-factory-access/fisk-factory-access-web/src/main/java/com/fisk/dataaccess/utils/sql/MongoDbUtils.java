package com.fisk.dataaccess.utils.sql;

import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoDbUtils {

    public List<TablePyhNameDTO> getTrueTableNameList(MongoClient mongoClient, String conDbname) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        //库名
        MongoDatabase database = mongoClient.getDatabase(conDbname);
        //获取集合名（表名）
        String tableName = "_schema";
        //根据collection名获取collection
        MongoCollection<Document> collection = database.getCollection(tableName);
        Set<String> keys = new HashSet<>();
        List<TableStructureDTO> tb_columns = new ArrayList<>();

        //查找collection中的所有数据
        for (Document document : collection.find()) {
            List<String> tb_columns1 = new ArrayList<>();
            String tblName = (String) document.get("table");
            Object fields = document.get("fields");
            JSONArray jsonArray = new JSONArray(fields.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String fieldName = jsonObject.getString("name");
                String fieldType = jsonObject.getString("type");
                TableStructureDTO dto = new TableStructureDTO();
                // 获取字段名称
                dto.fieldName = fieldName;
                // 获取字段类型
                dto.fieldType = "STRING";
                dto.sourceTblName = tableName;
                dto.sourceDbName = conDbname;
                tb_columns.add(dto);
            }
            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
            tablePyhNameDTO.setTableName(conDbname + "." + tableName);
            tablePyhNameDTO.setFields(tb_columns);
            list.add(tablePyhNameDTO);
        }

        //        //查找collection中的所有数据
//        for (Document document : collection.find()) {
//            for (String k : document.keySet()) {
//                if (!keys.contains(k)) {
//                    TableStructureDTO dto = new TableStructureDTO();
//                    // 获取字段名称
//                    dto.fieldName = k;
//                    // 获取字段类型
//                    dto.fieldType = "STRING";
//                    dto.sourceTblName = tableName;
//                    dto.sourceDbName = conDbname;
//                    tb_columns.add(dto);
//                    keys.add(k);
//                }
//            }
//            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
//            tablePyhNameDTO.setTableName(conDbname + "." + tableName);
//            tablePyhNameDTO.setFields(tb_columns);
//            list.add(tablePyhNameDTO);
//        }

        return list;
    }

    public List<TableStructureDTO> getTrueTableNameListForOneTbl(MongoClient mongoClient, String conDbname, String tblName) {
        //库名
        MongoDatabase database = mongoClient.getDatabase(conDbname);
        //根据集合名（表名) 获取collection
        MongoCollection<Document> collection = database.getCollection(tblName);
        //查找collection中的所有数据
        Set<String> keys = new HashSet<>();
        List<TableStructureDTO> tb_columns = new ArrayList<>();
        //查找collection中的所有数据
        for (Document document : collection.find()) {
            for (String k : document.keySet()) {
                if (!keys.contains(k)) {
                    TableStructureDTO dto = new TableStructureDTO();
                    // 获取字段名称
                    dto.fieldName = k;
                    // 获取字段类型
                    dto.fieldType = "STRING";
                    dto.sourceTblName = tblName;
                    dto.sourceDbName = conDbname;
                    tb_columns.add(dto);
                    keys.add(k);
                }
            }
        }
        return tb_columns;
    }
}
