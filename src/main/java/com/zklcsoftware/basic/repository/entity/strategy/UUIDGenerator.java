package com.zklcsoftware.basic.repository.entity.strategy;

import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import com.zklcsoftware.basic.repository.entity.UUIDEntity;

/**
 * 自定义UUID策略
 */
public class UUIDGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor sessionImplementor, Object o) throws HibernateException {
        if(o instanceof UUIDEntity){
            UUIDEntity entity = (UUIDEntity) o;
            return StringUtils.isNotBlank(entity.getId()) ? entity.getId() : UUID.randomUUID().toString();
        }else{
            throw new HibernateException("使用自定义UUID策略的实体非UUIDEntity：" + o.getClass().getName());
        }
    }

}
