package com.fisk.common;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.junit.Test;

public class ResultEntityBuildTest {

    @Test
    public void buildTest(){
        ResultEntity<Object> res = ResultEntityBuild.build(ResultEnum.SUCCESS);
        System.out.println(res);
        assert res.code == ResultEnum.SUCCESS.getCode();
        assert res.data == null;
    }
}
