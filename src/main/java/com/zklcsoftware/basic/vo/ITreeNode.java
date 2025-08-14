package com.zklcsoftware.basic.vo;

import java.util.List;

/**
 * 树节点接口
 */
public interface ITreeNode<T extends ITreeNode> {

    /**
     * 获取节点ID
     *
     * @return
     */
    String getId();

    /**
     * 获取节点名称
     *
     * @return
     */
    String getName();

    /**
     * 获取父节点
     *
     * @return
     */
    T getParent();

    /**
     * 获取子节点
     *
     * @return
     */
    List<T> getChildren();

    /**
     * 获取排序序号
     * @return
     */
    int getSort();
}
