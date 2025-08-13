package com.yedam.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle.Control;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



//init - service - detroy
// *.do -> 실행할 컨트롤.
//요청 url === 실행할 컨트롤.

public class FrontController extends HttpServlet {
sssss
	Map<String, Control> map;
	
	//생성자.
	public FrontController() {
		map = new HashMap<String, Control>();
	}
	@Override
	public void init(ServletConfig config) throws ServletException {
		map.put("/boardList.do", new BoardListControl()); //글목록
		
		
	}
	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
		// url vs. uri
		// http://localhost:8080//HelloJSP/boardList.do => url
		// /HelloJSP/boardList.do						=> uri
		String uri = req.getRequestURI();
		String context = req.getContextPath();		// /HelloJSP
		String page = uri.substring(context.length()); //
		
		Control control = map.get(page);
		control.execute(req, resp);
	}
}
