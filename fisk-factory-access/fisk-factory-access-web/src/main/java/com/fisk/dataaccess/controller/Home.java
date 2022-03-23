package com.fisk.dataaccess.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/1/20 13:47
 */
@RestController
public class Home {


    @GetMapping("test")
    public List<JsonTableData> get(JSONObject data) throws Exception {

        List<String> tableNameList = new ArrayList<>();
        tableNameList.add("tb_user");
        tableNameList.add("tb_role");
        tableNameList.add("tb_menu");

        // 获取目标表
        List<JsonTableData> targetTable = getTargetTable(tableNameList);
        System.out.println("json = " + data);
        targetTable.forEach(System.out::println);
        // 获取Json的schema信息
        List<JsonSchema> schemas = getJsonSchema();
//        schemas.forEach(System.out::println);
        // json根节点处理
        rootNodeHandler(schemas, data, targetTable);
        targetTable.forEach(System.out::println);
        int a = 1 / 0;
        return targetTable;
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
    private void rootNodeHandler(List<JsonSchema> schemas, JSONObject data, List<JsonTableData> targetTable) throws Exception {
        for (JsonSchema schema : schemas) {
            if (schema.type == JsonSchema.TypeEnum.ARRAY) {
                JSONArray arr = data.getJSONArray(schema.name);
                // json数据节点处理（递归处理所有节点）
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
    private void dataNodeHandler(List<JsonSchema> schemas, JSONArray data, List<JsonTableData> targetTable) throws Exception {
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
                                // json数据节点处理（递归处理所有节点）
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
    private List<JsonSchema> getJsonSchema() {
        List<JsonSchema> root = new ArrayList<>();
        List<JsonSchema> userDataSchema = new ArrayList<>();
        List<JsonSchema> roleDataSchema = new ArrayList<>();
        List<JsonSchema> menuDataSchema = new ArrayList<>();

        JsonSchema jsonSchema = JsonSchema.builder()
                .name("data")
                .type(JsonSchema.TypeEnum.ARRAY)
                .children(userDataSchema)
                .build();
        root.add(jsonSchema);

        userDataSchema.add(JsonSchema.builder()
                .name("id")
                .type(JsonSchema.TypeEnum.INT)
                .targetTableName("tb_user")
                .build());
        userDataSchema.add(JsonSchema.builder()
                .name("name")
                .type(JsonSchema.TypeEnum.STRING)
                .targetTableName("tb_user")
                .build());
        userDataSchema.add(JsonSchema.builder()
                .name("age")
                .type(JsonSchema.TypeEnum.INT)
                .targetTableName("tb_user")
                .build());
        userDataSchema.add(JsonSchema.builder()
                .name("createTime")
                .type(JsonSchema.TypeEnum.DATETIME)
                .targetTableName("tb_user")
                .build());
        userDataSchema.add(JsonSchema.builder()
                .name("role")
                .type(JsonSchema.TypeEnum.ARRAY)
                .targetTableName("tb_user")
                .children(roleDataSchema)
                .build());

        roleDataSchema.add(JsonSchema.builder()
                .name("userId")
                .type(JsonSchema.TypeEnum.INT)
                .targetTableName("tb_role")
                .build());
        roleDataSchema.add(JsonSchema.builder()
                .name("roleId")
                .type(JsonSchema.TypeEnum.INT)
                .targetTableName("tb_role")
                .build());
        roleDataSchema.add(JsonSchema.builder()
                .name("roleName")
                .type(JsonSchema.TypeEnum.STRING)
                .targetTableName("tb_role")
                .build());
        roleDataSchema.add(JsonSchema.builder()
                .name("menus")
                .type(JsonSchema.TypeEnum.ARRAY)
                .children(menuDataSchema)
                .build());

        menuDataSchema.add(JsonSchema.builder()
                .name("roleId")
                .type(JsonSchema.TypeEnum.INT)
                .targetTableName("tb_menu")
                .build());
        menuDataSchema.add(JsonSchema.builder()
                .name("menuName")
                .type(JsonSchema.TypeEnum.STRING)
                .targetTableName("tb_menu")
                .build());
        menuDataSchema.add(JsonSchema.builder()
                .name("menuSrc")
                .type(JsonSchema.TypeEnum.STRING)
                .targetTableName("tb_menu")
                .build());

        return root;
    }

    /**
     * 获取目标表
     *
     * @return java.util.List<com.fisk.dataaccess.dto.json.JsonTableData>
     * @description 获取目标表
     * @author Lock
     * @date 2022/1/21 11:38
     * @version v1.0
     * @params tableNameList
     */
    public List<JsonTableData> getTargetTable(List<String> tableNameList) {
        return tableNameList.stream()
                .map(tableName -> JsonTableData.builder().table(tableName).data(new JSONArray()).build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
