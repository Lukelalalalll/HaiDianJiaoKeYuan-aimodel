package com.zklcsoftware.basic.service;

import java.io.Serializable;
import java.util.List;
import com.zklcsoftware.common.web.resolver.IQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * 通用CRUD服务接口
 */
public interface BaseService<T, ID extends Serializable> extends IQueryResolver {

    /**
     * 保存
     *
     * @param entity
     */
    <S extends T> S save(S entity);

    /**
     * 批量保存
     *
     * @param entities
     * @param <S>
     * @return
     */
    <S extends T> Iterable<S> save(Iterable<S> entities);

    /**
     * 通过ID删除
     *
     * @param id
     */
    void delete(ID id);

    /**
     * 删除实体
     *
     * @param entity
     */
    void delete(T entity);

    /**
     * 通过ID批量删除
     *
     * @param ids
     */
    void delete(Iterable<ID> ids);
    /**
     * 通过ID查找
     *
     * @param id
     * @return
     */
    T findById(ID id);
    
    /**
     * 查询所有
     *
     * @return
     */
    List<T> findAll();

    /**
     * 动态条件查询所有
     *
     * @return
     */
    List<T> findAll(Specification<T> specification);
    
    /**
     * 动态条件分页查询
     *
     * @param specification
     * @return
     */
    Page<T> findPage(Specification<T> specification, Pageable pageable);
}
