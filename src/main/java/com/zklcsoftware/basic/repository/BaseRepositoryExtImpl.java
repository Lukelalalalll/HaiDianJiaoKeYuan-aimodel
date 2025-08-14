/**
 *
 */
package com.zklcsoftware.basic.repository;

import com.zklcsoftware.basic.util.DAOUtil;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author audin
 *
 */
public abstract class BaseRepositoryExtImpl implements BaseRepositoryExt {

	@Autowired
	protected SqlMap sqlMap;
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	protected EntityManager entityManager;
	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;

	/**
	 * @Description  根据配置查询当前数据库类型
	 * @Author zhushaog
	 * @UpdateTime 2023/4/23 13:21
	 * @return: com.zklcsoftware.basic.repository.DbType
	 * @throws
	 */
	protected DbType getDbType(){
		if("com.mysql.jdbc.Driver".equals(driverClassName)){
			return DbType.Mysql;
		}else if("oracle.jdbc.driver.OracleDriver".equals(driverClassName)){
			return DbType.Oracle;
		}else if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(driverClassName)){
			return DbType.SqlServer2012;
		}else if("org.postgresql.Driver".equals(driverClassName)){
			return DbType.PostgreSQL;
		}else{
			return DbType.Mysql;
		}
	}

	/**
	 * 获取配置文件中的动态SQL语句.
	 * @param sqlName 配置的SQL名称（key）
	 * @return 动态SQL语句.
	 */
	public String _(String sqlName) {
		return sqlMap.getSqls().get(sqlName);
	}

	/**
	 * 处理动态sql.
	 *
	 * @param params
	 *            参数.
	 * @param name
	 *            动态Sql的名称.
	 * @return
	 * @throws Exception
	 */
	public ISqlElement processSql(Map<String, Object> params, String name) {
		ISqlElement rs = DynamicSqlUtil.processSql(params, _(name));
		return rs;
	}

	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @return 分页对象
	 */
	public Page<Map<String, Object>> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable) {
		return findPage(querySqlName, countSqlName, params, pageable, null, getDbType());
	}

	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @param rowMapper 行映射
	 * @return 分页对象
	 */
	public <T> Page<T> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, RowMapper<T> rowMapper) {
		return findPage(querySqlName, countSqlName, params, pageable, rowMapper, getDbType());
	}

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
	public <T> Page<T> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, Class<?> clazz) {
		return (Page<T>) findPage(querySqlName, countSqlName, params, pageable, new ObjectRowMapper<>(clazz));
	}

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
	public <T> Page<T> findPageObj(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, Class<?> clazz, DbType dbType) {
		return (Page<T>) findPage(querySqlName, countSqlName, params, pageable, new ObjectRowMapper<>(clazz), dbType);
	}

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
	public <T> Page<T> findPage(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, RowMapper<T> rowMapper, DbType dbType) {
		//Long a=new Date().getTime();
		ISqlElement seQry = processSql(params, querySqlName);
		ISqlElement seCount = processSql(params, countSqlName);
		//Long b=new Date().getTime();
		//System.out.println("模板解析耗时》》》》》"+(b-a)+"ms");
		Long totalCount = jdbcTemplate.queryForObject(seCount.getSql(), seCount.getParams(), Long.class);
		String sqlQry = null;
		switch (dbType) {
			case Oracle:
				Long offset1 = pageable.getOffset();
				Long offset2 = offset1 + pageable.getPageSize();
				StringBuffer pageSql = new StringBuffer(500)
						.append("SELECT * FROM (SELECT PAGETABLE01.*, ROWNUM RN  FROM (")
						.append(seQry.getSql())
						.append(") PAGETABLE01 WHERE ROWNUM <= ").append(offset2).append(") WHERE RN > ").append(offset1);
				sqlQry = pageSql.toString();
				break;
			case SqlServer2012:
				sqlQry = seQry.getSql() + " OFFSET "+pageable.getOffset()+" ROWS FETCH NEXT "+pageable.getPageSize()+" ROWS ONLY";
				break;
			case PostgreSQL:
				sqlQry = seQry.getSql() + " LIMIT "+pageable.getPageSize()+" OFFSET "+pageable.getOffset();
			default: // mysql
				sqlQry = seQry.getSql() + " LIMIT "+pageable.getOffset()+","+pageable.getPageSize();
				break;
		}
		List<T> content = rowMapper != null ? jdbcTemplate.query(sqlQry, seQry.getParams(), rowMapper) : (List<T>)jdbcTemplate.queryForList(sqlQry, seQry.getParams());
		//System.out.println("查询数据耗时》》》》》"+(new Date().getTime()-b)+"ms");
		return new PageImpl<>(content, pageable, totalCount);
	}

	/**
	 * 查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param rowMapper 行映射，如果为<code>null</code>则返回的对象为<code>List&lt;Map&lt;String, Object&gt;&gt;</code>类型
	 * @return 数据列表
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(String querySqlName, Map<String, Object> params, RowMapper<T> rowMapper) {
		ISqlElement seQry = processSql(params, querySqlName);
		List<T> content = rowMapper != null ? jdbcTemplate.query(seQry.getSql(), seQry.getParams(), rowMapper) : (List<T>)jdbcTemplate.queryForList(seQry.getSql(), seQry.getParams());
		return content;
	}


	/**
	 * 查找分页数据
	 * @param querySqlName 查询语句名称
	 * @param countSqlName 总数语句名称
	 * @param params 数据参数
	 * @param pageable 分页参数
	 * @param rowMapper 行映射，如果为<code>null</code>则返回的对象为<code>Page&lt;Map&lt;String, Object&gt;&gt;</code>类型
	 * @return 分页对象
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> findPageWithIn(String querySqlName, String countSqlName, Map<String, Object> params, Pageable pageable, RowMapper<T> rowMapper) {
		//Long a=new Date().getTime();

		NamedParameterJdbcTemplate namedParameterJdbcTemplate=new NamedParameterJdbcTemplate(jdbcTemplate);
		//ISqlElement seQry = processSql(params, querySqlName);
		//ISqlElement seCount = processSql(params, countSqlName);
		//Long b=new Date().getTime();
		//System.out.println("模板解析耗时》》》》》"+(b-a)+"ms");
		Long totalCount = namedParameterJdbcTemplate.queryForObject(DynamicSqlUtil.processSqlNoPlaceholders(params,_(countSqlName)), params, Long.class);
		String sql = DynamicSqlUtil.processSqlNoPlaceholders(params,_(querySqlName));
		switch (getDbType()) {
			case Oracle:
				long offset1 = pageable.getOffset();
				long offset2 = offset1 + pageable.getPageSize();
				StringBuffer pageSql = new StringBuffer(500)
						.append("SELECT * FROM (SELECT PAGETABLE01.*, ROWNUM RN  FROM (")
						.append(sql)
						.append(") PAGETABLE01 WHERE ROWNUM <= ").append(offset2).append(") WHERE RN > ").append(offset1);
				sql = pageSql.toString();
				break;
			case SqlServer2012:
				sql = sql + " OFFSET "+pageable.getOffset()+" ROWS FETCH NEXT "+pageable.getPageSize()+" ROWS ONLY";
				break;
			case PostgreSQL:
				sql = sql + " LIMIT "+pageable.getPageSize()+" OFFSET "+pageable.getOffset();
				break;
			default: // mysql
				sql = sql + " LIMIT "+pageable.getOffset()+","+pageable.getPageSize();
				break;
		}
		List<T> content = rowMapper != null ? namedParameterJdbcTemplate.query(sql, params, rowMapper) : (List<T>)namedParameterJdbcTemplate.queryForList(sql, params);
		//System.out.println("查询数据耗时》》》》》"+(new Date().getTime()-b)+"ms");
		return new PageImpl<T>(content, pageable, totalCount);
	}

	/**
	 * 查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param rowMapper 行映射，如果为<code>null</code>则返回的对象为<code>List&lt;Map&lt;String, Object&gt;&gt;</code>类型
	 * @return 数据列表
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findListWithIn(String querySqlName, Map<String, Object> params, RowMapper<T> rowMapper) {
		NamedParameterJdbcTemplate namedParameterJdbcTemplate=new NamedParameterJdbcTemplate(jdbcTemplate);
		//ISqlElement seQry = processSql(params, querySqlName);
		List<T> content = rowMapper != null ? namedParameterJdbcTemplate.query(DynamicSqlUtil.processSqlNoPlaceholders(params,_(querySqlName)), params, rowMapper) : (List<T>)namedParameterJdbcTemplate.queryForList(DynamicSqlUtil.processSqlNoPlaceholders(params,_(querySqlName)), params);
		return content;
	}

	/**查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @return 数据列表
	 */
	public List<Map<String, Object>> findList(String querySqlName, Map<String, Object> params) {
		return findList(querySqlName, params, null);
	}

	/**
	 * 查询列表数据
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param clazz 列表中的数据类型
	 * @return 数据列表
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findListObj(String querySqlName, Map<String, Object> params, Class<?> clazz) {
		return (List<T>)findList(querySqlName, params, new ObjectRowMapper<>(clazz));
	}

	/**
	 * 查询一行数据并转换为对象
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @param clazz 列表中的数据类型
	 * @return 一行数据的对象
	 */
	public <T> T findOneObj(String querySqlName, Map<String, Object> params, Class<?> clazz) {
		List<T> list = findListObj(querySqlName, params, clazz);
		return list.size() > 0 ? list.get(0) : null;
	}

	/**
	 * 查询一行数据并转换为Map
	 * @param querySqlName 查询语句名称
	 * @param params 数据参数
	 * @return 一行数据的Map
	 */
	public Map<String, Object> findOneMap(String querySqlName, Map<String, Object> params) {
		List<Map<String,Object>> list = findList(querySqlName, params);
		return list.size() > 0 ? list.get(0) : null;
	}



	@Override
	public List findBySql(Class cla,String sql, List params){
		List list = new ArrayList();
		try {
			Query query=entityManager.createNativeQuery(sql);
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			if(params!=null && params.size()>0){
				for(int i=0;i<params.size();i++){
					query.setParameter(i+1, params.get(i));
				}
			}
			list= DAOUtil.queryMapsToList(cla,query.getResultList());
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public List findBySql(String sql, List params){

		Query query=entityManager.createNativeQuery(sql);

		if(params!=null && params.size()>0){
			for(int i=0;i<params.size();i++){
				query.setParameter(i+1, params.get(i));
			}

		}

		return query.getResultList();
	}


	@Override
	public List findMapBySql(String sql, List params){
		List list = new ArrayList();
		Query query=entityManager.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if(params!=null && params.size()>0){
			for(int i=0;i<params.size();i++){
				query.setParameter(i+1, params.get(i));
			}
		}
		return query.getResultList();
	}

	/**
	 * <p>
	 * 根据sql查询表返回结果集
	 * </p>
	 *
	 * @author zhshg 时间 2012-6-19 下午2:21:29
	 * @param entityClass
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@Override
	public List findByPageSql(Class entityClass, String sql, List params, int pageSize,int pageNo) {
		List list = new ArrayList();
		Query query=null;
		try {
			query=entityManager.createNativeQuery(sql);
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			if (params!=null && params.size() > 0) {
				for (int i = 0; i < params.size(); i++) {
					query.setParameter(i+1, params.get(i));
				}
			}
			if (pageNo != 0 && pageSize != 0) {
				query.setFirstResult((pageNo - 1) * pageSize);
				query.setMaxResults(pageSize);
			}

			list = DAOUtil.queryMapsToList(entityClass, query.getResultList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	/**
	 * <p>
	 * 根据sql查询表返回结果集
	 * </p>
	 *
	 * @author zhshg 时间 2012-6-19 下午2:21:29
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@Override
	public List findByPageSql(String sql, List params,int pageSize,int pageNo) {
		List list = new ArrayList();
		Query query=null;
		try {
			query=entityManager.createNativeQuery(sql);
			if (params.size() > 0) {
				for (int i = 0; i < params.size(); i++) {
					query.setParameter(i+1, params.get(i));
				}
			}
			if (pageNo != 0 && pageSize != 0) {
				query.setFirstResult((pageNo - 1) * pageSize);
				query.setMaxResults(pageSize);
			}

			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public List findMapByPageSql(String sql, List params, int pageSize,int pageNo){
		List list = new ArrayList();
		Query query=entityManager.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if(params!=null && params.size()>0){
			for(int i=0;i<params.size();i++){
				query.setParameter(i+1, params.get(i));
			}
		}
		if (pageNo != 0 && pageSize != 0) {
			query.setFirstResult((pageNo - 1) * pageSize);
			query.setMaxResults(pageSize);
		}


		return query.getResultList();
	}

	@Override
	public long countBySql(String sql, List params) {

		Query query=entityManager.createNativeQuery("SELECT COUNT(1) CNT FROM  ("+sql+")  as mysqltab ");
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if (params!=null && params.size() > 0) {
			for (int i = 0; i < params.size(); i++) {
				query.setParameter(i+1, params.get(i));
			}
		}
		List dataList=query.getResultList();
		Map dataMap=(Map) dataList.get(0);
		BigInteger bint=(BigInteger)dataMap.get("CNT");
		return bint.longValue();


    	/*Query query=entityManager.createNativeQuery(sql);
    	query.unwrap(SQLQuery.class);
        if (params!=null && params.size() > 0) {
          for (int i = 0; i < params.size(); i++) {
  			query.setParameter(i+1, params.get(i));
          }
        }
        return query.getMaxResults();*/
	}


	@Override
	public Page findByPageSql(Class entityClass, String sql, List params,String countSql, List countSqlParams, Pageable pageable) {
		List list = new ArrayList();
		long total=0;
		try {
			Query query=entityManager.createNativeQuery(sql);
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			if(params!=null && params.size()>0){
				for(int i=0;i<params.size();i++){
					query.setParameter(i+1, params.get(i));
				}
			}
			if (pageable!=null) {
				query.setFirstResult(Integer.parseInt(String.valueOf(pageable.getOffset())));
				query.setMaxResults(pageable.getPageSize());
			}
			total=countBySql(countSql,countSqlParams);
			List content = total > pageable.getOffset() ? query.getResultList() : Collections. emptyList();
			list = DAOUtil.queryMapsToList(entityClass, content);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new PageImpl(list, pageable, total);
	}

	@Override
	public Page findMapByPageSql(Class entityClass, String sql, List params,String countSql, List countSqlParams, Pageable pageable) {
		List list = new ArrayList();
		Query query=entityManager.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if(params!=null && params.size()>0){
			for(int i=0;i<params.size();i++){
				query.setParameter(i+1, params.get(i));
			}
		}
		if (pageable!=null) {
			query.setFirstResult(Integer.parseInt(String.valueOf(pageable.getOffset())));
			query.setMaxResults(pageable.getPageSize());
		}
		long total=countBySql(countSql,countSqlParams);
		List content = total > pageable.getOffset() ? query.getResultList() : Collections.emptyList();
		return new PageImpl(content, pageable, total);
	}
}
