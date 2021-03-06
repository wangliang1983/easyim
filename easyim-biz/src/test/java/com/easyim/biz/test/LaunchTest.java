package com.easyim.biz.test;


import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import com.easyim.biz.Launch;


@RunWith(SpringRunner.class) //SpringJUnit支持，由此引入Spring-Test框架支持！ 
@SpringBootTest(classes={
		Launch.class})
public abstract class LaunchTest {
	
}