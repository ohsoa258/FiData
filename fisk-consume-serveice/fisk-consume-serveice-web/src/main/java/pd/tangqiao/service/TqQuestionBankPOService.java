package pd.tangqiao.service;

import pd.tangqiao.entity.TqQuestionBankPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_question_bank】的数据库操作Service
* @createDate 2024-10-29 14:54:59
*/
public interface TqQuestionBankPOService extends IService<TqQuestionBankPO> {

    /**
     * 问题库新增
     *
     * @param po
     * @return
     */
    Object add(TqQuestionBankPO po);

    /**
     * 问题库回显
     *
     * @return
     */
    Object getBanks();
}
