package com.zklcsoftware.common.web.initialize;

import com.zklcsoftware.basic.util.StringUtil;
import com.zklcsoftware.common.web.provider.QueryCommonProvider;
import graphql.GraphQL;
import graphql.language.OperationTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GraphQLManagerProvider {

    @Value("${engine.schema.file_path:classpath*:schema/*.graphql*}")
    public String file_path;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private QueryCommonProvider queryCommonProvider;

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public GraphQL createGraphQL() throws Exception {
        TypeDefinitionRegistry sdl = getSDLFormLocal();
        if (sdl == null) {
            log.error("没有要加载的schema文件");
            return null;
        }
        //刷新bean
        initBean(sdl);
        return GraphQL.newGraphQL(buildSchema(sdl)).build();
    }

    public void initBean(TypeDefinitionRegistry sdl) {
        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
        //创建bean信息.
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(GraphQL.class);
        beanDefinitionBuilder.addConstructorArgValue(buildSchema(sdl));
        //动态注册bean.
        defaultListableBeanFactory.registerBeanDefinition("graphQL", beanDefinitionBuilder.getBeanDefinition());
        //获取动态注册的bean
        GraphQL graphQL = ctx.getBean(GraphQL.class);
        log.info("graphql refresh :{}", graphQL);
    }

    private GraphQLSchema buildSchema(TypeDefinitionRegistry typeDefinitionRegistry) {
        RuntimeWiring runtimeWiring = buildWiring(typeDefinitionRegistry);
        return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring(TypeDefinitionRegistry typeDefinitionRegistry) {
        Optional<SchemaDefinition> optionalSchemaDefinition = typeDefinitionRegistry.schemaDefinition();
        SchemaDefinition schemaDefinition = optionalSchemaDefinition.get();
        Map<String, Type> typeNameMap = schemaDefinition.getOperationTypeDefinitions()
                .stream()
                .collect(Collectors.toMap(OperationTypeDefinition::getName, OperationTypeDefinition::getTypeName));
        //动态加载schema中定义的query的typeName
        TypeRuntimeWiring.Builder queryBuilder = TypeRuntimeWiring.newTypeWiring(((TypeName) typeNameMap.get("query")).getName());
        //指定业务动态处理策略
        queryBuilder.defaultDataFetcher(this.queryCommonProvider);
        return RuntimeWiring.newRuntimeWiring()
                .type(queryBuilder)
                .scalar(new DateScalarType()).scalar(new LongScalarType())
                .build();
    }

    /**
     * 加载Schema
     *
     * @return
     * @throws Exception
     */
    private TypeDefinitionRegistry getSDLFormLocal() throws Exception {
        List<Resource> pathList = new ArrayList<>();
        Resource[] resources = findResource();
        if (resources != null && resources.length > 0) {
            for (Resource resource : resources) {
                if (resource.getFilename().indexOf("graphql") > 0) {
                    log.info("load schema file name: {}", resource.getFilename());
                    pathList.add(resource);
                }
            }
        } else {
            log.error("模型文件不存在");
            return null;
        }
        return typeDefinitionRegistry(pathList);
    }

    /**
     * 整合各个schema文件路径
     *
     * @param pathList
     * @return
     * @throws Exception
     */
    public TypeDefinitionRegistry typeDefinitionRegistry(List<Resource> pathList) throws Exception {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        for (Resource resource : pathList) {
            InputStream inputStream = resource.getInputStream();
            String sdl = StringUtil.readStreamString(inputStream, "UTF-8");
            typeRegistry.merge(schemaParser.parse(sdl));
        }
        return typeRegistry;
    }


    private Resource[] findResource() throws IOException {
        return resourcePatternResolver.getResources(file_path);
    }

}
