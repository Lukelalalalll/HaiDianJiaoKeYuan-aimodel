package com.zklcsoftware.basic.util;

import java.util.Arrays;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import net.kaczmarzyk.spring.data.jpa.domain.PathSpecification;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;


/**
 * <p>Filters with "in" where-clause (e.g. {@code where firstName in ("Homer", "Marge")}).</p>
 * 
 * <p>Values to match against should be provided as multiple values of a single HTTP parameter, eg.: 
 *  {@code GET http://myhost/customers?firstName=Homer&firstName=Marge}.</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * @author Tomasz Kaczmarzyk
 * @author Maciej Szewczyszyn
 */
public class In<T> extends PathSpecification<T> {

    private String[] allowedValues;
    private Converter converter;

    public In(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length < 1) {
            throw new IllegalArgumentException();
        }
        httpParamValues = httpParamValues[0].split(",");
        this.allowedValues = httpParamValues;
        this.converter = converter;
    }
    
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<?> path = path(root);
        Class<?> typeOnPath = path.getJavaType();
        return path.in(converter.convert(Arrays.asList(allowedValues), typeOnPath));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(allowedValues);
        result = prime * result + ((converter == null) ? 0 : converter.hashCode());
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
        In other = (In) obj;
        if (!Arrays.equals(allowedValues, other.allowedValues))
            return false;
        if (converter == null) {
            if (other.converter != null)
                return false;
        } else if (!converter.equals(other.converter))
            return false;
        return true;
    }
}
