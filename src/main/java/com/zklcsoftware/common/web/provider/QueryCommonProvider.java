package com.zklcsoftware.common.web.provider;

import com.zklcsoftware.common.web.exception.EngineException;
import com.zklcsoftware.common.web.initialize.ValueTypeTranslator;
import com.zklcsoftware.common.web.resolver.IQueryResolver;
import com.zklcsoftware.common.web.resolver.impl.CommonQueryResolver;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@Component
public class QueryCommonProvider implements DataFetcher {

    @Autowired
    private List<IQueryResolver> queryResolverList;

    @Autowired
    private CommonQueryResolver commonQueryResolver;


    /**
     * graphql执行业务实现
     *
     * @param environment
     * @return
     * @throws Exception
     */
    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
        String name = fieldDefinition.getName();
        Object[] traslatedArgs = new Object[0];
        Method currentMethord = null;
        IQueryResolver curResolver = null;
        for (IQueryResolver resolver : this.queryResolverList) {
            Method[] methods = resolver.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    currentMethord = method;
                    curResolver = resolver;
                    break;
                }
            }
        }
        if (currentMethord == null) {
            return doExcute(name, traslatedArgs);
        }
        Method real = AopUtils.getMostSpecificMethod(currentMethord,
                AopUtils.getTargetClass(curResolver));
        try {
            traslatedArgs = ValueTypeTranslator
                    .translateArgs(real, environment.getArguments(),
                            fieldDefinition.getArguments());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return doExcute(name, traslatedArgs);
    }

    /**
     * 遍历service和method寻找匹配的serviceMethod
     *
     * @param functionName
     * @param args
     * @return
     */
    private Object doExcute(String functionName, Object[] args) {
        for (IQueryResolver resolver : this.queryResolverList) {
            Method[] methods = resolver.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(functionName)) {
                    try {
                        return method.invoke(resolver, args);
                    } catch (IllegalAccessException e) {
                        throw new EngineException(e.getMessage());
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getTargetException();
                        if (cause instanceof EngineException) {
                            throw (EngineException) cause;
                        }
                        throw new EngineException(e.getCause().getMessage());
                    }
                }
            }
        }
        //查询不到执行下面
        this.commonQueryResolver.excute(functionName, args);
        return null;
    }

}
