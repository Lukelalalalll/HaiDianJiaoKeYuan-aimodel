package com.zklcsoftware.basic.repository.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;
import com.zklcsoftware.basic.vo.ITreeNode;

/**
 * 树型实体
 */
@MappedSuperclass
public abstract class TreeIdEntity<T extends TreeIdEntity> implements Serializable,ITreeNode<T> {

    protected String id; // 树型ID

    @Id
    @GenericGenerator(name = "generator", strategy = "com.xuebang.o2o.core.repository.entity.strategy.TreeIdGenerator")
    @GeneratedValue(generator = "generator")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
