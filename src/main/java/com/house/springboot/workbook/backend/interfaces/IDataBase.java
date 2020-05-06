package com.house.springboot.workbook.backend.interfaces;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.house.springboot.workbook.backend.models.T_SYST_FOR;
import com.house.springboot.workbook.backend.models.T_SYST_REP;

public interface IDataBase {
	public int NumberRows(String Table, String Where);
	
	public List<?> getAllRows(String IF, String WH, String PR);
	
	public List<?> getAllRowsForms(String CL, String TB, String WH, String OB);
	
	public List<?> getOneRow(String IF, String WH);
	
	public List<?> getInformation(String OB, String WH, String PR);
	
	public List<?> getInformationForms(String CL, String TB, String WH, String OB);
	
	public String valColString(String column, String table, String where);
	
	public int valColInt(String column, String table, String where);
	
	public boolean valExistsData(String table, String where);
	
	public <T> List<T> getDataList(String table, String where, Class<T> calledClass);
	
	public <T> T getData(String table, String where, Class<T> calledClass);
	
	public <T> List<T> getDataListQuery(String query, Class<T> calledClass);
	
	public String saveOrUpdate(String opcion, String table, T_SYST_FOR t_sys_for, int order, String user_name, String old_fol, String folio, JSONArray data);
	
	public String getFolioString(String object, String year, String key);
	
	public String getFolioSecuence(String object, String column, String folio, String colsec);
	
	public void getUpdateTable(String table, String values, String where);
	
	public List<?> getReport(String user, String process, String table, String filter, String params);
	
	public List<?> getAllRowsReport(String table, String filter);
	
	public void saveOrUpdateVersions(T_SYST_REP t_syst_rep, String user_name, String version, JSONObject data);
	
	public void deleteVersion(T_SYST_REP t_syst_rep, String user_name, String version);
	
	public void saveOrUpdateCatalogs(String object, String option, String table, String column, String user_name, String folio, JSONArray data);
	
	public void deleteCatalogs(String table, String condition);
}
