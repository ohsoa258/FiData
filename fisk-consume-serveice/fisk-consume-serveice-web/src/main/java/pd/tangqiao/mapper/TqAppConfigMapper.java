package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import pd.tangqiao.entity.TqAppConfigPO;
import pd.tangqiao.entity.TqAppConfigVO;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-29 13:42:34
 */
@Mapper
public interface TqAppConfigMapper extends BaseMapper<TqAppConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqAppConfigVO> getAll(Page<TqAppConfigVO> page);
}
