package com.house.springboot.workbook.backend.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.house.springboot.workbook.backend.interfaces.IDataBase;
import com.house.springboot.workbook.backend.models.T_SYST_FCL;
import com.house.springboot.workbook.backend.models.T_SYST_FOR;
import com.house.springboot.workbook.backend.models.T_SYST_INF;
import com.house.springboot.workbook.backend.models.T_SYST_REP;

@Service
public class DataBaseService implements IDataBase {
	private final Logger logger = Logger.getLogger(DataBaseService.class.getName());
	
	private static String sql = "";

	
	@Autowired 
	DataSource dataSource;
	
	private JdbcTemplate jdbcTemplateObject;
	
    public void setDataSource(DataSource data) {
        this.dataSource = data;
        jdbcTemplateObject = new JdbcTemplate(dataSource);
    }
    
	@Override
	public int NumberRows(String Table, String Where) {        
        int row=0;
        
        sql="select count(*) from "+Table+" "+Where;
        
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"NumberRows()", sql});
        
        row=(int)jdbcTemplateObject.queryForObject(sql, Integer.class);
        
        return row;
	}
	
	@Override
	public List<?> getAllRows(String IF, String WH, String PR) {
		String sql = "SELECT * FROM T_SYST_INF WHERE NMINF="+IF;
		
		T_SYST_INF information = getData("T_SYST_INF","WHERE NMINF="+IF,T_SYST_INF.class);
		
		if(information.getGRPBY() == 1){
			sql = "SELECT "+information.getSELEC()+" FROM "+information.getFROMM().replace("¯PARAMS¯",PR)+" "+WH+" GROUP BY "+information.getSELEC()+" ORDER BY "+information.getORDBY();
		}else {
			if(!information.getFROMM().equals("") && information.getFROMM() != null) {
				sql = "SELECT "+information.getSELEC()+" FROM "+information.getFROMM().replace("¯PARAMS¯",PR)+" "+WH+" ORDER BY "+information.getORDBY();
			}else {
				sql = "SELECT "+information.getSELEC();
			}
		}
        
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getAllRows()", sql});
        
        return jdbcTemplateObject.queryForList(sql);
	}
	
	@Override
	public List<?> getAllRowsForms(String CL, String TB, String WH, String OB) {
		String sql = "";
		
		if(OB.isEmpty()) {
			sql = "SELECT "+CL+" FROM "+TB+" "+WH;
		} else {
			sql = "SELECT "+CL+" FROM "+TB+" "+WH+" ORDER BY "+OB;
		}
        
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getAllRowsForms()", sql});
        
        return jdbcTemplateObject.queryForList(sql);
	}
	
	@Override
	public List<?> getOneRow(String IF, String WH) {
        String sql = "SELECT TOP 1 * FROM "+IF+" "+WH;
        
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getOneRow()", sql});
        
        return jdbcTemplateObject.queryForList(sql);
	}

	@Override
	public List<?> getInformation(String OB, String WH, String PR) {
		List<?> res = getAllRows(OB,WH,PR);
		
        if (res.isEmpty()) {
            return null;
        }
        
		return res;
	}
	
	@Override
	public List<?> getInformationForms(String CL, String TB, String WH, String OB) {		
		List<?> res = getAllRowsForms(CL,TB,WH,OB);
		
        if (res.isEmpty()) {
            return null;
        }
        
		return res;
	}
	
	@Override
	public String valColString(String column, String table, String where) {
        String sql="",
                val="";
         
         if(table.length() > 0){
             sql="select top 1 "+column+" from "+table+" "+where;
         }else{
             sql="select "+column;
         }
         
         logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"valColString()", sql});
         
         val=(String)jdbcTemplateObject.queryForObject(sql, String.class);
         
         return val;
	}
	
	@Override
	public int valColInt(String column, String table, String where) {
        String sql="";
        
        int val=0;
         
        sql="select top 1 "+column+" from "+table+" "+where;
        
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"valColInt()", sql});
         
        val=(int)jdbcTemplateObject.queryForObject(sql, Integer.class);
         
        return val;
	}
	
	@Override
	public boolean valExistsData(String table, String where) {
        String sql="";
        
        int val=0;
         
        sql="select count(*) from "+table+" "+where;
        
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"valExistsData()", sql});
         
        val=(int)jdbcTemplateObject.queryForObject(sql, Integer.class);
         
        return val == 0 ? false:true;
	}
	
	@Override
	public <T> List<T> getDataList(String table, String where, Class<T> calledClasss){
		String sql = "SELECT * FROM "+table+" "+where;
		
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getDataList()", sql});
		
		List<T> data = jdbcTemplateObject.query(sql, new BeanPropertyRowMapper<T>(calledClasss));
		
		return data;
	}
	
	@Override
	public <T> T getData(String table, String where, Class<T> calledClasss){
		String sql = "SELECT TOP 1 * FROM "+table+" "+where;
		
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getData()", sql});
		
		T data = (T) jdbcTemplateObject.queryForObject(sql, new BeanPropertyRowMapper<T>(calledClasss));
		
		return (T) data;
	}
	
	@Override
	public <T> List<T> getDataListQuery(String query, Class<T> calledClasss){
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getDataListQuery()", query});
		
		List<T> data = jdbcTemplateObject.query(query, new BeanPropertyRowMapper<T>(calledClasss));
		
		return data;
	}
	
	@Override
	public String saveOrUpdate(String opcion, String table, T_SYST_FOR t_sys_for, int order, String user_name, String old_fol, String folio, JSONArray data) {
		ArrayList<Object> ValFields = new ArrayList<Object>();
		
        String sql="";
		
        sql="exec dt_temp '"+table+"','"+user_name+"';";
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdate()", sql});
        jdbcTemplateObject.execute(sql);
        
		for(int x=0; x < data.length(); x++) {
			StringBuilder col = new StringBuilder();
			StringBuilder val = new StringBuilder();

            ValFields.clear();
            
    		JSONObject jDat = data.getJSONObject(x);
    		
    		Iterator<?> key = jDat.keys();
    		while(key.hasNext()) {
    			String field = (String)key.next();
    			    			    			
				Object aObj = jDat.get(field);
				
    			if(aObj instanceof String) {
    				if(field.equals("PASSW") && jDat.getString(field).length() > 0 && jDat.getString(field).length() <= 30) {
            			col.append(field).append(",");
            			val.append("?").append(",");
            			
            			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            			
            			String password = jDat.getString(field);
            			String passwordBcrypt = passwordEncoder.encode(password);
            			
            			ValFields.add(passwordBcrypt);
    				} else {
            			col.append(field).append(",");
            			val.append("?").append(",");
        				ValFields.add(jDat.getString(field));
    				}
    			}else if(aObj instanceof Long) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getLong(field));
    			}else if(aObj instanceof Double) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getDouble(field));
    			}else if(aObj instanceof Integer) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getInt(field));
    			}else if(aObj instanceof Boolean) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getBoolean(field));
    			}
    		}
    		
    		if(order != 1) {
    			List <T_SYST_FCL> t_syst_fcl = getDataList("T_SYST_FCL", "WHERE TABLA='"+table+"' AND RELAC != ''", T_SYST_FCL.class);
    			
    			for(T_SYST_FCL fcl:t_syst_fcl) {
        			col.append(fcl.getCOLMN()).append(",");
        			val.append("?").append(",");
        			ValFields.add("");    				
    			}
    		}
    		
			col.deleteCharAt(col.length()-1);
			val.deleteCharAt(val.length()-1);

    		Object[] p = ValFields.toArray();
    		
    		sql="INSERT INTO "+table+user_name+"("+col+") VALUES("+val+")";
    		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdate()", sql});
    		jdbcTemplateObject.update(sql, p);
    	}
		
		if(opcion.equals("CF") && order == 1) {
			if(t_sys_for.getGNKEY().length() > 0 && (folio.length() >= 30 || folio.length() <= 0)) {
				String gnkey = t_sys_for.getGNKEY();
				gnkey = gnkey.replace("|TEMPO|", table+user_name);
				gnkey = gnkey.replace("|USUAR|", user_name);
				gnkey = gnkey.replace("|MODUL|", t_sys_for.getMODUL());
				gnkey = gnkey.replace("|CATEG|", t_sys_for.getCATEG());
				gnkey = gnkey.replace("|FORMA|", t_sys_for.getFORMA());
				gnkey = gnkey.replace("|COLMN|", t_sys_for.getCOLMN());
				gnkey = gnkey.replace("|FOLIO|", t_sys_for.getFOLIO());
				
				logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdate()", gnkey});
				jdbcTemplateObject.execute(gnkey);
				
				folio = valColString(t_sys_for.getFNAME(), table+user_name, "");
			}
		}
		
        String[] fol = folio.split("¦");
        String[] ofl = old_fol.split("¦");
        
        List <T_SYST_FCL> t_syst_fcl = getDataList("T_SYST_FCL", "WHERE TABLA='"+table+"' AND LLAVE=1 AND LEN(COLMN)=5", T_SYST_FCL.class);
        
        String where = "",
        	   set = "";
        
        for(int x=0; x < t_syst_fcl.size(); x++) {
        	set += x==0 ? t_syst_fcl.get(x).getCOLMN()+"='"+fol[x]+"'":","+t_syst_fcl.get(x).getCOLMN()+"='"+fol[x]+"'";
        	
        	if(fol.length == ofl.length) {
        		where += x==0 ? t_syst_fcl.get(x).getCOLMN()+"='"+ofl[x]+"'":" AND "+t_syst_fcl.get(x).getCOLMN()+"='"+ofl[x]+"'";
        	}
        }
        
        sql = "UPDATE "+table+user_name+" SET "+set;
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdate()", sql});
        jdbcTemplateObject.update(sql);
        
        if(order == 1 && where.length() > 0) {        	
        	sql = "DELETE FROM "+table+" WHERE "+where;
        	logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdate()", sql});
        	jdbcTemplateObject.update(sql);
		}
		
		
        sql="EXEC dt_act '"+table+"','"+t_sys_for.getCOLMN()+"','"+order+"','"+user_name+"','"+opcion+"','"+folio+"','"+old_fol+"';";
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdate()", sql});
        jdbcTemplateObject.execute(sql);
        
        return folio;
	}
	
	@Override
	public String getFolioString(String object, String year, String key) {
        String sql="",
    		   month="";
        
        month = valColString("dbo.fn_fol_mes('"+year+"')","","");
        
        sql="exec dt_folio '"+object+"','"+year+"','"+month+"'";
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getFolioString()", sql});
        jdbcTemplateObject.execute(sql);
		
		return valColString("dbo.fn_folio('"+key+"','"+year+"','"+month+"',SECUE)","T_SYST_FOL","where FORMA='"+object+"' and YYYYY='"+year+"' and MMMMM='"+month+"'");
	}
	
	@Override
	public String getFolioSecuence(String object, String column, String folio, String colsec) {
		String table = valColString("TABLA","T_SYST_FTB","where FORMA='"+object+"' and ORDEN=1");
		int secuence = 0;
		
		secuence = valColInt("isnull(max("+colsec+"),0)",table,"where "+column+"='"+folio+"' and isnumeric("+colsec+")=1");
		
		secuence += 1;
		
		return String.valueOf(secuence);
	}
	
	@Override
	public void getUpdateTable(String table, String values, String where) {
		String sql="";
		
		sql="UPDATE "+table+" SET "+values+" "+where;
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getUpdateTable()", sql});
		jdbcTemplateObject.update(sql);
	}
	
	@Override
	public List<?> getReport(String user, String process, String table, String filter, String params) {
		String sql="";
		boolean drop = false;
		
		if(process != null && !process.isEmpty()) {
			drop = true;
			
			process = process.replace("|PARAMS|",params);
			
	        sql="exec "+process;
	        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getReport()", sql});
	        jdbcTemplateObject.execute(sql);
	        
			table += user;
		}
		
		List<?> res = getAllRowsReport(table,filter);
		
		if(drop) {
	        sql="exec dt_drop "+table;
	        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"getReport()", sql});
	        jdbcTemplateObject.execute(sql);
		}
		
        if (res.isEmpty()) {
            return null;
        }
        
		return res;
	}
	
	@Override
	public List<?> getAllRowsReport(String table, String filter) {
		String sql = "SELECT * FROM "+table+" "+filter;
		        
		logger.log(Level.INFO, "Query: {0}", new Object[]{sql});
        
        return jdbcTemplateObject.queryForList(sql);
	}
	
	@Override
	public void saveOrUpdateVersions(T_SYST_REP t_syst_rep, String user_name, String version, JSONObject data) {
		ArrayList<Object> ValFields = new ArrayList<Object>();
		
		String sql="";
		if(valExistsData("T_SYST_RVR","WHERE MODUL='"+t_syst_rep.getMODUL()+"' AND RPCTG='"+t_syst_rep.getRPCTG()+"' AND REPOR='"+t_syst_rep.getREPOR()+"' AND USUAR='"+user_name+"' AND VERSI='"+version+"'")) {
			ValFields.add(data.getString("COLUM"));
			ValFields.add(data.getString("FILTE"));
			ValFields.add(data.getString("ORDBY"));
			
			Object[] p = ValFields.toArray();
			
			sql="UPDATE T_SYST_RVR SET COLUM=?,FILTE=?,ORDBY=? WHERE MODUL='"+t_syst_rep.getMODUL()+"' AND RPCTG='"+t_syst_rep.getRPCTG()+"' AND REPOR='"+t_syst_rep.getREPOR()+"' AND USUAR='"+user_name+"' AND VERSI='"+version+"'";
			logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdateVersions()", sql});
			jdbcTemplateObject.update(sql, p);
		} else {
			ValFields.add(t_syst_rep.getMODUL());
			ValFields.add(t_syst_rep.getRPCTG());
			ValFields.add(t_syst_rep.getREPOR());
			ValFields.add(user_name);
			ValFields.add(version);
			ValFields.add(data.getString("DESCR"));
			ValFields.add(data.getString("COLUM"));
			ValFields.add(data.getString("FILTE"));
			ValFields.add(data.getString("ORDBY"));
			
			Object[] p = ValFields.toArray();
			
    		sql="INSERT INTO T_SYST_RVR(MODUL,RPCTG,REPOR,USUAR,VERSI,DESCR,COLUM,FILTE,ORDBY) VALUES(?,?,?,?,?,?,?,?,?)";
    		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdateVersions()", sql});
    		jdbcTemplateObject.update(sql, p);
		}
	}
	
	@Override
	public void deleteVersion(T_SYST_REP t_syst_rep, String user_name, String version) {
		String sql="DELETE FROM T_SYST_RVR WHERE MODUL='"+t_syst_rep.getMODUL()+"' AND RPCTG='"+t_syst_rep.getRPCTG()+"' AND REPOR='"+t_syst_rep.getREPOR()+"' AND USUAR='"+user_name+"' AND VERSI='"+version+"'";
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"deleteVersion()", sql});
		jdbcTemplateObject.update(sql);
	}
	
	@Override
	public void saveOrUpdateCatalogs(String object, String option, String table, String column, String user_name, String folio, JSONArray data) {
		ArrayList<Object> ValFields = new ArrayList<Object>();
		
        String sql="";
		
        sql="exec dt_temp '"+table+"','"+user_name+"';";
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdateCatalogs()", sql});
        jdbcTemplateObject.execute(sql);
        
		for(int x=0; x < data.length(); x++) {
			StringBuilder col = new StringBuilder();
			StringBuilder val = new StringBuilder();

            ValFields.clear();
            
    		JSONObject jDat = data.getJSONObject(x);
    		
    		Iterator<?> key = jDat.keys();
    		while(key.hasNext()) {
    			String field = (String)key.next();
    			    			    			
				Object aObj = jDat.get(field);
				
    			if(aObj instanceof String) {
    				if(field.equals("PASSW") && jDat.getString(field).length() > 0 && jDat.getString(field).length() <= 30) {
            			col.append(field).append(",");
            			val.append("?").append(",");
            			
            			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            			
            			String password = jDat.getString(field);
            			String passwordBcrypt = passwordEncoder.encode(password);
            			
            			ValFields.add(passwordBcrypt);
    				} else {
            			col.append(field).append(",");
            			val.append("?").append(",");
        				ValFields.add(jDat.getString(field));
    				}
    			}else if(aObj instanceof Long) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getLong(field));
    			}else if(aObj instanceof Double) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getDouble(field));
    			}else if(aObj instanceof Integer) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getInt(field));
    			}else if(aObj instanceof Boolean) {
        			col.append(field).append(",");
        			val.append("?").append(",");
    				ValFields.add(jDat.getBoolean(field));
    			}
    		}
    		    		
			col.deleteCharAt(col.length()-1);
			val.deleteCharAt(val.length()-1);

    		Object[] p = ValFields.toArray();
    		
    		sql="INSERT INTO "+table+user_name+"("+col+") VALUES("+val+")";
    		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdateCatalogs()", sql});
    		jdbcTemplateObject.update(sql, p);
    		
    		sql="UPDATE "+table+user_name+" SET "+column+"='"+folio+"'";
    		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdateCatalogs()", sql});
    		jdbcTemplateObject.update(sql);
    	}

		
        sql="EXEC dt_act_cat '"+object+"','"+table+"','"+user_name+"','"+option+"';";
        logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"saveOrUpdateCatalogs()", sql});
        jdbcTemplateObject.execute(sql);
	}
	
	@Override
	public void deleteCatalogs(String table, String condition) {
		String sql="DELETE FROM "+table+" WHERE "+condition;
		logger.log(Level.INFO, "{0} Query: {1}", new Object[]{"deleteCatalogs()", sql});
		jdbcTemplateObject.update(sql);
	}
}
