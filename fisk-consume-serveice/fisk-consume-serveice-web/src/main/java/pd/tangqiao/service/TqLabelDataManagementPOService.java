package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import pd.tangqiao.entity.TqLabelDataManagementPO;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tq_label_data_management】的数据库操作Service
 * @createDate 2024-10-29 14:35:34
 */
public interface TqLabelDataManagementPOService extends IService<TqLabelDataManagementPO> {

    /**
     * 为指定规则添加标签
     *
     * @param dtos
     * @return
     */
    Object add(List<TqLabelDataManagementPO> dtos);

    /**
     * 获取标签集合
     *
     * @return
     */
    Object getLables();

    Object edit(List<TqLabelDataManagementPO> dtos);
}
