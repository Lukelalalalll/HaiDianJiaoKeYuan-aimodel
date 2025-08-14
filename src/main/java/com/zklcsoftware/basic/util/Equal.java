package com.zklcsoftware.basic.util;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

public class Equal<T> extends PathSpecification<T> {
    
    protected String currentLogin = "current_login";
    protected String expectedValue;
    private Converter converter;
    
    public Equal(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length != 1) {
            throw new IllegalArgumentException();
        }
        
        this.expectedValue = httpParamValues[0];
        if(path.equals("currentUserid") && this.expectedValue.equals(currentLogin)){
            this.expectedValue = getUserGuid();
        }
        
        this.converter = converter;
    }
    
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Class<?> typeOnPath = path(root).getJavaType();
        return cb.equal(path(root), converter.convert(expectedValue, typeOnPath));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((converter == null) ? 0 : converter.hashCode());
        result = prime * result + ((expectedValue == null) ? 0 : expectedValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Equal other = (Equal) obj;
        if (converter == null) {
            if (other.converter != null)
                return false;
        } else if (!converter.equals(other.converter))
            return false;
        if (expectedValue == null) {
            if (other.expectedValue != null)
                return false;
        } else if (!expectedValue.equals(other.expectedValue))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Equal [expectedValue=" + expectedValue + ", converter=" + converter + "]";
    }
}
