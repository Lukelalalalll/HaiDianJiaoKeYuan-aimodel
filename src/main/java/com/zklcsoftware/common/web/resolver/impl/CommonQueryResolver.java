package com.zklcsoftware.common.web.resolver.impl;

import com.zklcsoftware.common.web.exception.EngineErrorCodeEnmu;
import com.zklcsoftware.common.web.exception.EngineException;
import com.zklcsoftware.common.web.resolver.IQueryResolver;
import org.springframework.stereotype.Component;

@Component
public class CommonQueryResolver implements IQueryResolver {

    public Object excute(String functionName, Object[] args) {
        throw new EngineException(EngineErrorCodeEnmu.REQ_NOT_EXIT_ERROR);
    }
}