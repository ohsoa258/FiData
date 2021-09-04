package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.mapper.MessageLogMapper;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.service.IOlap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 建模
 * @author JinXingWang
 */
public class Olapmpl extends ServiceImpl<OlapMapper, OlapPO> implements IOlap {

    @Resource
    OlapMapper mapper;

    /**
     * 生成建模sql(创建指标表sql，创建维度表sql,查询指标表数据sql)
     * @param modelMetaDataDTOS
     * @return
     */
    @Override
    public boolean build(List<ModelMetaDataDTO> modelMetaDataDTOS) {
//        mapper.deleteByBusinessId(modelMetaDataDTOS);
        return true;

    }

    /**
     * 生成创建主键模型sql
     * @param modelMetaDataDTO 维度表信息
     * @return sql
     */
    public String buildCreateUniqModelSql(ModelMetaDataDTO modelMetaDataDTO){
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(modelMetaDataDTO.tableName);
        sql.append("(");
        StringBuilder sqlFileds_build = new StringBuilder();
        StringBuilder sqlUnique_build = new StringBuilder("ENGINE=OLAP  UNIQUE KEY(");
        StringBuilder sqlDistributed_build = new StringBuilder("DISTRIBUTED BY HASH(");
        modelMetaDataDTO.dto.forEach((l) -> {
            if (l.fieldCnName.equals(modelMetaDataDTO.tableName+"_key")) {
                sqlUnique_build.append(l.fieldEnName + ",");
                sqlDistributed_build.append(l.fieldEnName + ",");
            }
            sqlFileds_build.append(l.fieldEnName + " " + l.fieldType + " comment " + "'" + l.fieldCnName + "' ,");
        });
        sqlFileds_build.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' )");
        String sqlFileds = sqlFileds_build.toString();
        //sqlFileds = sqlFileds.substring(0, sqlFileds.lastIndexOf(",")) + ")";
        String sqlUnique = sqlUnique_build.toString();
        sqlUnique = sqlUnique.substring(0, sqlUnique.lastIndexOf(",")) + ")";
        String sqlDistributed = sqlDistributed_build.toString();
        sqlDistributed = sqlDistributed.substring(0, sqlDistributed.lastIndexOf(",")) + ") BUCKETS 10";
        sql.append(sqlFileds).append(sqlUnique).append(sqlDistributed).append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        return sql.toString();
    }

    /**
     * 创建聚合模型sql
     * @return sql
     */
    public String buildCreateAggregateModelSql(){
        return "";
    }
}
