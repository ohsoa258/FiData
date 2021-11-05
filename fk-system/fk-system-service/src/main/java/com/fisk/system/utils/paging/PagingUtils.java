package com.fisk.system.utils.paging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/9/1 17:37
 */
public class PagingUtils {

    /**
     * 开始分页
     *
     * @param list
     * @param pageNo  页码
     * @param dataSize 每页多少条数据
     * @return
     */
    public static <F> List<F> startPage(List<F> list, int pageNo, int dataSize) {
        if (list == null) {
            list = new ArrayList<F>();
        }
        if ((Object) pageNo == null) {
            pageNo = 1;
        }
        if ((Object) dataSize == null) {
            dataSize = 1;
        }
        if (pageNo <= 0) {
            pageNo = 1;
        }

        //记录一下数据一共有多少条
        int totalitems = list.size();
        //实例化一个接受分页处理之后的数据
        List<F> afterList = new ArrayList<F>();

        for (int i = (pageNo - 1) * dataSize;
         i < (((pageNo - 1) * dataSize) + dataSize >
                 totalitems ? totalitems : ((pageNo - 1) * dataSize) + dataSize);
         i++) {
            //然后将数据存入afterList中

            afterList.add(list.get(i));
        }

        return afterList;
    }
}
