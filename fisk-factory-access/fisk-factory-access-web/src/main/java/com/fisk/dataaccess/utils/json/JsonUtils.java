package com.fisk.dataaccess.utils.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.FieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/18 17:38
 */
@Slf4j
public class JsonUtils {

    public final String JSONSTR = "{\\\"data\\\":[{\\\"createTime\\\":\\\"2022-1-2212:00:00\\\",\\\"name\\\":\\\"张三\\\",\\\"tb_role\\\":[{\\\"roleId\\\":1,\\\"roleName\\\":\\\"role1\\\",\\\"tb_menu\\\":[{\\\"menuSrc\\\":\\\"menu1\\\",\\\"roleId\\\":1,\\\"menuName\\\":\\\"menu1\\\"},{\\\"menuSrc\\\":\\\"menu2\\\",\\\"roleId\\\":1,\\\"menuName\\\":\\\"menu2\\\"},{\\\"menuSrc\\\":\\\"menu3\\\",\\\"roleId\\\":1,\\\"menuName\\\":\\\"menu3\\\"}],\\\"userId\\\":1},{\\\"roleId\\\":2,\\\"roleName\\\":\\\"role2\\\",\\\"tb_menu\\\":[{\\\"menuSrc\\\":\\\"menu21\\\",\\\"roleId\\\":2,\\\"menuName\\\":\\\"menu21\\\"},{\\\"menuSrc\\\":\\\"menu22\\\",\\\"roleId\\\":2,\\\"menuName\\\":\\\"menu22\\\"},{\\\"menuSrc\\\":\\\"menu23\\\",\\\"roleId\\\":2,\\\"menuName\\\":\\\"menu23\\\"}],\\\"userId\\\":1},{\\\"roleId\\\":3,\\\"roleName\\\":\\\"role3\\\",\\\"userId\\\":1}],\\\"id\\\":1,\\\"age\\\":16},{\\\"createTime\\\":\\\"2022-1-2212:30:00\\\",\\\"name\\\":\\\"李四\\\",\\\"tb_role\\\":[{\\\"roleId\\\":4,\\\"roleName\\\":\\\"role4\\\",\\\"userId\\\":2},{\\\"roleId\\\":5,\\\"roleName\\\":\\\"role5\\\",\\\"userId\\\":2},{\\\"roleId\\\":6,\\\"roleName\\\":\\\"role6\\\",\\\"userId\\\":2}],\\\"id\\\":2,\\\"age\\\":17}]}";


//    public static void main(String[] args) {
//        // 测试时间
////        Instant inst1 = Instant.now();
//        JSONObject json = JSON.parseObject(JSONSTR);
////        System.out.println("json = " + json);
//
//        // 封装数据库存储的数据结构
//        List<ApiTableDTO> apiTableDtoList = getApiTableDtoList01();
////        apiTableDtoList.forEach(System.out::println);
////        int a = 1 / 0;
//
//        List<String> tableNameList = apiTableDtoList.stream().map(tableDTO -> tableDTO.tableName).collect(Collectors.toList());
//        // 获取目标表
//        List<JsonTableData> targetTable = getTargetTable(tableNameList);
//        targetTable.forEach(System.out::println);
//        // 获取Json的schema信息
//        List<JsonSchema> schemas = getJsonSchema(apiTableDtoList);
//        schemas.forEach(System.out::println);
////        System.out.println("====================");
//        try {
//            // json根节点处理
//            rootNodeHandler(schemas, json, targetTable);
//            targetTable.forEach(System.out::println);
//
//            System.out.println("开始执行sql");
//            PgsqlUtils pgsqlUtils = new PgsqlUtils();
//            // ods_abbreviationName_tableName
//            pgsqlUtils.executeBatchPgsql("", targetTable);
//
////            Instant inst2 = Instant.now();
////            System.out.println("Difference in 纳秒 : " + Duration.between(inst1, inst2).getNano());
////            System.out.println("Difference in seconds : " + Duration.between(inst1, inst2).getSeconds());
//        } catch (Exception e) {
//            System.out.println("执行失败");
//        }
//    }

    /**
     * json根节点处理
     *
     * @return void
     * @description json根节点处理
     * @author gy
     * @date 2022/1/20 15:49
     * @version v1.0
     * @params schemas json架构
     * @params data json数据
     * @params targetTable 最终处理结果
     */
    public void rootNodeHandler(List<JsonSchema> schemas, JSONObject data, List<JsonTableData> targetTable) throws Exception {
        for (JsonSchema schema : schemas) {
            if (schema.type == JsonSchema.TypeEnum.ARRAY) {
                JSONArray arr = data.getJSONArray(schema.name);
                dataNodeHandler(schema.children, arr, targetTable);
            }
        }
    }

    /**
     * json数据节点处理（递归处理所有节点）
     *
     * @return void
     * @description json数据节点处理
     * @author gy
     * @date 2022/1/20 15:50
     * @version v1.0
     * @params schemas json架构
     * @params data json数据
     * @params targetTable 最终处理结果
     */
    public void dataNodeHandler(List<JsonSchema> schemas, JSONArray data, List<JsonTableData> targetTable) throws Exception {
        if (data == null) {
            return;
        }
        for (JsonTableData item : targetTable) {
            List<JsonSchema> tableSchema = schemas.stream()
                    .filter(e -> StringUtils.hasLength(e.targetTableName) && e.targetTableName.equals(item.table))
                    .collect(Collectors.toList());
            if (tableSchema.size() > 0) {
                for (Object rowData : data) {
                    JSONObject jsonRowData = (JSONObject) rowData;
                    JSONObject model = new JSONObject();
                    for (JsonSchema columnSchema : schemas) {
                        switch (columnSchema.type) {
                            case INT:
                                model.put(columnSchema.name, jsonRowData.getInteger(columnSchema.name));
                                break;
                            case STRING:
                                model.put(columnSchema.name, jsonRowData.getString(columnSchema.name));
                                break;
                            case DATETIME:
                                model.put(columnSchema.name, jsonRowData.getDate(columnSchema.name));
                                break;
                            case ARRAY:
                                dataNodeHandler(columnSchema.children, jsonRowData.getJSONArray(columnSchema.name), targetTable);
                                break;
                            default:
                                throw new Exception("未知类型");
                        }
                    }
                    item.data.add(model);
                }
            }
        }
    }

    /**
     * 获取Json的schema信息
     *
     * @return java.util.List<com.fisk.test.model.JsonSchema>
     * @description 获取Json的schema信息
     * @author gy
     * @date 2022/1/20 14:16
     * @version v1.0
     * @params jsonKey json解析的根节点
     */
    public List<JsonSchema> getJsonSchema(List<ApiTableDTO> apiTableDtoList, String jsonKey) {
        List<JsonSchema> root = new ArrayList<>();
        try {
            Map<String, List<JsonSchema>> map = getSchemaDetail(apiTableDtoList);
////            System.out.println("map = " + map);
            for (ApiTableDTO apiTableDTO : apiTableDtoList) {
////                List<JsonSchema> dataSchema = test02(apiTableDTO);
                List<JsonSchema> mySchema = map.get(apiTableDTO.tableName);
                // 当前为父级
                if (apiTableDTO.pid == 0) {
                    JsonSchema jsonSchema = JsonSchema.builder()
                            .name(jsonKey)
                            .type(JsonSchema.TypeEnum.ARRAY)
                            .children(mySchema)
                            .build();
                    root.add(jsonSchema);
                }
                // 存在子级
                if (!CollectionUtils.isEmpty(apiTableDTO.childTableName)) {
                    for (String childTableName : apiTableDTO.childTableName) {
                        List<JsonSchema> jsonSchemas = map.get(childTableName);
                        mySchema.add(JsonSchema.builder()
                                .name(childTableName)
                                .type(JsonSchema.TypeEnum.ARRAY)
                                .targetTableName(apiTableDTO.tableName)
                                .children(jsonSchemas)
                                .build());
                    }
                }
            }
            return root;
        } catch (Exception e) {
            System.out.println("封装节点参数有误");
            log.error("【getTables】获取表名报错, ex", e);
            throw new FkException(ResultEnum.PARSE_JSONSCHEMA_ERROR);
        }
    }

    /**
     * 封装表字段
     *
     * @return java.util.List<com.fisk.dataaccess.dto.json.JsonSchema>
     * @description 封装表字段
     * @author Lock
     * @date 2022/2/15 17:40
     * @version v1.0
     * @params tableDto
     */
    public List<JsonSchema> getSchemaField(ApiTableDTO tableDto) {
        List<JsonSchema> dataSchema = new ArrayList<>();

        for (TableFieldsDTO fieldsDto : tableDto.list) {

            dataSchema.add(JsonSchema.builder()
                    .name(fieldsDto.sourceFieldName)
                    .type(converFieldType(fieldsDto.fieldType))
                    .targetTableName(tableDto.tableName)
                    .build());
        }
        return dataSchema;
    }

    /**
     * 封装所有参数
     *
     * @return java.util.Map<java.lang.String, java.util.List < com.fisk.dataaccess.dto.json.JsonSchema>>
     * @description 封装所有参数
     * @author Lock
     * @date 2022/2/15 17:41
     * @version v1.0
     * @params apiTableDtoList
     */
    public Map<String, List<JsonSchema>> getSchemaDetail(List<ApiTableDTO> apiTableDtoList) {
        Map<String, List<JsonSchema>> map = new HashMap(1000);

        for (ApiTableDTO apiTableDTO : apiTableDtoList) {
            List<JsonSchema> dataSchema = getSchemaField(apiTableDTO);
            map.put(apiTableDTO.tableName, dataSchema);
        }

        return map;
    }

    /**
     * 获取目标表
     *
     * @return java.util.List<com.fisk.dataaccess.dto.json.JsonTableData>
     * @description 获取目标表
     * @author Lock
     * @date 2022/1/21 11:38
     * @version v1.1
     * @params tableNameList 表名集合
     */
    public List<JsonTableData> getTargetTable(List<String> tableNameList) {
        return tableNameList.stream()
                .map(tableName -> JsonTableData.builder().table(tableName).data(new JSONArray()).build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 转换字段类型
     *
     * @return com.fisk.dataaccess.dto.json.JsonSchema.TypeEnum
     * @description 转换字段类型
     * @author Lock
     * @date 2022/1/21 14:50
     * @version v1.0
     * @params fieldType 字段类型
     */
    public JsonSchema.TypeEnum converFieldType(String fieldType) {
        FieldTypeEnum fieldTypeEnum = FieldTypeEnum.getValue(fieldType);
        switch (Objects.requireNonNull(fieldTypeEnum)) {
            case INT:
                return JsonSchema.TypeEnum.INT;
            case DATETIME:
                return JsonSchema.TypeEnum.DATETIME;
            case STRING1:
            case STRING2:
            default:
                return JsonSchema.TypeEnum.STRING;
        }
    }


    /**
     * data是个list的字符串集合
     *
     * @param data     操作的数据
     * @param fieldMap 要修改的字段
     * @return
     */
    public static String updateJsonArray(String data, Map<String, String> fieldMap) {

        //如果data是个集合
        JSONArray dataArray = JSONArray.parseArray(data);
        //遍历dataArray
        Iterator<Object> iterator = dataArray.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            JSONObject object = (JSONObject) iterator.next();
            Iterator<Map.Entry<String, String>> iterator1 = fieldMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<String, String> next = iterator1.next();
                //获取原本key
                String source = object.getString(next.getKey());
                //删除connection对象
                object.remove(next.getKey());
                object.put(next.getValue(), source);
            }
        }
        return JSON.toJSONString(dataArray);
    }

}
