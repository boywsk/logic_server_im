package cn.com.gome.logic.test.javassist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class A {
	static Logger log = LoggerFactory.getLogger(A.class);
    public void method() {
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (int i = 0; i < 1000000; i++) {  
        }
        System.out.println("method1");  
    }  
}  