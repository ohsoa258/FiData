package com.fisk.datagovernance.mapper.dataquality;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterPageDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerAppConfigPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-14 14:17:55
 */
@Mapper
public interface DatacheckServerAppConfigMapper extends FKBaseMapper<DatacheckServerAppConfigPO> {

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<AppRegisterVO> filter(Page<AppRegisterVO> page, @Param("query") AppRegisterPageDTO query);
}
