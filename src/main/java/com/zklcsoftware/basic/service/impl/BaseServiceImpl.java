package com.zklcsoftware.basic.service.impl;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.zklcsoftware.basic.exception.RestException;
import com.zklcsoftware.basic.repository.BaseDao;
import com.zklcsoftware.basic.service.BaseService;

/**
 * 通用CRUD服务
 */
public abstract class BaseServiceImpl<T, ID extends Serializable>  implements BaseService<T, ID> {

    @Autowired
    protected BaseDao<T, ID> baseDao;
    /**
     * 保存
     *
     * @param entity
     */
    @Override
    public <S extends T> S save(S entity) throws RestException {
        return baseDao.save(entity);
    }

    /**
     * 批量保存
     *
     * @param entities
     * @return
     */
    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        return baseDao.saveAll(entities);
    }

    /**
     * 通过ID删除
     *
     * @param id
     */
    @Override
    public void delete(ID id) {
        baseDao.deleteById(id);
    }

    /**
     * 删除实体
     *
     * @param entity
     */
    @Override
    public void delete(T entity) {
        baseDao.delete(entity);
    }

    /**
     * 通过ID批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Iterable<ID> ids) {
        for (ID id : ids) {
            delete(id);
        }
    }
    

    /**
     * 通过ID查找
     *
     * @param id
     * @return
     */
    @Override
    public T findById(ID id) {
        return baseDao.findById(id).isPresent() ? baseDao.findById(id).get() : null;
    }
    
    /**
     * 查询所有
     *
     * @return
     */
    @Override
    public List<T> findAll() {
        return baseDao.findAll();
    }

    /**
     * 动态条件查询所有
     * @param specification
     * @return
     */
    @Override
    public List<T> findAll(Specification<T> specification) {
        return baseDao.findAll(specification);
    }
    
    /**
     * 动态条件分页查询
     *
     * @param specification
     * @param pageable
     * @return
     */
    @Override
    public Page<T> findPage(Specification<T> specification,Pageable pageable) {
        return baseDao.findAll(specification, pageable);
    }

    
}
