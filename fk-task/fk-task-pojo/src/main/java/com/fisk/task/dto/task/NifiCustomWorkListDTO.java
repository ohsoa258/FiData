package com.fisk.task.dto.task;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NifiCustomWorkListDTO {
    //所有节点
    public List<NifiCustomWorkDTO> nifiCustomWorkDTOS;
    //组分层,还没有定数据结构
    public Map<Map, Map> structure;
    /**
     * 外部父子级
     */
    public Map<Map, Map> externalStructure;

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
        //结构--structure--(父,子)
        Map<Map, Map> structure = new HashMap<>();
        //structure1--父级(id,name)
        Map<Integer, String> structure1 = new HashMap<>();
        structure1.put(1, "1a");
        Map<Integer, String> structure11 = new HashMap<>();
        structure11.put(11, "11");
        //structure2--子级(id,name)
        Map<Integer, String> structure2 = new HashMap<>();
        Map<Integer, String> structure22 = new HashMap<>();
        structure2.put(2, "2a");
        structure22.put(22, "22");
        structure.put(structure1, structure2);
        structure.put(structure11, structure22);


        //structure.put(structure1, structure2);

        System.out.println("structure22 = " + structure);
    }
}
