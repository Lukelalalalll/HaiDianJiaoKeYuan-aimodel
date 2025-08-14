package com.zklcsoftware;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.zklcsoftware.basic.repository.SqlMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author audin
 *
 */
@SpringBootApplication
@Slf4j
@ServletComponentScan
@EnableConfigurationProperties(SqlMap.class)
public class StartApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		log.debug("configure(SpringApplicationBuilder) - start"); //$NON-NLS-1$

		SpringApplicationBuilder sab = application.sources(StartApplication.class);

		log.debug("configure(SpringApplicationBuilder) - end"); //$NON-NLS-1$
        return sab;
    }

	public static void main(String[] args) throws Exception {
		log.debug("main(String[]) - start"); //$NON-NLS-1$
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

		log.info("file.encoding>>>"+System.getProperty("file.encoding"));
		log.info("sun.jnu.encoding>>>"+System.getProperty("sun.jnu.encoding"));
		log.info("https.protocols>>>"+System.getProperty("https.protocols"));
		SpringApplication.run(StartApplication.class, args);

		log.debug("main(String[]) - end"); //$NON-NLS-1$
	}

	@Bean
	protected ServletContextListener listener() {
		return new ServletContextListener() {
			@Override
			public void contextInitialized(ServletContextEvent sce) {
				log.info("ServletContext initialized");
			}

			@Override
			public void contextDestroyed(ServletContextEvent sce) {
				log.info("ServletContext destroyed");
			}
		};
	}

	@Bean
	public ConfigurableServletWebServerFactory webServerFactory() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "|{}[]"));
		return factory;
	}
}
