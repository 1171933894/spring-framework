package com.test.service;

import com.test.dao.TestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestService {
	private final TestDao testDao;

	@Autowired
	public TestService(TestDao testDao) {
		this.testDao = testDao;
	}
}