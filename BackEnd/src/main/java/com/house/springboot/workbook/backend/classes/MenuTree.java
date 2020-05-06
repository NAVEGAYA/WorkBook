package com.house.springboot.workbook.backend.classes;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.house.springboot.workbook.backend.models.MENU;
import com.house.springboot.workbook.backend.services.DataBaseService;

public class MenuTree {
	
	public MenuTree(MENU menu) {
		this.response = new JsonObject();
		this.parentMenu = menu;
	}
	
	private final JsonObject response;
	private final MENU parentMenu;
	
	ApplicationContext context = new ClassPathXmlApplicationContext("SpringJdbc.xml");
	DataBaseService DBSI = (DataBaseService) context.getBean("DBImpl");
	
	public void build(String User) {
		
		List <MENU> menu_list=DBSI.getDataList("dbo.fn_MENU('"+User+"')", "WHERE PAREN='"+parentMenu.getID()+"' AND ACCES=1 ORDER BY SEC", MENU.class);
		
		if(menu_list != null && !menu_list.isEmpty()) {
			for(MENU menu:menu_list) {
				createMenuStructure(menu,response,User);
			}
		}
	}
	
	private void createMenuStructure(MENU menu, JsonObject parentObject, String User) {
		
		JsonObject menuInfoObject = new JsonObject();
		JsonArray childArray = new JsonArray();
		
		menuInfoObject.addProperty("id", menu.getID().toLowerCase());
		menuInfoObject.addProperty("title", menu.getTITLE());
		menuInfoObject.addProperty("translate", menu.getLABEL());
		menuInfoObject.addProperty("type", menu.getTYPPE());
		menuInfoObject.addProperty("icon", menu.getTICON());
		
		if(menu.getTYPPE().equals("item")) {
			menuInfoObject.addProperty("url", menu.getUURLL());
		}
		
		List <MENU> menu_list=DBSI.getDataList("dbo.fn_MENU('"+User+"')", "WHERE GRUPO='"+menu.getGRUPO()+"' AND PAREN='"+menu.getID()+"' AND ACCES=1 ORDER BY SEC", MENU.class);
		
		if(menu_list != null && !menu_list.isEmpty()) {
			for(MENU menuL:menu_list) {
				createMenuStructure(menuL,menuInfoObject,User);
			}
		}
		
		childArray.add(menuInfoObject);
		
		if(parentObject.has("children")) {
			JsonArray previousArray = parentObject.getAsJsonArray("children");
			
			for(JsonElement jsonElement: childArray) {
				previousArray.add(jsonElement);
			}
			
			parentObject.add("children", previousArray);
		} else {
			parentObject.add("children", childArray);
		}
	}
	
	public JsonObject getStructure() {
		return this.response;
	}
}
