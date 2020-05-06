package com.house.springboot.workbook.backend.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import com.house.springboot.workbook.backend.methods.AccessDataBase;

@Component
public class InfoAdditionalToken<K, V> implements TokenEnhancer{
	private final Logger logger = Logger.getLogger(InfoAdditionalToken.class.getName());

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		
		AccessDataBase ADB = new AccessDataBase();
		
		List<?> User = ADB.getOneRow("V_SYST_USR","where CUSER='"+authentication.getName()+"'");
		
		if(User.toString() == "[]") {
			logger.log(Level.SEVERE, "El Usuario {0} no existe en el sistema", new Object[]{authentication.getName()});
			throw new UsernameNotFoundException("El Usuario "+authentication.getName()+" no existe en el sistema");
		}
		
		@SuppressWarnings("unchecked")
		Map<K,V> userInfo = (Map<K, V>) User.get(0);
		
		
		Map<String, Object> info = new HashMap<>();
		info.put("n_emp", (String) userInfo.get("NUEMP"));
		info.put("name", (String) userInfo.get("NAMEE"));
		info.put("surname_first", (String) userInfo.get("PRIAP"));
		info.put("surname_second", (String) userInfo.get("SEGAP"));
		info.put("title", (String) userInfo.get("TITUL"));
		info.put("job", (String) userInfo.get("PUEST"));
		info.put("adscription", (String) userInfo.get("ADSCR"));
		info.put("level", (String) userInfo.get("NIVEL"));
		info.put("rfc", (String) userInfo.get("RRFCC"));
		info.put("curp", (String) userInfo.get("CCURP"));
		info.put("nss", (String) userInfo.get("NMNSS"));
		info.put("office", (String) userInfo.get("REGIO"));
		info.put("email", (String) userInfo.get("EMAIL"));
		info.put("extension", (String) userInfo.get("EXTEN"));
		info.put("profile", (String) userInfo.get("PERFL"));
		
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
		
		return accessToken;
	}

}
