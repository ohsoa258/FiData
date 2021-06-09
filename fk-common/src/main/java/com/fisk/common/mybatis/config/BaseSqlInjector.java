package com.fisk.common.mybatis.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.LogicDeleteByIdWithFill;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BaseSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        //MP自定义的SQL语句，如果不添加，MP自定义的语句就不能用了
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new LogicDeleteByIdWithFill());
        return methodList;
    }
}
