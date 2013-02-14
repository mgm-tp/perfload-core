/*
 * Copyright (c) 2013 mgm technology partners GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mgmtp.perfload.core.testapp;

import java.io.IOException;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that returns a fibonacci number.
 * 
 * @author rnaegele
 */
public class FibonacciServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final FibonacciComputer fc = new FibonacciComputer();

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		int n = Integer.parseInt(req.getParameter("n"));
		BigInteger fib = fc.compute(n);
		String respBody = fib.toString();
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/plain");
		resp.setContentLength(respBody.length());
		resp.getWriter().print(respBody);
	}
}
