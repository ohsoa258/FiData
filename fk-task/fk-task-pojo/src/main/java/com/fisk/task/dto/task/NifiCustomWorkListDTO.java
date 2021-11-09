package com.fisk.task.dto.task;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NifiCustomWorkListDTO {
    //所有节点
    public List<NifiCustomWorkDTO> nifiCustomWorkDTOS;
    //组分层,还没有定数据结构
    public Map<Map, Map> structure;
    //管道id
    public Integer pipelineId;
    //管道name
    public String pipelineName;
    //userid
    public Long userId;


    /*
    * 数据结构
    * */
    public static void main(String[] args) {
        //结构--structure--(父,子)
        Map<Map, Map> structure = new HashMap<>();
        //structure1--父级(id,name)
        Map<Integer, String> structure1 = new HashMap<>();
        //structure2--子级(id,name)
        Map<Integer, String> structure2 = new HashMap<>();
        structure.put(structure1, structure2);
        

    }
}
