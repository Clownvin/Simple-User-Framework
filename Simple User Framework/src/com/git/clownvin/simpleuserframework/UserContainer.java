package com.git.clownvin.simpleuserframework;

public interface UserContainer<UserT extends User> {
	
	public UserT getProfile();
	
	public void setProfile(UserT user);
}
