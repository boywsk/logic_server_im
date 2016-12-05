package cn.com.gome.logic.test.javassist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class Test {
	private final static Logger log = LoggerFactory.getLogger(Test.class);
	
	public static void main(String[] args) throws Exception {
		// 用于取得字节码类，必须在当前的classpath中，使用全称
		CtClass ctClass = ClassPool.getDefault().get("cn.com.gome.logic.test.javassist.A");
		// 需要修改的方法名称
		String mname = "method";
		CtMethod mold = ctClass.getDeclaredMethod(mname);
		
//		StringBuffer body = new StringBuffer();
//		body.append("{\nlong start = System.currentTimeMillis();\n");
//		body.append(mname + "($$);\n");
//		mold.insertAfter("log.info(\"Call to method " + mname
//		+ " took \" +\n (System.currentTimeMillis()-start) + " + "\" ms.\");\n}");
//		
//		mold.setBody(body.toString());
		
		
//		//mold.insertAt(9, "\nlong start = System.currentTimeMillis();\n");
//		mold.insertBefore("\nlong start = System.currentTimeMillis();\n");
//		mold.insertAfter("log.info(\"Call to method " + mname
//				+ " took \" +\n (System.currentTimeMillis()-start) + " + "\" ms.\");\n");
		
		// 修改原有的方法名称
		String nname = mname + "$impl";
		mold.setName(nname);
		// 创建新的方法，复制原来的方法
		CtMethod mnew = CtNewMethod.copy(mold, mname, ctClass, null);
		// 主要的注入代码
		StringBuffer body = new StringBuffer();
		body.append("{\nlong start = System.currentTimeMillis();\n");
		// 调用原有代码，类似于method();($$)表示所有的参数
		body.append(nname + "($$);\n");
		body.append("log.info(\"Call to method " + mname
				+ " took \" +\n (System.currentTimeMillis()-start) + " + "\" ms.\");\n");

		body.append("}");
		// 替换新方法
		mnew.setBody(body.toString());
		// 增加新方法
		ctClass.addMethod(mnew);
		// 类已经更改，注意不能使用A a=new A();，因为在同一个classloader中，不允许装载同一个类两次
		A a = (A) ctClass.toClass().newInstance();
		a.method();
	}
}
