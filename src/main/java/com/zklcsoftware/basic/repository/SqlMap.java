/**
 *
 */
package com.zklcsoftware.basic.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;

/**
 * @author audin
 *
 */
@ConfigurationProperties(prefix="nativesql")
public class SqlMap {
	private Map<String, String> sqls = new HashMap<>();

	public Map<String, String> getSqls() {
		return sqls;
	}
}
