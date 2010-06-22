/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleRepository;

/**
 *
 */
public class FindModulesServlet extends HttpServlet {
	
	/**
     * 
     */
	private static final long serialVersionUID = 1456733423L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		
		final int iTotalRecords;
		
		// the following parameters are described in http://www.datatables.net/usage/server-side
		final int iDisplayStart = getIntParameter(request, "iDisplayStart", 0);
		final int iDisplayLength = getIntParameter(request, "iDisplayLength", 100);
		final String sSearch = request.getParameter("sSearch");

		// final boolean bEscapeRegex =
		// "true".equalsIgnoreCase(request.getParameter("bEscapeRegex"));
		// final int iColumns = getIntParameter(request, "iColumns", 0);
		final int iSortingCols = getIntParameter(request, "iSortingCols", 0);
		final int[] sortingCols = new int[iSortingCols];
		final String[] sortingDirs = new String[iSortingCols];
		for (int i = 0; i < iSortingCols; i++) {
			final int iSortCol = getIntParameter(request, "iSortCol_" + i, 0);
			final String iSortDir = request.getParameter("iSortDir_" + i);
			sortingCols[i] = iSortCol;
			sortingDirs[i] = iSortDir;
		}
		final String sEcho = request.getParameter("sEcho");

		List<Module> modules;
		try {			
			modules = ModuleRepository.searchModules(sSearch);//RepositoryService.findModules(sSearch, openmrsVersion, excludeMods, sortingCols, sortingDirs);
			iTotalRecords = modules.size();
		}
		catch (Throwable t) {
			System.out.println("Error finding modules: " + t);
			t.printStackTrace(System.out);
			throw new ServletException(t);
		}
		
		final int iTotalDisplayRecords = modules.size();
		int fromIndex = iDisplayStart;
		int toIndex = fromIndex + iDisplayLength;
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (toIndex > iTotalDisplayRecords) {
			toIndex = iTotalDisplayRecords;
		}
		if (fromIndex > toIndex) {
			int aux = toIndex;
			toIndex = fromIndex;
			fromIndex = aux;
		}
		modules = modules.subList(fromIndex, toIndex);
		
		//System.out.println("searching for : " + sSearch + " and excludeModules: " + excludeModules + " and openmrs v: " + openmrsVersion);
		
		final String jsonpcallback = request.getParameter("callback");
		
		// to support cross site scripting and jquery's jsonp
		if (jsonpcallback != null)
			out.print(jsonpcallback + "(");
		
		out.print("{");
		try {
			int sEchoVal = Integer.valueOf(sEcho);
			out.print("\"sEcho\":" + sEchoVal + ",");
		}
		catch (NumberFormatException nfe) {}
		out.print("\"iTotalRecords\":" + iTotalRecords + ",");
		out.print("\"iTotalDisplayRecords\":" + iTotalDisplayRecords + ",");
		out.print("\"sColumns\": \"Action,Name,Version,Author,Description\",");
		out.print("\"aaData\":");
		out.print("[");
		boolean first = true;
		for (Module module : modules) {
			if (first) {
				first = false;
			} else {
				out.print(",");
			}
			out.print("[");
			out.print("\"" + module.getDownloadURL() + "\",");
			out.print("\"" + module.getName() + "\",");
			out.print("\"" + module.getVersion() + "\",");
			out.print("\"" + module.getAuthor() + "\",");
			out.print("\"" + module.getDescription() + "\"");
			out.print("]");
		}
		out.print("]");
		out.print("}");
		
		// to support cross site scripting and jquery's jsonp
		if (jsonpcallback != null)
			out.print(")");
		
	}

	private int getIntParameter(HttpServletRequest request, String param, int defaultVal) {
		try {
			return Integer.valueOf(request.getParameter(param));
		}
		catch (NumberFormatException nfe) {
			return defaultVal;
		}
	}
}
