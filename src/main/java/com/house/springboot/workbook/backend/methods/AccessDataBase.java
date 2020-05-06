package com.house.springboot.workbook.backend.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.house.springboot.workbook.backend.classes.MenuTree;
import com.house.springboot.workbook.backend.models.MENU;
import com.house.springboot.workbook.backend.models.T_SYST_CTG;
import com.house.springboot.workbook.backend.models.T_SYST_CCL;
import com.house.springboot.workbook.backend.models.T_SYST_MOD;
import com.house.springboot.workbook.backend.models.T_SYST_FCL;
import com.house.springboot.workbook.backend.models.T_SYST_FOR;
import com.house.springboot.workbook.backend.models.T_SYST_FTB;
import com.house.springboot.workbook.backend.models.T_SYST_PCL;
import com.house.springboot.workbook.backend.models.T_SYST_PDF;
import com.house.springboot.workbook.backend.models.T_SYST_TRN;
import com.house.springboot.workbook.backend.models.T_SYST_REP;
import com.house.springboot.workbook.backend.models.T_SYST_RHD;
import com.house.springboot.workbook.backend.models.T_SYST_RPR;
import com.house.springboot.workbook.backend.models.T_SYST_RCL;
import com.house.springboot.workbook.backend.models.T_SYST_RCT;
import com.house.springboot.workbook.backend.services.DataBaseService;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class AccessDataBase {
	private final Logger logger = Logger.getLogger(AccessDataBase.class.getName());
	
	ApplicationContext context = new ClassPathXmlApplicationContext("SpringJdbc.xml");
	
	DataBaseService DBSI = (DataBaseService) context.getBean("DBImpl");

	private BufferedReader br;
	
	public int NumberRows(String Table, String Where){
        int rows=0;
        
        try{            
            rows=DBSI.NumberRows(Table,Where);
            
        }catch (Exception e){
        	logger.log(Level.SEVERE, "Error: {0}", new Object[]{e.toString()});
        }
        
        return rows;
	}
	
	public List<?> getAllRows(String OB, String WH, String UN){
		List<?> Contenido = null;
		
		try {
			Contenido=DBSI.getAllRows(OB,WH, UN);
		}catch (Exception e){
			logger.log(Level.SEVERE, "Error: {0}", new Object[]{e.toString()});
        }
		
		return Contenido;
	}
	
	public List<?> getOneRow(String OB, String WH){
		List<?> Contenido = null;
		
		try {
			Contenido=DBSI.getOneRow(OB,WH);
		}catch (Exception e){
			logger.log(Level.SEVERE, "Error: {0}", new Object[]{e.toString()});
        }
		
		return Contenido;
	}
	
	public String getMenu(String User){
		
		List <MENU> item_list=DBSI.getDataList("dbo.fn_MENU('"+User+"')", "WHERE ORNV1=0", MENU.class);
		
		String result = "";
		
		if(item_list.size() > 0) {
			MENU menu = item_list.get(0);
			
			MenuTree tree = new MenuTree(menu);
			
			tree.build(User);
			
			JsonObject jsonResult = tree.getStructure();
			
			result = jsonResult.toString();
		}
		
		return result;
	}
	
	public String getAccess(String User) {
		String result = "";
		
		List <MENU> access_list=DBSI.getDataList("dbo.fn_MENU('"+User+"')", "WHERE ACCES=1 AND LEN(UURLL) > 0", MENU.class);
		
		if(access_list.size() > 0) {
			for(MENU access:access_list) {
				result += access.getGRUPO()+":"+access.getID() + ",";
			}
			
			result = result.substring(0,result.length()-1);
		}
		
		return result;
	}
	
	public Map<String, Object> getInformation(String OB, String WH, String PR){
		Map<String, Object> response = new HashMap<String, Object>();
		
		List<?> content;
		
			if(WH.isEmpty()) {
				content=DBSI.getInformation(OB, "", PR);
			}else {
				content=DBSI.getInformation(OB, "WHERE "+WH, PR);
			}
		
		response.put("data", content);
		
		return response;
	}
	
	public Map<String, Object> getTranlations(String language){
		Map<String, Object> response = new HashMap<String, Object>();
		
		List <T_SYST_TRN> t_syst_trn=DBSI.getDataListQuery("SELECT LANGU,SOURC FROM T_SYST_TRN WHERE LANGU='"+language+"'", T_SYST_TRN.class);
		
		response.put("data", t_syst_trn.toString());
		
		return response;
	}
	
	public String prepareData(String opcion, String object, String user_name, String folio, String json, String uploads) {
		T_SYST_FOR t_syst_for =DBSI.getData("T_SYST_FOR", "WHERE FORMA='"+object+"'", T_SYST_FOR.class);
		
		List <T_SYST_FTB> t_syst_ftb=DBSI.getDataList("T_SYST_FTB", "WHERE FORMA='"+object+"' ORDER BY ORDEN", T_SYST_FTB.class);
		
		String old_fol = folio;
		
		if(opcion.equals("SI") && folio.length() <= 0) {
			folio=unique().substring(0,30);
		} else if(opcion.equals("SI") && folio.substring(folio.length()-1,folio.length()).equals("¦")) {
			folio+=unique().substring(0,30);
		}
		
		JSONObject jObj = new JSONObject(json);
		
		for(T_SYST_FTB ftb:t_syst_ftb) {
			String Obj = ftb.getTABLA();
			
			JSONArray jVal = new JSONArray();
			
			if(ftb.getVROWS()) {
				jVal = jObj.getJSONArray(Obj);
			}else {
				JSONObject jDat = jObj.getJSONObject(Obj);
				jVal.put(jDat);
			}
			
			folio = DBSI.saveOrUpdate(opcion, Obj, t_syst_for, ftb.getORDEN(), user_name, old_fol, folio, jVal);
		}
		
		if((opcion.equals("CF") || opcion.equals("CL")) && old_fol.length() >= 30) {
			changeDirUpload(user_name, t_syst_for, t_syst_ftb.get(0), old_fol, folio, uploads);
		}
		
		return folio;
	}
	
    public static String unique() {
        SecureRandom ng = new SecureRandom();
        long MSB = 0x8000000000000000L;
        
        return Long.toHexString(MSB | ng.nextLong()) + Long.toHexString(MSB | ng.nextLong());
    }
    
    public static boolean isNumeric(String cadena){
    	try {
    		Integer.parseInt(cadena);
    		return true;
    	} catch (NumberFormatException nfe){
    		return false;
    	}
    }
    
    public Map<String, Object> retriveData(String object, String where){
    	Map<String, Object> response = new HashMap<String, Object>();
    	
    	List <T_SYST_FTB> t_syst_ftb=DBSI.getDataList("T_SYST_FTB", "WHERE FORMA='"+object+"' ORDER BY ORDEN", T_SYST_FTB.class);
    	
    	for(T_SYST_FTB list:t_syst_ftb) {
        	List<?> data;
        	
    		List <T_SYST_FCL> t_syst_fcl=DBSI.getDataList("T_SYST_FCL", "WHERE FORMA='"+object+"' AND TABLA='"+list.getTABLA()+"' ORDER BY IDCOL", T_SYST_FCL.class);
    		
    		StringBuilder columns = new StringBuilder();
    		
    		boolean firstRow = true;
    		
    		for(T_SYST_FCL col:t_syst_fcl) {
    			if(firstRow) {
    				firstRow = !firstRow;
    			}else {
    				columns.append(",");
    			}
    			
    			if(col.getTTIPO().equals("datetime")) {
    				columns.append("convert(varchar,").append(col.getCOLMN()).append(",103)+' '+convert(varchar, ").append(col.getCOLMN()).append(",108) AS ").append(col.getCOLMN());
    			} else if(col.getTTIPO().equals("date")) {
    				columns.append("convert(varchar,").append(col.getCOLMN()).append(",103) AS ").append(col.getCOLMN());
    			} else {
    				columns.append(col.getCOLMN());
    			}
    			
    			if(firstRow) {
    				firstRow = !firstRow;
    			}
    		}
    		
    		if(list.getVROWS()) {
    			data = DBSI.getInformationForms(columns.toString(), list.getTABLA(), "WHERE "+where, "ID");
    		} else {
    			data = DBSI.getInformationForms(columns.toString(), list.getTABLA(), "WHERE "+where, "");
    		}
    		
    		response.put(list.getTABLA(), data);
    	}
    	
    	return response;
    }
    
    public String createDirUpload(String user_name, String object, String folio, String uploads) {
    	T_SYST_FOR t_syst_for =DBSI.getData("T_SYST_FOR", "WHERE FORMA='"+object+"'", T_SYST_FOR.class);
    	T_SYST_FTB t_syst_ftb=DBSI.getData("T_SYST_FTB", "WHERE FORMA='"+object+"' AND ORDEN=1", T_SYST_FTB.class);
    	
    	String modul = t_syst_for.getMODUL(),
      		   categ = t_syst_for.getCATEG(),
      		   forma = t_syst_for.getFORMA(),
      		   puplo = t_syst_for.getPUPLO(),
      		   fname = t_syst_for.getFNAME();
    	
    	puplo = puplo.replace("|MODUL|", modul);
    	puplo = puplo.replace("|CATEG|", categ);
    	puplo = puplo.replace("|FORMA|", forma);
    	puplo = DBSI.valColString(puplo, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	String key = DBSI.valColString(fname, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	StringBuilder path = new StringBuilder();
    	
    	path.append(uploads).append("/");
    	path.append(puplo);
    	
    	if(folio.length() == 30) {
    		path.append(folio);
    	}else {
    		path.append(key);
    	}
    	
    	path.append("/");
    	
        File Dir=new File(path.toString());
        
        Dir.mkdirs();
        
    	return path.toString();
    }
    
    public void changeDirUpload(String user_name, T_SYST_FOR t_syst_for, T_SYST_FTB t_syst_ftb, String old_fol, String folio, String uploads) {
    	
    	String modul = t_syst_for.getMODUL(),
     		   categ = t_syst_for.getCATEG(),
     		   forma = t_syst_for.getFORMA(),
     		   puplo = t_syst_for.getPUPLO(),
     		   fname = t_syst_for.getFNAME();
    	
    	puplo = puplo.replace("|MODUL|", modul);
    	puplo = puplo.replace("|CATEG|", categ);
    	puplo = puplo.replace("|FORMA|", forma);
    	puplo = DBSI.valColString(puplo, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	String file = DBSI.valColString(fname, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	StringBuilder oldpath = new StringBuilder();
	   
    	oldpath.append(uploads).append("/");
    	oldpath.append(puplo);
    	oldpath.append(old_fol).append("/");
	   
	   
    	StringBuilder newpath = new StringBuilder();	   
    		   
    	newpath.append(uploads).append("/");
    	newpath.append(puplo);
    	newpath.append(file);
    	newpath.append("/");
    	
        File Dir=new File(oldpath.toString());
        File newDir=new File(newpath.toString());
        
        Dir.renameTo(newDir);
    }
    
    public String readerXML(File file) {
    	String data = "";
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			StringBuilder sb = new StringBuilder();

			while((line=br.readLine())!= null){
			    sb.append(line.trim());
			}

	        try {
	        	JSONObject xmlJSONObj = XML.toJSONObject(sb.toString());
	        	data = xmlJSONObj.toString();
	        	
        	} catch (JSONException je) {
        		data = "Error en la lectura del XML";
        		logger.log(Level.SEVERE, "Error: {0}", new Object[]{je.toString()});
        	}
		} catch (FileNotFoundException e) {
			data = "Error en la lectura del XML";
			logger.log(Level.SEVERE, "Error: {0}", new Object[]{e.toString()});
		} catch (IOException e) {
			data = "Error en la lectura del XML";
			logger.log(Level.SEVERE, "Error: {0}", new Object[]{e.toString()});
		}
    	
    	return data;
    }
    
    public String createPDF(String option, String user_name, String object, String folio, String uploads, String templates, String pdfs) {
    	T_SYST_FOR t_syst_for =DBSI.getData("T_SYST_FOR", "WHERE FORMA='"+object+"'", T_SYST_FOR.class);
    	T_SYST_FTB t_syst_ftb=DBSI.getData("T_SYST_FTB", "WHERE FORMA='"+object+"' AND ORDEN=1", T_SYST_FTB.class);
    	
    	String modul = t_syst_for.getMODUL(),
      		   categ = t_syst_for.getCATEG(),
      		   forma = t_syst_for.getFORMA(),
			   puplo = t_syst_for.getPUPLO(),
      		   ppdfs = t_syst_for.getPPDFS(),
      		   fname = t_syst_for.getFNAME();
    	
    	puplo = puplo.replace("|MODUL|", modul);
    	puplo = puplo.replace("|CATEG|", categ);
    	puplo = puplo.replace("|FORMA|", forma);
    	puplo = DBSI.valColString(puplo, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	StringBuilder path_upl = new StringBuilder();
    	
    	path_upl.append(uploads).append("/");
    	path_upl.append(puplo);
    	
    	path_upl.append(folio);
    	
    	path_upl.append("/");
    	
    	ppdfs = ppdfs.replace("|MODUL|", modul);
    	ppdfs = ppdfs.replace("|CATEG|", categ);
    	ppdfs = ppdfs.replace("|FORMA|", forma);
    	ppdfs = DBSI.valColString(ppdfs, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	StringBuilder path_pdf = new StringBuilder();
    	
    	path_pdf.append(pdfs).append("/");
    	path_pdf.append(ppdfs);
    	    	
    	String url_t = "",
    		   url_d = "",
			   url_b = "",
			   url_m = "",
			   url_f = "",
    		   url_u = "",
    		   table_row = "";
    	
    	ArrayList<String> PDFS = new ArrayList<String>();
    	
    	List <T_SYST_PDF> t_syst_pdf=DBSI.getDataList("T_SYST_PDF", "WHERE FORMA='"+object+"' ORDER BY ORDEN", T_SYST_PDF.class);
    	
    	try{
	    	for(T_SYST_PDF data_pdf:t_syst_pdf) {
	    		String ptemp = data_pdf.getPTEMP(); 
	    		
	    		ptemp = ptemp.replace("|MODUL|", modul);
	    		ptemp = ptemp.replace("|CATEG|", categ);
	    		ptemp = ptemp.replace("|FORMA|", forma);
	        	
	    		StringBuilder path_tem = new StringBuilder();
	    		
	    		path_tem.append(templates).append("/").append(option).append("/");
	    		path_tem.append(ptemp);
	        	
	    		url_t = path_tem.toString()+data_pdf.getPPDFF()+".pdf";
				url_d = path_pdf.toString();
						
				if(data_pdf.getOPTIO() != 2) {
					table_row = data_pdf.getROWSS() == 1 || data_pdf.getOPTIO() == 3 ? DBSI.valColString("OBJEC", "T_SYST_PCL", "WHERE FORMA='"+object+"' AND PPDFF='"+data_pdf.getPPDFF()+"' AND TDFML=1"):DBSI.valColString("OBJEC", "T_SYST_PCL", "WHERE FORMA='"+object+"' AND PPDFF='"+data_pdf.getPPDFF()+"' AND TDFML=2");
				}
				
	    		List <T_SYST_PCL> t_syst_pcl=DBSI.getDataList("T_SYST_PCL", "WHERE FORMA='"+object+"' AND PPDFF='"+data_pdf.getPPDFF()+"' ORDER BY TDFML,IDFML", T_SYST_PCL.class);
	    		
	            if(data_pdf.getOPTIO() == 1 || data_pdf.getOPTIO() == 3) {
	                int row=data_pdf.getROWSS(),
	                    sheet=0,
	                    res=0,
	                    id=1,
	                    rw=data_pdf.getROWSS(),
	                    Fil=0;
	                
	                if(data_pdf.getOPTIO() == 3) {
	                	Fil = row;
	                } else {
	                	Fil=DBSI.valColInt("count(*)", table_row, "WHERE "+data_pdf.getCLFOL()+"='"+folio+"'" + data_pdf.getCONDI());
	                }
	                
	                if(Fil > 0) {
		                File Dir=new File(url_d);
		                
		                Dir.mkdirs();
		                
		                if(Fil != 1) {
		                    res=Fil%row;
		                    sheet=res > 0 ? (Fil/row)+1:Fil/row;
		                }else {
		                    res=0;
		                    sheet=1;
		                }
		                
		                for(int x=1; x <= sheet; x++){
		                	if(option.equals("VP")) {
		                		url_b = path_pdf.toString()+data_pdf.getPPDFF()+"_"+user_name+"_"+x+".pdf";
		                		PDFS.add(url_b);
		                	} else if(option.equals("OK")) {
		                		url_b = path_pdf.toString()+data_pdf.getPPDFF()+"_"+folio+"_"+x+".pdf";
		                		PDFS.add(url_b);
		                	}
		                	
		                	PdfReader reader = new PdfReader(url_t);
		                	
		                	PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(url_b));
		                	
		                	AcroFields form1 = stamp.getAcroFields();
		                	
	                        String column="",
	                               formul="";
		                	
		            		for(T_SYST_PCL data_pcl:t_syst_pcl) {
	                            switch(data_pcl.getTDFML()) {
	                            	case 1:
	                            		column=DBSI.valColString(data_pcl.getCOLUM(),data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"'");
	                            		formul=data_pcl.getFRPDF();
	                            		form1.setField(formul, column);
	                            		break;
	                            	case 2:
	                            		int sc=1;
	                            		
	                            		for(int i=id; i <= rw; i++){
	                            			if(DBSI.valExistsData(data_pcl.getOBJEC(), "where "+data_pdf.getCLFOL()+"='"+folio+"' and ID="+i)) {
	                            				column=DBSI.valColString(data_pcl.getCOLUM(),data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"' and ID="+i);
	                            				formul=data_pcl.getFRPDF();
	                            				form1.setField(formul+sc, column);
	                            			} else {
	                            				break;
	                            			}
	                            			
	                            			sc++;
	                            		}
	                            		
	                            		break;
	                            	case 3:
	                                    int min=((x-1)*row)+1,
	                                    	max=(row*x);
	                                    
	                    				column=DBSI.valColString(data_pcl.getCOLUM(),data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"' and ID between "+min+" AND "+max);
	                    				formul=data_pcl.getFRPDF();
	                    				form1.setField(formul, column);
	                            		break;
	                            	case 4:
	                                    String pg=String.valueOf(x),
	                                    	   tp=String.valueOf(sheet);
	                                    
	                                    if(data_pcl.getCOLUM().contains("|PG|")){
	                                        column=data_pcl.getCOLUM().replace("|PG|",pg);
	                                    }else if(data_pcl.getCOLUM().contains("|PT|")){
	                                        column=data_pcl.getCOLUM().replace("|PT|",tp);
	                                    }
	                                    
	                    				formul=data_pcl.getFRPDF();
	                    				form1.setField(formul, column);
	                            		break;
	                        		default:
	                        			break;
	                            }
		            		}
		            		
	                        stamp.setFormFlattening(true);
	                        stamp.close();
	                        
	                        id += row;
	                        rw += row;
		                } 
	                }
	            } else if(data_pdf.getOPTIO() == 2) {
	            	for(T_SYST_PCL data_pcl:t_syst_pcl) {
	            		String file="";
	            		
	            		switch(data_pcl.getTDFML()) {
	            			case 1:
	            				file=DBSI.valColString(data_pcl.getCOLUM(),data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"'");
	            				
	            				if(file.length() >= 4) {
		            				if(right(file, 4).equals(".pdf")) {
			            				if(file.length() > 0){
			            					url_u = path_upl.toString()+file;
		                                    PDFS.add(url_u);
			            				}
		            				}
	            				}
	            				
	            				break;
	            			case 2:
	                            int mn=DBSI.valColInt("min(ID)",data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"'"),
                                	mx=DBSI.valColInt("max(ID)",data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"'");
	                            
	                            for(int i=mn; i <= mx; i++){
	                            	file=DBSI.valColString(data_pcl.getCOLUM(),data_pcl.getOBJEC(),"where "+data_pdf.getCLFOL()+"='"+folio+"' AND ID="+i);
	                            	
	                            	if(file.length() >= 4) {
		                            	if(right(file, 4).equals(".pdf")) {
				            				if(file.length() > 0){
				            					url_u = path_upl.toString()+file;
			                                    PDFS.add(url_u);
				            				}
		                            	}
	                            	}
	                            }
	            				break;
            				default:
            					break;
	            		}
	            	}
	            }
	    	}
	    	
	    	if(option.equals("VP")) {
	    		url_m = path_pdf.toString()+user_name+"_M.pdf";
	    		url_f = path_pdf.toString()+user_name+".pdf";
	    	} else if(option.equals("OK")) {
	    		url_m = path_pdf.toString()+folio+"_M.pdf";
	    		url_f = path_pdf.toString()+folio+".pdf";
	    	}
	    	
	    	PDFMergerUtility PDFmerger = new PDFMergerUtility();
	    	PDFmerger.setDestinationFileName(url_m);
	    	PDDocument doc = null;
	    	
	    	int PDF=0;
	    	
	        for(int x=0; x < PDFS.size(); x++){
	        	logger.log(Level.INFO, "PDFS: {0}", new Object[]{PDFS.get(x)});
	            
	            File af = new File(PDFS.get(x));
	            
	            if(af.exists()) { 
	                doc = PDDocument.load(af);
	                PDFmerger.addSource(af);
	                doc.close();
	            }else{
	            	logger.log(Level.WARNING, "El Archivo: {0} No Se Encuentra Disponible", new Object[]{PDFS.get(x)});
	                PDF=1;
	                break;
	            }
	        }
	        
	        PDFmerger.mergeDocuments(null);
	        
            if(PDF==0){
                File file = new File(url_f);
                file.getParentFile().mkdirs();
                manipulatePdf(url_m, url_f);
                
                for(int x=0; x < PDFS.size(); x++){
                    if(!PDFS.get(x).contains(uploads)){                

                        File pdf=new File(PDFS.get(x));

                        try{
                            pdf.delete();
                        }catch (Exception e){
                            e.printStackTrace();            
                        }
                    }
                }
                
                File pdfp=new File(url_m);
                
                try{
                    pdfp.delete();
                }catch (Exception e){
                    e.printStackTrace();            
                }
            }else{
                File pdf=new File(url_f);

                try{
                    pdf.delete();
                }catch (Exception e){
                    e.printStackTrace();            
                }
            }
            
            return url_f;
        }catch (Exception e) {
            e.printStackTrace();
            
            return url_f;
        }
    }

    public String downloadPDF(String option, String user_name, String object, String folio, String pdfs) {
    	T_SYST_FOR t_syst_for =DBSI.getData("T_SYST_FOR", "WHERE FORMA='"+object+"'", T_SYST_FOR.class);
    	T_SYST_FTB t_syst_ftb=DBSI.getData("T_SYST_FTB", "WHERE FORMA='"+object+"' AND ORDEN=1", T_SYST_FTB.class);
    	
    	String modul = t_syst_for.getMODUL(),
      		   categ = t_syst_for.getCATEG(),
      		   forma = t_syst_for.getFORMA(),
      		   ppdfs = t_syst_for.getPPDFS(),
      		   fname = t_syst_for.getFNAME();
    	
    	String key = DBSI.valColString(fname, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	ppdfs = ppdfs.replace("|MODUL|", modul);
    	ppdfs = ppdfs.replace("|CATEG|", categ);
    	ppdfs = ppdfs.replace("|FORMA|", forma);
    	ppdfs = DBSI.valColString(ppdfs, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+folio+"'");
    	
    	StringBuilder path_pdf = new StringBuilder();
    	
    	path_pdf.append(pdfs).append("/");
    	path_pdf.append(ppdfs);
    	
    	if(option.equals("VP")) {
    		path_pdf.append(user_name).append(".pdf");
    	} else if(option.equals("OK")) {
    		path_pdf.append(key).append(".pdf");
    	}
    	
    	return path_pdf.toString();
    }
    
    public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        PdfContentByte pagecontent;
        for (int i = 0; i < n; ) {
            pagecontent = stamper.getOverContent(++i);
            ColumnText.showTextAligned(pagecontent, Element.ALIGN_RIGHT,
                    new Phrase(String.format("Página %s de %s", i, n)), 180, 10, 0);
        }
        stamper.close();
        reader.close();
    }
    
    public String right(String value, int length) {
    	return value.substring(value.length() - length);
    }
    
    public String uploadForm(String user_name, String object, String fileName, String pdfs) {
    	T_SYST_FOR t_syst_for=DBSI.getData("T_SYST_FOR", "WHERE FORMA='"+object+"'", T_SYST_FOR.class);
    	T_SYST_FTB t_syst_ftb=DBSI.getData("T_SYST_FTB", "WHERE FORMA='"+object+"' AND ORDEN=1", T_SYST_FTB.class);
    	
    	String modul = t_syst_for.getMODUL(),
      		   categ = t_syst_for.getCATEG(),
      		   forma = t_syst_for.getFORMA(),
      		   ppdfs = t_syst_for.getPPDFS(),
      		   fname = t_syst_for.getFNAME();
    	
    	ppdfs = ppdfs.replace("|MODUL|", modul);
    	ppdfs = ppdfs.replace("|CATEG|", categ);
    	ppdfs = ppdfs.replace("|FORMA|", forma);
    	ppdfs = DBSI.valColString(ppdfs, t_syst_ftb.getTABLA(), "WHERE "+fname+"='"+fileName+"'");
    	
    	StringBuilder path = new StringBuilder();
    	
    	path.append(pdfs).append("/");
    	path.append(ppdfs);
    	
        File Dir=new File(path.toString());
        
        Dir.mkdirs();
        
    	return path.toString();
    }
    
    public void changeStatus(String user_name, String object, String fileName) {
    	T_SYST_FOR t_syst_for=DBSI.getData("T_SYST_FOR", "WHERE FORMA='"+object+"'", T_SYST_FOR.class);
    	T_SYST_FTB t_syst_ftb=DBSI.getData("T_SYST_FTB", "WHERE FORMA='"+object+"' AND ORDEN=1", T_SYST_FTB.class);
    	
    	String fname = t_syst_for.getFNAME(),
    		   table = t_syst_ftb.getTABLA();
    	
    	DBSI.getUpdateTable(table,"STTUS=3,FCACT=current_timestamp","WHERE "+fname+"='"+fileName+"'");
    }
    
    public String uploadPDF(String pdf, String type, String templates) {
    	T_SYST_PDF t_syst_pdf=DBSI.getData("T_SYST_PDF", "WHERE PPDFF='"+pdf+"' ORDER BY ORDEN", T_SYST_PDF.class);
    	
		String ptemp = templates+"/"+type+"/"+t_syst_pdf.getPTEMP(); 
		
		ptemp = ptemp.replace("|MODUL|", t_syst_pdf.getMODUL());
		ptemp = ptemp.replace("|CATEG|", t_syst_pdf.getCATEG());
		ptemp = ptemp.replace("|FORMA|", t_syst_pdf.getFORMA());
		
        File Dir=new File(ptemp);
        
        Dir.mkdirs();
		
    	return ptemp;
    }
    
	public String getStructure(String report){
		String result = null;
		
		T_SYST_REP t_syst_rep=DBSI.getData("T_SYST_REP", "WHERE REPOR='"+report+"'", T_SYST_REP.class);
		
		if(t_syst_rep != null) {
			boolean params = t_syst_rep.getPROCE().indexOf("|PARAMS|") >= 0;
			
			T_SYST_RCT t_syst_rct=DBSI.getData("T_SYST_RCT", "WHERE RPCTG='"+t_syst_rep.getRPCTG()+"'", T_SYST_RCT.class);
			T_SYST_MOD t_syst_mod=DBSI.getData("T_SYST_MOD", "WHERE MODUL='"+t_syst_rep.getMODUL()+"'", T_SYST_MOD.class);
			
			
			List <T_SYST_RHD> t_syst_rhd=DBSI.getDataList("T_SYST_RHD", "WHERE MODUL='"+t_syst_rep.getMODUL()+"' AND RPCTG='"+t_syst_rep.getRPCTG()+"' AND REPOR='"+t_syst_rep.getREPOR()+"' ORDER BY IDHEA", T_SYST_RHD.class);
			
			JsonObject jsonObject = new JsonObject();
			
			JsonArray jsonArray = new JsonArray();
			
			for(T_SYST_RHD row:t_syst_rhd ) {
				if(row.isOPTIO()) {
					JsonObject jsonHeader = new JsonObject();
					
					jsonHeader.addProperty("headerName",row.getNAMEE());
					jsonHeader.add("children",getColumnsReport(row.getMODUL(),row.getRPCTG(),row.getREPOR(),row.getIDHEA()));
					jsonArray.add(jsonHeader);
					
				} else {
					JsonArray array = new JsonArray();
					
					array=getColumnsReport(row.getMODUL(),row.getRPCTG(),row.getREPOR(),row.getIDHEA());
					
					for(JsonElement jsonElement: array) {
						jsonArray.add(jsonElement);
					}
				}
			}
			
			jsonObject.addProperty("modul",t_syst_mod.getDESCR());
			jsonObject.addProperty("rpctg",t_syst_rct.getDESCR());
			jsonObject.addProperty("repor",t_syst_rep.getDESCR());
			jsonObject.addProperty("param",params);
			
			if(params) {
				jsonObject.add("params",getParams(t_syst_rep.getMODUL(),t_syst_rep.getRPCTG(),t_syst_rep.getREPOR()));
			}
			
			jsonObject.add("expor",getColumnsExport(t_syst_rep.getMODUL(),t_syst_rep.getRPCTG(),t_syst_rep.getREPOR()));
			jsonObject.add("columns",jsonArray);
			
			result = jsonObject.toString();
		}
		
		return result;
	}
	
	public JsonArray getColumnsReport(String modul, String rpctg, String repor, int idhead) {
		JsonArray jsonArray = new JsonArray();
		
		List <T_SYST_RCL> t_syst_rcl=DBSI.getDataList("T_SYST_RCL", "WHERE MODUL='"+modul+"' AND RPCTG='"+rpctg+"' AND REPOR='"+repor+"' AND IDHEA="+idhead+" ORDER BY IDCOL", T_SYST_RCL.class);
		
		for(T_SYST_RCL row:t_syst_rcl) {
			JsonObject json = new JsonObject();
			
			json.addProperty("headerName",row.getDESCR());
			json.addProperty("field",row.getCOLMN());
			json.addProperty("width",row.getWIDTH());
			json.addProperty("filter",row.isENFLT());
			json.addProperty("resizable",true);
			json.addProperty("suppressSizeToFit",true);
			
			if(row.isENORD()) {
				json.addProperty("sortable",row.isENORD());
			}
			
			if(row.isENGRP()) {
				json.addProperty("enableRowGroup",row.isENGRP());
			}
			
			if(row.getORGRP() != null) {
				json.addProperty("rowGroupIndex",row.getORGRP());
			}
			
			if(row.getTYYPE()==2 && row.getFUNCI() != null && row.getFUNCI().length() > 0) {
				json.addProperty("aggFunc",row.getFUNCI());
			}
			
			if(row.isENFLT()) {
				switch(row.getTYYPE()) {
					case 1:
						json.addProperty("filter","agTextColumnFilter");
						break;
					case 2:
						json.addProperty("filter","agNumberColumnFilter");
						
						if(row.getTEMPL() != null && row.getTEMPL().length() > 0) {
							json.addProperty("cellRendererFramework","currencyGrid");
						}
						break;
					case 3:
						json.addProperty("filter","agDateColumnFilter");
						json.addProperty("filterParams","filterDate");
						break;
					default:
						json.addProperty("filter","agTextColumnFilter");
						break;
				}
			}
			
			if(row.getTEMPL() != null && row.getTEMPL().length() > 0) {					
				if(row.getTYYPE() == 4) {
					json.addProperty("cellRendererFramework","buttonGrid");
					json.addProperty("cellRendererParams",row.getTEMPL());
				} else {
					json.addProperty("valueFormatter",row.getTEMPL());
				}
			}
			
			
			if(row.getCOLST() != null && row.getCOLST().length() > 0){
				json.addProperty("cellStyle",row.getCOLST());
			}
			
			jsonArray.add(json);
		}
		
		return jsonArray;
	}
	
	public JsonArray getColumnsExport(String modul, String rpctg, String repor) {
		JsonArray jsonArray = new JsonArray();
		
		List <T_SYST_RCL> t_syst_rcl=DBSI.getDataList("T_SYST_RCL", "WHERE MODUL='"+modul+"' AND RPCTG='"+rpctg+"' AND REPOR='"+repor+"' AND EXPOR=1 ORDER BY IDHEA,IDCOL", T_SYST_RCL.class);
		
		for(T_SYST_RCL row:t_syst_rcl) {
			jsonArray.add(row.getCOLMN());
		}
		
		return jsonArray;
	}
	
	public JsonArray getParams(String modul, String rpctg, String repor) {
		JsonArray jsonArray = new JsonArray();
		
		List <T_SYST_RPR> t_syst_rpr=DBSI.getDataList("T_SYST_RPR", "WHERE MODUL='"+modul+"' AND RPCTG='"+rpctg+"' AND REPOR='"+repor+"' ORDER BY PARAM", T_SYST_RPR.class);
		
		for(T_SYST_RPR row:t_syst_rpr) {
			JsonObject json = new JsonObject();
			
			json.addProperty("column",row.getCOLUM());
			json.addProperty("description",row.getDESCR());
			json.addProperty("value","");
			json.addProperty("option",row.getOPTIO());
			
			if(row.getOPTIO()==2) {
				JsonArray array = new JsonArray();
				
				for(String object:row.getLISTA().split("\\|")) {
					array.add(object);
				}
				
				json.add("list",array);
			}
			
			jsonArray.add(json);
		}
		
		return jsonArray;
	}
	
	public Map<String, Object> getReport(String user, String report, String filter, String params){
		T_SYST_REP t_syst_rep=DBSI.getData("T_SYST_REP", "WHERE REPOR='"+report+"'", T_SYST_REP.class);
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		List<?> content;
		
		if(filter.isEmpty()) {
			content=DBSI.getReport(user, t_syst_rep.getPROCE(), t_syst_rep.getOBJET(), "", params);
		}else {
			content=DBSI.getReport(user, t_syst_rep.getPROCE(), t_syst_rep.getOBJET(), "WHERE "+filter, params);
		}
		
		response.put("data", content);
		
		return response;
	}
	
	public String saveVersion(String object, String user_name, String version, String json) {
		T_SYST_REP t_syst_rep =DBSI.getData("T_SYST_REP", "WHERE REPOR='"+object+"'", T_SYST_REP.class);
		
		if(version != null && version.length() <= 0) {
			version=unique().substring(0,30);
		}
		
		JSONObject jObj = new JSONObject(json);
		
		DBSI.saveOrUpdateVersions(t_syst_rep, user_name, version, jObj);

		return version;
	}
	
	public Map<String, Object> getVersions(String user, String report){
		T_SYST_REP t_syst_rep=DBSI.getData("T_SYST_REP", "WHERE REPOR='"+report+"'", T_SYST_REP.class);
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		List<?> content = DBSI.getAllRowsReport("T_SYST_RVR","WHERE MODUL='"+t_syst_rep.getMODUL()+"' AND RPCTG='"+t_syst_rep.getRPCTG()+"' AND REPOR='"+t_syst_rep.getREPOR()+"' AND USUAR='"+user+"' ORDER BY DESCR");
				
		response.put("data", content.isEmpty() ? null:content);
		
		return response;
	}
	
	public Map<String, Object> deleteVersion(String user, String report, String version){
		T_SYST_REP t_syst_rep=DBSI.getData("T_SYST_REP", "WHERE REPOR='"+report+"'", T_SYST_REP.class);
		
		DBSI.deleteVersion(t_syst_rep, user, version);
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		response.put("message", "La versión fue eliminada");
		
		return response;
	}
	
	public String prepareCatalogs(String option, String object, String user_name, String folio, String params, String json, String uploads) {
		T_SYST_CTG t_syst_ctg =DBSI.getData("T_SYST_CTG", "WHERE CATAL='"+object+"'", T_SYST_CTG.class);
		
		if(folio.length() <= 0 && t_syst_ctg.getGNKEY().length() > 0) {
			String key = t_syst_ctg.getGNKEY().replace("|PARAMS|",params);
					
			folio = DBSI.valColString(key,"","");
		}
		
		JSONObject jObj = new JSONObject(json);
		JSONArray jVal = new JSONArray();
		
		JSONObject jDat = jObj.getJSONObject(t_syst_ctg.getCATAL());
		jVal.put(jDat);
		
		DBSI.saveOrUpdateCatalogs(object, option, t_syst_ctg.getOBJEC(), t_syst_ctg.getCOLMN(), user_name, folio, jVal );

		
		//changeDirUpload(user_name, t_syst_for, t_syst_ftb.get(0), old_fol, folio, uploads);
		
		return folio;
	}
	
	public Map<String, Object> deleteCatalogs(String object, String user_name, String folio){
		String[] keys = folio.split("\\|");
		
		T_SYST_CTG t_syst_ctg =DBSI.getData("T_SYST_CTG", "WHERE CATAL='"+object+"'", T_SYST_CTG.class);
		
		List <T_SYST_CCL> t_syst_ccl=DBSI.getDataList("T_SYST_CCL", "WHERE MODUL='"+t_syst_ctg.getMODUL()+"' AND CATAL='"+object+"' AND LLAVE=1 ORDER BY IDCOL", T_SYST_CCL.class);
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		if(keys.length == t_syst_ccl.size()) {
			String condition = "";
			int sec = 0;
			
			for(T_SYST_CCL row:t_syst_ccl) {
				if(keys.length-1 == sec) {
					condition += row.getCOLMN()+"='"+keys[sec]+"'";
				}else {
					condition += row.getCOLMN()+"='"+keys[sec]+"' AND ";
				}
				
				sec += 1;
			}
			
			DBSI.deleteCatalogs(t_syst_ctg.getOBJEC(), condition);
			response.put("message", "El registro fue eliminado");
		}else {
			response.put("error", "El registro no puede ser eliminado");
		}
		
		return response;
	}
}
