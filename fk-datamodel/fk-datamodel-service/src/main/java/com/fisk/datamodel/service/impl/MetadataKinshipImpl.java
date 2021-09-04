package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.datamodel.dto.metadatakinship.*;
import com.fisk.datamodel.service.IMetadataKinship;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class MetadataKinshipImpl implements IMetadataKinship {

    public String baseUrl ="http://192.168.1.250:21000";
    public String user ="admin";
    public String pwd ="admin";
    public String instanceActionUrl = "/api/atlas/v2/search/basic";
    public String lineageActionUrl = "/api/atlas/v2/lineage";
    public int limit=100;

    @Override
    public List<InstanceDTO> getInstance()
    {
        try
        {
            boolean isLast=false;
            int offset=0; //偏移量
            int total=0; //实例总数
            int count=0; //数据总数
            List<InstanceDTO> list=new ArrayList<>(); //存储实例集合
            //分页获取所有实例
            while (!isLast)
            {
                String path= baseUrl + instanceActionUrl +"?typeName=rdbms_instance&limit="+limit+"&offset="+offset;
                String result= sendGet(path);
                JSONObject jsonObj = JSON.parseObject(result);
                total=Integer.parseInt(jsonObj.getString("approximateCount"));
                JSONArray array = jsonObj.getJSONArray("entities");
                for (int i = 0; i < array.size(); i++)
                {
                    InstanceDTO dto=new InstanceDTO();
                    dto.value =array.getJSONObject(i).getString("guid");
                    dto.name =array.getJSONObject(i).getString("displayText");
                    list.add(dto);
                    count+=1;
                }
                if (count>=total)
                {
                    isLast=true;
                    break;
                }
                offset+=limit;
            }
            //根据实例获取实例下库、表、字段
            for (InstanceDTO item:list)
            {
                String path= baseUrl + lineageActionUrl +"/"+item.value;
                String result= sendGet(path);
                JSONObject jsonObj = JSON.parseObject(result);
                //获取关联关系id
                RelationsShipDTO relationsShipList=new RelationsShipDTO();
                List<RelationsDTO> list1=new ArrayList<>();
                JSONArray relationsArr=jsonObj.getJSONArray("relations");
                String guidEntityMap=jsonObj.getString("guidEntityMap");
                JSONObject jsonObj1 = JSON.parseObject(guidEntityMap);

                for (int i = 0; i < relationsArr.size(); i++)
                {
                    RelationsDTO relationsDTO=new RelationsDTO();
                    relationsDTO.fromEntityId=relationsArr.getJSONObject(i).getString("fromEntityId");
                    relationsDTO.toEntityId=relationsArr.getJSONObject(i).getString("toEntityId");
                    list1.add(relationsDTO);
                }
                relationsShipList.list=list1;
                //根据guidList获取相应名称
                //根据实例id，去查相应process信息
                List<RelationsDTO> processGuid=list1.stream().filter(e->item.value.equals(e.getFromEntityId())).collect(Collectors.toList());
                if (processGuid==null || processGuid.size()==0)
                {
                    break;
                }
                RelationsDTO model=processGuid.stream().findFirst().get();
                //根据process去查对象库(可能多个库)
                List<RelationsDTO> dbList=relationsShipList.list.stream().filter(e->model.toEntityId.equals(e.toEntityId)).collect(Collectors.toList());
                if (dbList==null || dbList.size()==0)
                {
                    break;
                }
                List<DbBaseDTO> dbBaseDTOS=new ArrayList<>();
                for (RelationsDTO db:dbList)
                {
                    //获取process=>toEntityId
                    List<RelationsDTO> toEntityIds=relationsShipList.list.stream().filter(e->model.toEntityId.equals(e.getFromEntityId())).collect(Collectors.toList());
                    if(toEntityIds==null || toEntityIds.size()==0)
                    {
                        break;
                    }
                    for(RelationsDTO relationsDTO:toEntityIds)
                    {
                        //获取数据库名称
                        ReturnDTO content=getContent(jsonObj1,relationsDTO.toEntityId);
                        DbBaseDTO dto=new DbBaseDTO();
                        dto.value =content.guid;
                        dto.name =content.name;
                        dbBaseDTOS.add(dto);
                    }
                }
                item.list =dbBaseDTOS;
                List<TableDTO> tableDTOS=new ArrayList<>();
                //获取库下的表数据
                for (DbBaseDTO dbBaseDTO:dbBaseDTOS)
                {
                    //根据表guid获取process
                    List<RelationsDTO> tabProcessList=relationsShipList.list.stream().filter(e->dbBaseDTO.value.equals(e.getFromEntityId())).collect(Collectors.toList());
                    for (RelationsDTO relation:tabProcessList)
                    {
                        List<RelationsDTO> toRelation=relationsShipList.list.stream().filter(e->relation.toEntityId.equals(e.getFromEntityId())).collect(Collectors.toList());
                        for (RelationsDTO table:toRelation)
                        {
                            //获取数据库名称
                            ReturnDTO content=getContent(jsonObj1,table.toEntityId);
                            TableDTO dto=new TableDTO();
                            dto.value =content.guid;
                            dto.name =content.name;
                            tableDTOS.add(dto);
                        }
                    }
                    dbBaseDTO.list =tableDTOS;
                }

                //获取表下的所有字段
                for (TableDTO table:tableDTOS)
                {
                    List<FieldDTO> fieldDTOList=new ArrayList<>();
                    //根据表guid获取process
                    List<RelationsDTO> fieldProcessList=relationsShipList.list.stream().filter(e->table.value.equals(e.getFromEntityId())).collect(Collectors.toList());
                    for (RelationsDTO relation:fieldProcessList)
                    {
                        List<RelationsDTO> toRelation=relationsShipList.list.stream().filter(e->relation.toEntityId.equals(e.getFromEntityId())).collect(Collectors.toList());
                        for (RelationsDTO field:toRelation)
                        {
                            //获取数据库名称
                            ReturnDTO content=getContent(jsonObj1,field.toEntityId);
                            FieldDTO dto=new FieldDTO();
                            dto.value =content.guid;
                            dto.name =content.name;
                            fieldDTOList.add(dto);
                        }
                    }
                    table.list =fieldDTOList;
                }
            }
            return list;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
        }
        return null;
    }

    //根据guid，获取相关名称与guid
    public ReturnDTO getContent(JSONObject jsonObj,String relationId){
        try {
            ReturnDTO dto=new ReturnDTO();
            String  jsonObj1String = jsonObj.getString(relationId);
            JSONObject jsonObj2 = JSON.parseObject(jsonObj1String);
            dto.guid=jsonObj2.getString("guid");
            dto.name=jsonObj2.getString("displayText");
            return dto;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
        }
        return null;
    }


    public String sendGet(String urlParam) {
        String output;
        try
        {
            String url=urlParam;
            URL serverUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Authorization", getAuthorization());
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException(
                        "HTTP GET Request Failed with Error code : "+ connection.getResponseCode());
            }
            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            if ((output = responseBuffer.readLine()) != null) {
                System.out.println(output);
            }
            else {
                System.err.println(output);
            }
            connection.disconnect();
        }
        catch (IOException e)
        {
            output=e.getMessage();
        }
        return output;
    }

    private static String getAuthorization() {
        BASE64Encoder encoder = new BASE64Encoder();
        String auth = "admin" + ":" + "admin";
        String base64 = encoder.encode(auth.getBytes());
        return "Basic " + base64;
    }


}
