package com.test.jsr;

import org.springframework.lang.NonNull;

/**
 * @Description:
 * @Author: heyuanxin3
 * @Date: 2020/4/13 20:33
 */
public class TestNonNull {
	public void test(@NonNull String str1, String str2) {

	}

	public void test1() {
		test (null, null);
	}
}
