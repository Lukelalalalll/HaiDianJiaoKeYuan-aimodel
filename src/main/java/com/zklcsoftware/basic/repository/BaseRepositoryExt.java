package com.zklcsoftware.basic.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;

public interface BaseRepositoryExt {
	
	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @return 分页对象
	 */
	public Page<Map<String, Object>> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable);
	
	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @param rowMapper 行映射
	 * @return 分页对象
	 */
	public <T> Page<T> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, RowMapper<T> rowMapper);
	
	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @param clazz 分页内容中的数据类型
	 * @return 分页对象
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, Class<?> clazz);
	
	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @param clazz 分页内容中的数据类型
	 * @param dbType 数据库类型，默认为Mysql
	 * @return 分页对象
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> findPageObj(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, Class<?> clazz, DbType dbType);
	
	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @param rowMapper 行映射，如果为<code>null</code>则返回的对象为<code>Page&lt;Map&lt;String, Object&gt;&gt;</code>类型
	 * @param dbType 数据库类型，默认为Mysql
	 * @return 分页对象
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, RowMapper<T> rowMapper, DbType dbType);

	/**
	 * 查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param rowMapper 行映射，如果为<code>null</code>则返回的对象为<code>List&lt;Map&lt;String, Object&gt;&gt;</code>类型
	 * @return 数据列表
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(String querySqlName, Map<String, Object> params, RowMapper<T> rowMapper);
	
	/**查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @return 数据列表
	 */
	public List<Map<String, Object>> findList(String querySqlName, Map<String, Object> params);

	/**
	 * 查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param clazz 列表中的数据类型
	 * @return 数据列表
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findListObj(String querySqlName, Map<String, Object> params, Class<?> clazz);
	
	/**
	 * 查询一行数据并转换为对象
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param clazz 列表中的数据类型
	 * @return 一行数据的对象
	 */
	public <T> T findOneObj(String querySqlName, Map<String, Object> params, Class<?> clazz);
	
	/**
	 * 查询一行数据并转换为Map
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @return 一行数据的Map
	 */
	public Map<String, Object> findOneMap(String querySqlName, Map<String, Object> params);
	
	
	
    /**
     * 通过原生sql查询数据 返回指定对象
     * @param cla
     * @param sql
     * @return
     */
    List findBySql(Class cla,String sql, List params);
    
    
    /**
     * 通过原生sql查询数据 返回数组对象
     * @param cla
     * @param sql
     * @return
     */
    List findBySql(String sql, List params);
    
    /**
     * 通过原生sql查询数据 返回map对象
     * @param cla
     * @param sql
     * @return
     */
    List findMapBySql(String sql, List params);
    
    /**
     * 分页查询数据
     * @param entityClass
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNo
     * @return
     */
    public List findByPageSql(Class entityClass, String sql, List params, int pageSize,int pageNo);
    /**
     * 分页查询数据返回数组
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNo
     * @return
     */
    public List findByPageSql(String sql, List params, int pageSize,int pageNo);
    /**
     * 分页查询数据返回map
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNo
     * @return
     */
    public List findMapByPageSql(String sql, List params, int pageSize,int pageNo);
    /**
     * 查询总数
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNo
     * @return
     */
    public long countBySql(String sql, List params);
    
    
    /**
     * 分页查询数据
     * @param entityClass
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNo
     * @return
     */
    public Page findByPageSql(Class entityClass, String sql, List params,String countSql, List countSqlParams, Pageable pageable);
    
    
    /**
     * 分页查询数据返回map
     * @param entityClass
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNo
     * @return
     */
    public Page findMapByPageSql(Class entityClass, String sql, List params,String countSql, List countSqlParams, Pageable pageable);
    
    
 


}
