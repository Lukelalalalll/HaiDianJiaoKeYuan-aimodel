package com.zklcsoftware.common.web.initialize;

import com.alibaba.fastjson.JSON;
import com.zklcsoftware.basic.util.StringUtil;
import graphql.schema.GraphQLArgument;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static graphql.Scalars.GraphQLString;
import static java.util.regex.Pattern.compile;

public class ValueTypeTranslator {
    public static Object[] translateArgs(Method currentMethord,
                                         Map<String, Object> args, List<GraphQLArgument> gqlArgs)
            throws InvocationTargetException,
            IllegalAccessException,
            NoSuchMethodException,
            InstantiationException {
        List<Object> rtns = new ArrayList<>();
        //add 反射调用带参数的方法，需要保证参数顺序；

        Parameter[] parameters = currentMethord.getParameters();
        Class[] parameterTypes = currentMethord.getParameterTypes();
        if (parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class parameterType = parameterTypes[i];
                GraphQLArgument gqlArg = findByName(parameter.getName(), gqlArgs);
                //缺少的参数进行初始化补全
                if (gqlArg == null) {
                    gqlArg = GraphQLArgument.newArgument().name(parameter.getName())
                            .type(GraphQLString).build();
                    rtns.add(gqlArg);
                    continue;
                }
                rtns.add(traslateValueType(parameterType, gqlArg, args.get(parameter.getName())));
            }
        }

        return rtns.toArray();
    }

    private static GraphQLArgument findByName(String name, List<GraphQLArgument> gqlArgs) {
        for (GraphQLArgument arg : gqlArgs) {
            if (arg.getName().equals(name)) {
                return arg;
            }
        }
        //不存在返回null
        return null;
    }

    private static Object traslateValueType(Class parameterType, GraphQLArgument gqlArg,
                                            Object source)
            throws InvocationTargetException,
            IllegalAccessException,
            NoSuchMethodException,
            InstantiationException {

        if (!StringUtil.isJavaClass(parameterType)) {
            return JSON.parseObject(JSON.toJSONString((Map) source), parameterType);
        }
        String typeName = gqlArg.getType().toString();
        if (compile("(?i)string").matcher(typeName).find()) {
            return source == null ? "" : source.toString();
        }
        if (compile("(?i)boolean").matcher(typeName).find()) {
            if (source == null) {
                return false;
            }
            return Boolean.valueOf(source.toString());
        }
        if (compile("(?i)int").matcher(typeName).find()) {
            return Integer.valueOf(source == null ? "0" : source.toString());
        }
        if (compile("(?i)id").matcher(typeName).find()) {
            if (source == null) {
                source = "0";
            }
            return Long.valueOf(source.toString());
        }
        if (compile("(?i)float").matcher(typeName).find()) {
            return Float.valueOf(source == null ? "0" : source.toString());
        } else {
            //自定义类型，序列化
            Constructor<?> ctor = parameterType.getConstructor();
            Object rtn = ctor.newInstance(new Object[]{});
            BeanUtils.populate(rtn, (Map) source);
            return rtn;
        }
    }
}
