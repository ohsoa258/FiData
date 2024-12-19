package com.fisk.task.utils.powerbiutils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.pbi.PBItemDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class PBIUtils {


    /**
     * pbi获取当前用户下的工作区信息
     *
     * @param token
     * @return
     */
    public List<PBItemDTO> getAllGroups(String token) {
        String PBI_GET_GROUPS = "https://api.powerbi.com/v1.0/myorg/groups";
        List<PBItemDTO> items = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(PBI_GET_GROUPS)
                .header("Authorization", "Bearer " + token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("pbi【getAllGroups】获取工作区报错, status code: {},msg：{}", response.code(), response.body());
                throw new FkException(ResultEnum.PBI_GET_GROUP_ERROR, "Failed to get groups, status code: " + response.code());
            }

            String responseBody = Objects.requireNonNull(response.body()).string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray valueArray = jsonResponse.getJSONArray("value");

            for (int i = 0; i < valueArray.size(); i++) {
                JSONObject group = valueArray.getJSONObject(i);
                PBItemDTO pbItemDTO = new PBItemDTO();
                pbItemDTO.setName(group.getString("name"));
                pbItemDTO.setGuid(group.getString("id"));
                pbItemDTO.setType(group.getString("type"));
                items.add(pbItemDTO);
            }

        } catch (Exception e) {
            log.error("pbi【getAllGroups】获取工作区报错, ex", e);
            throw new FkException(ResultEnum.PBI_GET_GROUP_ERROR, e.getMessage());
        }
        return items;
    }

    /**
     * pbi获取指定工作区下的数据集列表
     *
     * @param token
     * @return
     */
    public List<PBItemDTO> getAllDatasets(String token, String groupId) {

        List<PBItemDTO> items = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();

        String PBI_GET_DATASETS = "https://api.powerbi.com/v1.0/myorg/groups/{groupid}/datasets";
        PBI_GET_DATASETS = PBI_GET_DATASETS.replace("{groupid}", groupId);

        Request request = new Request.Builder()
                .url(PBI_GET_DATASETS)
                .header("Authorization", "Bearer " + token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("pbi【getAllDatasets】获取工作区下的数据集报错, status code: {},msg：{}", response.code(), response.body());
                throw new FkException(ResultEnum.PBI_GET_DATASETS_ERROR);
            }

            String responseBody = Objects.requireNonNull(response.body()).string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray valueArray = jsonResponse.getJSONArray("value");

            for (int i = 0; i < valueArray.size(); i++) {
                JSONObject group = valueArray.getJSONObject(i);
                PBItemDTO pbItemDTO = new PBItemDTO();
                pbItemDTO.setName(group.getString("name"));
                pbItemDTO.setGuid(group.getString("id"));
                pbItemDTO.setCreateUser(group.getString("configuredBy"));
                items.add(pbItemDTO);
            }

        } catch (Exception e) {
            log.error("pbi【getAllDatasets】获取工作区下的数据集报错, ex", e);
            throw new FkException(ResultEnum.PBI_GET_DATASETS_ERROR, e.getMessage());
        }
        return items;
    }

    /**
     * pbi获取指定数据集包含的所有表
     *
     * @param token
     * @return
     */
//    public List<PBItemDTO> getAllTables(String token, String groupId, String datasetId) {
//
//        List<PBItemDTO> items = new ArrayList<>();
//
//        OkHttpClient client = new OkHttpClient();
//
//        PBI_GET_TABLES = PBI_GET_TABLES.replace("{groupid}", groupId);
//        PBI_GET_TABLES = PBI_GET_TABLES.replace("{datasetid}", datasetId);
//
//        Request request = new Request.Builder()
//                .url(PBI_GET_TABLES)
//                .header("Authorization", "Bearer " + token)
//                .build();
//
//        try {
//            Response response = client.newCall(request).execute();
//            if (!response.isSuccessful()) {
//                log.error("pbi【getAllTables】获取指定数据集包含的所有表报错, status code: {},msg：{}", response.code(), response.body());
//                throw new FkException(ResultEnum.PBI_GET_DATASETS_ERROR);
//            }
//
//            String responseBody = Objects.requireNonNull(response.body()).string();
//            JSONObject jsonResponse = JSON.parseObject(responseBody);
//            JSONArray valueArray = jsonResponse.getJSONArray("value");
//
//            for (int i = 0; i < valueArray.size(); i++) {
//                JSONObject group = valueArray.getJSONObject(i);
//                PBItemDTO pbItemDTO = new PBItemDTO();
//                pbItemDTO.setName(group.getString("name"));
//                items.add(pbItemDTO);
//            }
//
//        } catch (Exception e) {
//            log.error("pbi【getAllTables】获取指定数据集包含的所有表报错, ex", e);
//            throw new FkException(ResultEnum.PBI_GET_DATASETS_ERROR, e.getMessage());
//        }
//        return items;
//    }
    public List<PBItemDTO> getAllTables(String token, String groupId, String datasetId) {

        List<PBItemDTO> items = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();

        //pbi获取指定数据集包含的所有表  url
        String PBI_GET_TABLES = "https://api.powerbi.com/v1.0/myorg/groups/{groupid}/datasets/{datasetid}/tables";
        String url = PBI_GET_TABLES.replace("{groupid}", groupId).replace("{datasetid}", datasetId);
        log.info("Request URL: {}", url);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("pbi【getAllTables】获取指定数据集包含的所有表报错, status code: {}, msg: {}", response.code(), response.body());
                throw new FkException(ResultEnum.PBI_GET_DATASETS_ERROR);
            }

            String responseBody = Objects.requireNonNull(response.body()).string();
            log.info("Response Body: {}", responseBody);

            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray valueArray = jsonResponse.getJSONArray("value");

            for (int i = 0; i < valueArray.size(); i++) {
                JSONObject table = valueArray.getJSONObject(i);
                PBItemDTO pbItemDTO = new PBItemDTO();
                pbItemDTO.setName(table.getString("name"));
                items.add(pbItemDTO);
            }

        } catch (Exception e) {
            log.error("pbi【getAllTables】获取指定数据集包含的所有表报错, ex", e);
            throw new FkException(ResultEnum.PBI_GET_DATASETS_ERROR, e.getMessage());
        }
        return items;
    }

    /**
     * pbi查询指定数据集下指定表的数据  执行查询语句
     *
     * @param token
     * @param selectSql
     * @return
     * @throws IOException
     */
    public String executePowerBiQuery(String token, String selectSql, String datasetId, String pbiUserName) throws IOException {
        if (StringUtils.isEmpty(pbiUserName)){
            pbiUserName = "someuser@mycompany.com";
        }
        OkHttpClient client = new OkHttpClient();
        String PBI_POST_DATASET_QUERY = "https://api.powerbi.com/v1.0/myorg/datasets/{datasetid}/executeQueries";
        PBI_POST_DATASET_QUERY = PBI_POST_DATASET_QUERY.replace("{datasetid}", datasetId);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String jsonBody = "{\n" +
                "  \"queries\": [\n" +
                "    {\n" +
                "      \"query\": \"" + selectSql + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"serializerSettings\": {\n" +
                "    \"includeNulls\": true\n" +
                "  },\n" +
                "  \"impersonatedUserName\": \"" + pbiUserName + "\"\n" +
                "}";
        log.info("pbi jsonBody = " + jsonBody);

        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Request request = new Request.Builder()
                .url(PBI_POST_DATASET_QUERY)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("PBI DAX语句：" + selectSql + "。【executePowerBiQuery】pbi执行查询失败: " + response.code() + " - " + response.message());
            }
        } catch (Exception e) {
            log.error("【executePowerBiQuery】pbi执行查询报错, ex", e);
            throw new IOException("PBI DAX语句：" + selectSql + "。【executePowerBiQuery】pbi执行查询失败");
        }
    }

    /**
     * pbi获取token下的所有工作区 group
     *
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getAllGroupsByToken(String token) {

        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            //1.获取当前用户下的所有工作区
            List<PBItemDTO> allGroups = getAllGroups(token);
            if (allGroups.isEmpty()) {
                return list;
            }

            //2.获取当前工作区里的所有数据集
            for (PBItemDTO group : allGroups) {
                TablePyhNameDTO dto = new TablePyhNameDTO();
                dto.setTableName(group.getName());
                dto.setGuid(group.getGuid());
                list.add(dto);

            }

        } catch (Exception e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return list;
    }

}
