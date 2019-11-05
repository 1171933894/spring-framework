package com.test.main;

import com.test.app.Appconfig;
import com.test.dao.TestDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext
				= new AnnotationConfigApplicationContext(Appconfig.class);
		TestDao testDao = annotationConfigApplicationContext.getBean(TestDao.class);
		testDao.query();
	}
}