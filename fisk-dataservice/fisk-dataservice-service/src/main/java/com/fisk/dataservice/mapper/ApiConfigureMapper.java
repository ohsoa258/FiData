package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowPageDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.dataservice.dto.DownSystemPageDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.vo.DownSystemQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;;
import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2021/7/8 14:58
 */
@Mapper
public interface ApiConfigureMapper extends BaseMapper<ApiConfigurePO> {

    /**
     * 查询指定表的数据
     * @param splitSql 拼接sql
     * @param currentPage  当前页数
     * @param pageSize     页数大小
     * @return
     */
    List<Map> queryData(@Param("splitSql") String splitSql,
                        @Param("currentPage")Integer currentPage,
                        @Param("pageSize")Integer pageSize
    );

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<DownSystemQueryVO> filter(Page<DownSystemQueryVO> page, @Param("query") DownSystemPageDTO query);
}
