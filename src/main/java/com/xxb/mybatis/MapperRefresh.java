package com.xxb.mybatis;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MapperRefresh {

	private Logger log = LoggerFactory.getLogger(MapperRefresh.class);

	private Configuration configuration;
	private String[] mapFieldNames;

	public MapperRefresh(Configuration configuration) throws Exception {
		Field loadedResourcesField = configuration.getClass().getDeclaredField("loadedResources");
		loadedResourcesField.setAccessible(true);
		Set<String> loadedResourcesSet = ((Set<String>) loadedResourcesField.get(configuration));
		loadedResourcesSet.forEach(res -> {
			log.info("loadedResources--{}", res);
		});
		this.configuration = configuration;
		mapFieldNames = new String[] { "mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators",
				"sqlFragments" };
	}

	public void refresh(String dirPath) throws Exception {
		File dir = new File(dirPath);
		Field loadedResourcesField = configuration.getClass().getDeclaredField("loadedResources");
		loadedResourcesField.setAccessible(true);
		Set<String> loadedResourcesSet = ((Set<String>) loadedResourcesField.get(configuration));
		Map<String, Map> mapSet = Maps.newHashMap();
		for (String fieldName : mapFieldNames) {
			Field field = configuration.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Map map = ((Map) field.get(configuration));
			mapSet.put(fieldName, map);
		}

		log.info("更新路径:{}", dirPath);
		Collection<File> fileList = getRefreshFile(dir);
		try {
			for (File file : fileList) {
				InputStream inputStream = new FileInputStream(file);
				String resource = file.getAbsolutePath();
				loadedResourcesSet.remove(resource);
				String mapperName = FilenameUtils.getBaseName(file.getName());
				for (String fieldName : mapFieldNames) {
					Map map = mapSet.get(fieldName);
					if (map==null||map.isEmpty()) {
						continue;
					}
					List<String> keys = Lists.newArrayList();
					map.forEach((k, v) -> {
						String key = (String) k;
						if (fieldName.equals("mappedStatements")) {
							MappedStatement ms = (MappedStatement) v;
							String[] msKey = ms.getId().split("\\.");
							if (StringUtils.equals(mapperName, msKey[msKey.length - 2])) {
								keys.add(key);
							}
						}
						if (fieldName.equals("resultMaps")) {
							ResultMap rm = (ResultMap) v;
							String[] msKey = rm.getId().split("\\.");
							if (StringUtils.equals(mapperName, msKey[msKey.length - 2])) {
								keys.add(key);
							}
						}
					});
					keys.forEach(k -> {
						map.remove(k);
					});
				}
				XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration, resource,
						configuration.getSqlFragments());
				xmlMapperBuilder.parse();
			}
		} finally {
			ErrorContext.instance().reset();
		}
	}

	private Collection<File> getRefreshFile(File dir) {
		return FileUtils.listFiles(dir, new String[] { "xml" }, false);
	}

}
