package com.zklcsoftware.basic.util;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import com.zklcsoftware.common.web.ExtBaseController;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

public abstract class PathSpecification<T> extends ExtBaseController implements Specification<T> {
    
    protected String path;
    private QueryContext queryContext;
    

    public PathSpecification(QueryContext queryContext, String path) {
        this.queryContext = queryContext;
        this.path = path;
    }
    
    @SuppressWarnings("unchecked")
    protected <F> Path<F> path(Root<T> root) {
        Path<?> expr = null;
        for (String field : path.split("\\.")) {
            if (expr == null) {
                if (queryContext != null && queryContext.get(field) != null) {
                    expr = (Path<T>) queryContext.get(field);
                } else {
                    expr = root.get(field);
                }
            } else {
                expr = expr.get(field);
            }
        }
        return (Path<F>) expr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((queryContext == null) ? 0 : queryContext.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathSpecification other = (PathSpecification) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (queryContext == null) {
            if (other.queryContext != null)
                return false;
        } else if (!queryContext.equals(other.queryContext))
            return false;
        return true;
    }

}

