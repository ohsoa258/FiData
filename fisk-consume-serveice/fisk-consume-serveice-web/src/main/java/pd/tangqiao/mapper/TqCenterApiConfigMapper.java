package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pd.tangqiao.entity.TqCenterApiConfigPO;
import pd.tangqiao.entity.TqCenterApiConfigQueryDTO;
import pd.tangqiao.entity.TqCenterApiConfigVO;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-29 16:04:32
 */
@Mapper
public interface TqCenterApiConfigMapper extends BaseMapper<TqCenterApiConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqCenterApiConfigVO> getAll(Page<TqCenterApiConfigVO> page, @Param("query") TqCenterApiConfigQueryDTO dto);
}
