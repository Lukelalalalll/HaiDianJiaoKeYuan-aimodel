package com.zklcsoftware.common.web.initialize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
@Slf4j
public class GraphQLManager {
    @Autowired
    private GraphQLManagerProvider graphQLManagerProvider;

    @PostConstruct
    public void init() {
        try {
            log.info("graphql init");
            graphQLManagerProvider.createGraphQL();
        } catch (Exception e) {
            log.error("GraphQLManager#init", e);
        }
    }
}
