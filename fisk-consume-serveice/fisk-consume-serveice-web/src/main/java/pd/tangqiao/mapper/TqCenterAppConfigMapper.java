package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import pd.tangqiao.entity.TqCenterAppConfigPO;
import pd.tangqiao.entity.TqCenterAppConfigVO;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-29 16:04:32
 */
@Mapper
public interface TqCenterAppConfigMapper extends BaseMapper<TqCenterAppConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqCenterAppConfigVO> getAll(Page<TqCenterAppConfigVO> page);
}
