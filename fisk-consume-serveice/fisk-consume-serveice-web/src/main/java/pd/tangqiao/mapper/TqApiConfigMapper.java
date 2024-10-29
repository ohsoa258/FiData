package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pd.tangqiao.entity.TqApiConfigPO;
import pd.tangqiao.entity.TqApiConfigQueryDTO;
import pd.tangqiao.entity.TqApiConfigVO;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-29 13:46:14
 */
@Mapper
public interface TqApiConfigMapper extends BaseMapper<TqApiConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqApiConfigVO> getAll(Page<TqApiConfigVO> page,@Param("query") TqApiConfigQueryDTO dto);
}
