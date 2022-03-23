package com.fisk.task.dto.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fisk.task.dto.MQBaseDTO;
import com.google.gson.Gson;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NifiCustomWorkListDTO  extends MQBaseDTO {
    //所有节点
    public List<NifiCustomWorkDTO> nifiCustomWorkDTOS;
    //组分层,还没有定数据结构
    @JsonIgnore
    public Map<Map, Map> structure;
    /**
     * 外部父子级
     */
    @JsonIgnore
    public Map<Map, Map> externalStructure;

    public String structure1;
    public String externalStructure1;

    //管道英文id(workflowId)
    public String nifiCustomWorkflowId;
    //管道id
    public Long pipelineId;
    //管道name
    public String pipelineName;
    //userid
    public Long userId;


    /*
    * 数据结构
    * */
    public static void main(String[] args) {
        String mapString="{{dde685ce-9b4b-421d-95aa-08ce2f488c97=cmd}={643=job1, 644=job2},{124=cmd}={643=job1, 644=job2},{125=cmd}={643=job1, 644=job2},{126=cmd}={643=job1, 644=job2}}";
        mapString=mapString.substring(1,mapString.length()-2);
        String[] split = mapString.split("},");
         Map<Map,Map> map=new HashMap<>();
         Map<Map,Map> map1=new HashMap<>();
         Map<Map,Map> map2=new HashMap<>();
        Map<Map, Map> map3 = new HashMap<>();
        Gson gson = new Gson();
        for (String s:split) {
            map1=map3;
            map2=map3;
            s+="}";
            //{123=cmd}={643=job1, 644=job2}
            String[] split1 = s.split("}=");
            split1[0]+="}";
            map1 = gson.fromJson(split1[0],Map.class);
            map2 = gson.fromJson(split1[1],Map.class);
            map.put(map1, map2);
        }

    }
}
