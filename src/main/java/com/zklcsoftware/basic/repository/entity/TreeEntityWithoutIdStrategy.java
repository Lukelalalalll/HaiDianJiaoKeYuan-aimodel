package com.zklcsoftware.basic.repository.entity;


import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import com.zklcsoftware.basic.vo.ITreeNode;

/**
 * 树型实体
 */
@MappedSuperclass
public abstract class TreeEntityWithoutIdStrategy<T extends TreeEntityWithoutIdStrategy> implements Serializable, ITreeNode<T> {

    protected String id; // 树型ID

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
