package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqQuestionBankPO;
import pd.tangqiao.mapper.TqQuestionBankPOMapper;
import pd.tangqiao.service.TqQuestionBankPOService;

/**
 * @author 56263
 * @description 针对表【tq_question_bank】的数据库操作Service实现
 * @createDate 2024-10-29 14:54:59
 */
@Service
public class TqQuestionBankPOServiceImpl extends ServiceImpl<TqQuestionBankPOMapper, TqQuestionBankPO>
        implements TqQuestionBankPOService {

    /**
     * 问题库新增
     *
     * @param po
     * @return
     */
    @Override
    public Object add(TqQuestionBankPO po) {
        return this.save(po);
    }

    /**
     * 问题库回显
     *
     * @return
     */
    @Override
    public Object getBanks() {
        return this.list();
    }
}




