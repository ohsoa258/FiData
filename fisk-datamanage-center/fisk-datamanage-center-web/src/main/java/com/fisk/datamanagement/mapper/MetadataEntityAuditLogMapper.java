package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.dto.metaauditlog.AuditAnalysisDayChangeTotalVO;
import com.fisk.datamanagement.entity.AuditLogWithEntityTypeAndDetailPO;
import com.fisk.datamanagement.entity.MetadataEntityAuditLogPO;
import com.fisk.datamanagement.entity.MetadataEntityAuditLogPOWithEntityType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JinXingWang
 * @description 针对表【tb_metadata_entity_audit_log】的数据库操作Mapper
 * @createDate 2024-03-14 11:44:11
 * @Entity com.fisk.datamanagement.entity.MetadataEntityAuditLogPO
 */
public interface MetadataEntityAuditLogMapper extends BaseMapper<MetadataEntityAuditLogPO> {

    @Delete("truncate TABLE tb_metadata_entity_audit_log")
    int truncateTable();

    List<AuditAnalysisDayChangeTotalVO> getDayTotal(String beginTime, String endTime);

    @Select("SELECT a.id,a.operation_type,a.create_time,b.type_id,b.owner,c.business_classification_id as class_id FROM tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_classification_map c ON b.id = c.metadata_entity_id " +
            "WHERE a.create_time >= #{startTime} AND a.create_time <= #{endTime} AND a.del_flag = 1 AND b.del_flag = 1 " +
            "order by a.id DESC;")
    List<MetadataEntityAuditLogPOWithEntityType> getMetaChangesCharts(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT a.id,a.operation_type,a.create_time,b.type_id,b.owner,c.business_classification_id as class_id FROM tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_classification_map c ON b.id = c.metadata_entity_id " +
            "WHERE a.create_time >= #{startTime} AND a.create_time <= #{endTime} AND b.type_id = #{entityType}  AND a.del_flag = 1 AND b.del_flag = 1 " +
            "order by a.id DESC;")
    List<MetadataEntityAuditLogPOWithEntityType> getMetaChangesChartsByEntityType(@Param("startTime") LocalDateTime startTime,
                                                                              @Param("endTime") LocalDateTime endTime,
                                                                              @Param("entityType") Integer entityType);

    @Select("SELECT " +
            "a.id," +
            "a.entity_id," +
            "a.operation_type," +
            "a.create_time," +
            "a.create_user," +
            "b.type_id," +
            "b.name," +
            "b.parent_id," +
            "b.owner," +
            "c.audit_id," +
            "c.attribute," +
            "c.before_value," +
            "c.after_value," +
            "d.business_classification_id" +
            " FROM " +
            "tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_entity_audit_atrribute_change c ON a.id = c.audit_id " +
            "LEFT JOIN tb_metadata_classification_map d ON b.id = d.metadata_entity_id " +
            "WHERE " +
            "a.create_time >= #{startTime} AND a.create_time <= #{endTime} AND b.type_id = #{entityType} " +
            "AND a.del_flag = 1 " +
            "AND b.del_flag = 1 " +
            "order by a.id DESC " +
            "LIMIT #{current},#{size};")
    List<AuditLogWithEntityTypeAndDetailPO> getMetaChangesChartsDetailByOpType(@Param("startTime") LocalDateTime startTime,
                                                                               @Param("endTime") LocalDateTime endTime,
                                                                               @Param("entityType") Integer entityType,
                                                                               @Param("current") Integer current,
                                                                               @Param("size") Integer size
    );

    @Select("SELECT " +
            "a.id," +
            "a.entity_id," +
            "a.operation_type," +
            "a.create_time," +
            "a.create_user," +
            "b.type_id," +
            "b.name," +
            "b.parent_id," +
            "b.owner," +
            "c.audit_id," +
            "c.attribute," +
            "c.before_value," +
            "c.after_value," +
            "d.business_classification_id" +
            " FROM " +
            "tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_entity_audit_atrribute_change c ON a.id = c.audit_id " +
            "LEFT JOIN tb_metadata_classification_map d ON b.id = d.metadata_entity_id " +
            "WHERE " +
            "a.create_time >= #{startTime} AND a.create_time <= #{endTime} AND b.type_id = #{entityType}" +
            "AND a.del_flag = 1 " +
            "AND b.del_flag = 1 " +
            "order by a.id DESC " +
            ";")
    List<AuditLogWithEntityTypeAndDetailPO> getMetaChangesChartsDetailByOpTypeNoPage(@Param("startTime") LocalDateTime startTime,
                                                                               @Param("endTime") LocalDateTime endTime,
                                                                               @Param("entityType") Integer entityType
    );

    @Select("SELECT " +
            "a.id," +
            "a.entity_id," +
            "a.operation_type," +
            "a.create_time," +
            "a.create_user," +
            "b.type_id," +
            "b.name," +
            "b.parent_id," +
            "b.owner," +
            "c.audit_id," +
            "c.attribute," +
            "c.before_value," +
            "c.after_value," +
            "d.business_classification_id" +
            " FROM " +
            "tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_entity_audit_atrribute_change c ON a.id = c.audit_id " +
            "LEFT JOIN tb_metadata_classification_map d ON b.id = d.metadata_entity_id " +
            "WHERE " +
            "a.create_time >= #{startTime} AND a.create_time <= #{endTime} AND (b.type_id = 3 or b.type_id = 6 ) " +
            "AND a.del_flag = 1 " +
            "AND b.del_flag = 1 " +
            "order by a.id DESC " +
            " LIMIT #{current},#{size};")
    List<AuditLogWithEntityTypeAndDetailPO> getMetaChangesChartsDetail(@Param("startTime") LocalDateTime startTime,
                                                                       @Param("endTime") LocalDateTime endTime,
                                                                       @Param("current") Integer current,
                                                                       @Param("size") Integer size
    );

    @Select("SELECT " +
            "a.id," +
            "a.entity_id," +
            "a.operation_type," +
            "a.create_time," +
            "a.create_user," +
            "b.type_id," +
            "b.name," +
            "b.parent_id," +
            "b.owner," +
            "c.audit_id," +
            "c.attribute," +
            "c.before_value," +
            "c.after_value," +
            "d.business_classification_id" +
            " FROM " +
            "tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_entity_audit_atrribute_change c ON a.id = c.audit_id " +
            "LEFT JOIN tb_metadata_classification_map d ON b.id = d.metadata_entity_id " +
            "WHERE " +
            "a.create_time >= #{startTime} AND a.create_time <= #{endTime} AND (b.type_id = 3 or b.type_id = 6 ) " +
            "AND a.del_flag = 1 " +
            "AND b.del_flag = 1 " +
            "order by a.id DESC " +
            ";")
    List<AuditLogWithEntityTypeAndDetailPO> getMetaChangesChartsDetailNoPage(@Param("startTime") LocalDateTime startTime,
                                                                       @Param("endTime") LocalDateTime endTime
    );

    @Select("SELECT COUNT(0) FROM tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_entity_audit_atrribute_change c ON a.id = c.audit_id " +
            "WHERE a.create_time >= #{startTime} AND a.create_time <= #{endTime} " +
            "AND b.type_id = #{entityType} AND a.del_flag = 1 AND b.del_flag = 1")
    Long countMetaChangesCharts(@Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                @Param("entityType") Integer entityType);

    @Select("SELECT COUNT(0) FROM tb_metadata_entity_audit_log a " +
            "LEFT JOIN tb_metadata_entity b ON a.entity_id = b.id " +
            "LEFT JOIN tb_metadata_entity_audit_atrribute_change c ON a.id = c.audit_id " +
            "WHERE a.create_time >= #{startTime} AND a.create_time <= #{endTime} " +
            "AND b.type_id = #{entityType} AND a.del_flag = 1 AND b.del_flag = 1")
    Long countMetaChangesChartsByOpType(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("entityType") Integer entityType);

}




