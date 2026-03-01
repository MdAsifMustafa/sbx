package io.github.mdasifmustafa.sbx.template.controller;

public class TestControllerTemplate {
	
	private TestControllerTemplate() {
    }
	
	 public static String generate(String pkg, String className) {
	        return ""
	            + "package " + pkg + ";\n\n"
	            + "import org.springframework.stereotype.Controller;\n\n"
	            + "@Controller\n"
	            + "public class " + className + " {\n"
	            + "}\n";
	    }

}
