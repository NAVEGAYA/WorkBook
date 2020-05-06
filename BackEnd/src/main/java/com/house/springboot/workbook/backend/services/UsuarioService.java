package com.house.springboot.workbook.backend.services;

import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.house.springboot.workbook.backend.methods.AccessDataBase;

@Service
public class UsuarioService<K, V> implements UserDetailsService{
	private final Logger logger = Logger.getLogger(UsuarioService.class.getName());
	
	
	@Override
	@Transactional(readOnly=true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		AccessDataBase ADB = new AccessDataBase();
		
		List<?> User = ADB.getOneRow("V_SYST_USR","where CUSER='"+username+"'");
		
		if(User.toString() == "[]") {
			logger.log(Level.SEVERE, "El Usuario {0} no existe en el sistema", new Object[]{username});
			throw new UsernameNotFoundException("El Usuario "+username+" no existe en el sistema");
		}
		
		@SuppressWarnings("unchecked")
		Map<K,V> userInfo = (Map<K, V>) User.get(0);
		
		String password = (String) userInfo.get("PASSW");
		
		Boolean enabled = (Boolean) userInfo.get("ENABL");
		
		String access = ADB.getAccess(username);
				
		List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(access);
		
		return new User(username, password, enabled, true, true, true, authorities);
	}
	
}
