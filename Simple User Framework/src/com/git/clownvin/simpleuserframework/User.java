package com.git.clownvin.simpleuserframework;

import java.io.Serializable;
import java.util.Hashtable;

import com.git.clownvin.security.Passwords;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5187544274917947184L;
	private String username;
	private byte[] hash;
	private byte[] salt;
	private Hashtable<String, Object> values = new Hashtable<>();
	
	public User() {
		//Only for deserialization, don't actually use lol
	}
	
	public User(String username, byte[] salt, byte[] hash) {
		this.username = username;
		this.hash = hash;
		this.salt = salt;
	}
	
	public String getUsername() {
		return username;
	}
	
	public boolean verify(char[] password) {
		return Passwords.matches(password, salt, hash);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setCredentials(byte[] hash, byte[] salt) {
		this.hash = hash;
		this.salt = salt;
	}
	
	@Override
	public String toString() {
		return "User("+username+")";
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) values.get(key);
	}
	
	public void put(String key, Object value) {
		if (!(value instanceof Serializable))
			throw new RuntimeException(value+" is not serializable.");
		values.put(key, value);
	}
}
