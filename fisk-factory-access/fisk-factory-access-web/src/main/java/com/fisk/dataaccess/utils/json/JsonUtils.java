package com.fisk.dataaccess.utils.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO02;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
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
     * 手动设置数据库参数
     *
     * @return list
     */
    public List<ApiTableDTO> getApiTableDtoList01() {

        List<ApiTableDTO> list = new ArrayList<>();

        ApiTableDTO apiTableDTO1 = new ApiTableDTO();
        apiTableDTO1.tableName = "tb_user";
        apiTableDTO1.pid = 0;

        List<TableFieldsDTO> fieldsDTOS1 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO1 = new TableFieldsDTO();
        tableFieldsDTO1.fieldName = "id";
        tableFieldsDTO1.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO2 = new TableFieldsDTO();
        tableFieldsDTO2.fieldName = "age";
        tableFieldsDTO2.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO3 = new TableFieldsDTO();
        tableFieldsDTO3.fieldName = "name";
        tableFieldsDTO3.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO4 = new TableFieldsDTO();
        tableFieldsDTO4.fieldName = "createTime";
        tableFieldsDTO4.fieldType = "VARCHAR";
        fieldsDTOS1.add(tableFieldsDTO1);
        fieldsDTOS1.add(tableFieldsDTO2);
        fieldsDTOS1.add(tableFieldsDTO3);
        fieldsDTOS1.add(tableFieldsDTO4);
        apiTableDTO1.list = fieldsDTOS1;
        List<String> ch = new ArrayList<>();
        ch.add("tb_role");
//        ch.add("tb_menu");
        apiTableDTO1.childTableName = ch;


        ApiTableDTO apiTableDTO2 = new ApiTableDTO();
        apiTableDTO2.tableName = "tb_role";
        apiTableDTO2.pid = 1;

        List<TableFieldsDTO> fieldsDTOS2 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO5 = new TableFieldsDTO();
        tableFieldsDTO5.fieldName = "roleId";
        tableFieldsDTO5.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO6 = new TableFieldsDTO();
        tableFieldsDTO6.fieldName = "roleName";
        tableFieldsDTO6.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO7 = new TableFieldsDTO();
        tableFieldsDTO7.fieldName = "userId";
        tableFieldsDTO7.fieldType = "INT";
        fieldsDTOS2.add(tableFieldsDTO5);
        fieldsDTOS2.add(tableFieldsDTO6);
        fieldsDTOS2.add(tableFieldsDTO7);
        apiTableDTO2.list = fieldsDTOS2;
        List<String> ch2 = new ArrayList<>();
        ch2.add("tb_menu");
        apiTableDTO2.childTableName = ch2;

        ApiTableDTO apiTableDTO3 = new ApiTableDTO();
        apiTableDTO3.tableName = "tb_menu";
        apiTableDTO3.pid = 1;
        List<TableFieldsDTO> fieldsDTOS3 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO8 = new TableFieldsDTO();
        tableFieldsDTO8.fieldName = "menuSrc";
        tableFieldsDTO8.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO9 = new TableFieldsDTO();
        tableFieldsDTO9.fieldName = "roleId";
        tableFieldsDTO9.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO10 = new TableFieldsDTO();
        tableFieldsDTO10.fieldName = "menuName";
        tableFieldsDTO10.fieldType = "VARCHAR";
        fieldsDTOS3.add(tableFieldsDTO8);
        fieldsDTOS3.add(tableFieldsDTO9);
        fieldsDTOS3.add(tableFieldsDTO10);
        apiTableDTO3.list = fieldsDTOS3;
        apiTableDTO3.childTableName = null;


        list.add(apiTableDTO1);
        list.add(apiTableDTO2);
        list.add(apiTableDTO3);
        return list;
    }

    public  List<ApiTableDTO02> getApiTableDtoList02() {

        List<ApiTableDTO02> list = new ArrayList<>();
        List<ApiTableDTO02> list2 = new ArrayList<>();
        List<ApiTableDTO02> list3 = new ArrayList<>();

        ApiTableDTO02 apiTableDTO1 = new ApiTableDTO02();
        apiTableDTO1.tableName = "tb_user";
        apiTableDTO1.pid = true;

        List<TableFieldsDTO> fieldsDTOS1 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO1 = new TableFieldsDTO();
        tableFieldsDTO1.fieldName = "id";
        tableFieldsDTO1.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO2 = new TableFieldsDTO();
        tableFieldsDTO2.fieldName = "age";
        tableFieldsDTO2.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO3 = new TableFieldsDTO();
        tableFieldsDTO3.fieldName = "name";
        tableFieldsDTO3.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO4 = new TableFieldsDTO();
        tableFieldsDTO4.fieldName = "createTime";
        tableFieldsDTO4.fieldType = "VARCHAR";
        fieldsDTOS1.add(tableFieldsDTO1);
        fieldsDTOS1.add(tableFieldsDTO2);
        fieldsDTOS1.add(tableFieldsDTO3);
        fieldsDTOS1.add(tableFieldsDTO4);
        apiTableDTO1.list = fieldsDTOS1;
        List<String> ch = new ArrayList<>();
        ch.add("tb_role");
        ch.add("tb_menu");
//        apiTableDTO1.childTableName = ch;


        ApiTableDTO02 apiTableDTO2 = new ApiTableDTO02();
        apiTableDTO2.tableName = "tb_role";
        apiTableDTO2.pid = false;

        List<TableFieldsDTO> fieldsDTOS2 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO5 = new TableFieldsDTO();
        tableFieldsDTO5.fieldName = "roleId";
        tableFieldsDTO5.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO6 = new TableFieldsDTO();
        tableFieldsDTO6.fieldName = "roleName";
        tableFieldsDTO6.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO7 = new TableFieldsDTO();
        tableFieldsDTO7.fieldName = "userId";
        tableFieldsDTO7.fieldType = "INT";
        fieldsDTOS2.add(tableFieldsDTO5);
        fieldsDTOS2.add(tableFieldsDTO6);
        fieldsDTOS2.add(tableFieldsDTO7);
        apiTableDTO2.list = fieldsDTOS2;
        List<String> ch2 = new ArrayList<>();
        ch2.add("tb_menu");
//        apiTableDTO2.childTableName = ch2;

        ApiTableDTO02 apiTableDTO3 = new ApiTableDTO02();
        apiTableDTO3.tableName = "tb_menu";
        apiTableDTO3.pid = false;
        List<TableFieldsDTO> fieldsDTOS3 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO8 = new TableFieldsDTO();
        tableFieldsDTO8.fieldName = "menuSrc";
        tableFieldsDTO8.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO9 = new TableFieldsDTO();
        tableFieldsDTO9.fieldName = "roleId";
        tableFieldsDTO9.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO10 = new TableFieldsDTO();
        tableFieldsDTO10.fieldName = "menuName";
        tableFieldsDTO10.fieldType = "VARCHAR";
        fieldsDTOS3.add(tableFieldsDTO8);
        fieldsDTOS3.add(tableFieldsDTO9);
        fieldsDTOS3.add(tableFieldsDTO10);
        apiTableDTO3.list = fieldsDTOS3;
//        apiTableDTO3.childTableName = null;

        list3.add(apiTableDTO3);
        list2.add(apiTableDTO2);
        apiTableDTO2.childTable = list3;
        apiTableDTO1.childTable = list2;
//        list.add(apiTableDTO2);
//        list.add(apiTableDTO3);
        list.add(apiTableDTO1);

        return list;
    }

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
        if (data == null)
            return;
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
     * @params
     */
    public List<JsonSchema> getJsonSchema(List<ApiTableDTO> apiTableDtoList) {
        List<JsonSchema> root = new ArrayList<>();
        try {
            Map<String, List<JsonSchema>> map = getSchemaDetail(apiTableDtoList);
//            System.out.println("map = " + map);
            for (ApiTableDTO apiTableDTO : apiTableDtoList) {
//                List<JsonSchema> dataSchema = test02(apiTableDTO);
                List<JsonSchema> mySchema = map.get(apiTableDTO.tableName);
                // 当前为父级
                if (apiTableDTO.pid==0) {
                    JsonSchema jsonSchema = JsonSchema.builder()
                            .name("data")
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
                    .name(fieldsDto.fieldName)
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
        Map<String, List<JsonSchema>> map = new HashMap();

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

}
