package com.git.clownvin.simpleuserframework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import com.git.clownvin.security.Passwords;

public final class UserDatabase<UserT extends User, ContainerT extends UserContainer<UserT>> {
	
	private final Hashtable<String, ContainerT> activeUsers = new Hashtable<>();
	//private static File userLocation;
	private String userLocation = "./data/users/";
	private final Class<UserT> userClass;
	
	public UserDatabase(Class<UserT> userClass) {
		ensureLocationExists();
		this.userClass = userClass;
	}
	
	private void ensureLocationExists() {
		File file = new File(userLocation);
		if (!file.exists())
			file.mkdirs();
	}
	
	public final boolean isUserActive(String username) {
		return activeUsers.get(username.toLowerCase()) != null;
	}
	
	private final File getUserFile(String username) {
		//OLD probably really bad method
		/*for (File file : userLocation.listFiles()) {
			if (file.getName().equalsIgnoreCase(username))
				return file;
		}*/
		return new File(userLocation+username.toLowerCase()+".user");
	}
	
	private final File getUserFile(User user) {
		return getUserFile(user.getUsername());
	}
	
	public final ContainerT getActiveUser(User user) {
		return getActiveUser(user.getUsername());
	}
	
	public final ContainerT getActiveUser(String username) {
		return activeUsers.get(username.toLowerCase());
	}
	
	public final User getInactiveUser(String username) throws IOException {
		return loadUserFromFile(getUserFile(username.toLowerCase()));
	}
	
	public final void saveAll() {
		for (String username : activeUsers.keySet()) {
			saveUser(activeUsers.get(username).getProfile());
		}
	}
	
	public final ContainerT logIn(ContainerT user) {
		//System.out.println("Logging in "+user);
		activeUsers.put(user.getProfile().getUsername().toLowerCase(), user);
		return user;
	}
	
	public final void logOut(ContainerT user) {
		logOut(user.getProfile());
	}
	
	public final void logOut(User user) {
		System.out.println("Logging out "+user);
		saveUser(activeUsers.remove(user.getUsername().toLowerCase()).getProfile());
	}
	
	public final UserT createUser(String username, String password) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		System.out.println("Creating user with username: "+username);
		byte[] salt = Passwords.getNextSalt();
		UserT user = userClass.getConstructor(String.class, byte[].class, byte[].class).newInstance(username, salt, Passwords.hash(password.toCharArray(), salt));
		saveUser(user);
		return user;
	}
	
	public static final byte SUCCESS = 0;
	public static final byte INVALID_CREDENTIALS = 1;
	public static final byte ALREADY_LOGGED_IN = 2;
	public static final byte EXCEPTION_OCCURED = 3;
	
	public final byte verifyCredentials(UserContainer<UserT> container, String username, String password) {
		//System.out.println("Verifying user...");
		if (this.getActiveUser(username) != null)
			return ALREADY_LOGGED_IN;
		File userFile = getUserFile(username);
		UserT user;
		if (!userFile.exists()) {
			if (container != null) {
				try {
					user = createUser(username, password);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
					return EXCEPTION_OCCURED;
				}
				container.setProfile(user);
				return SUCCESS;
			}
			return SUCCESS;
		}
		try {
			user = loadUserFromFile(userFile);
		} catch (IOException e) {
			e.printStackTrace();
			return EXCEPTION_OCCURED;
		}
		if (user.verify(password.toCharArray())) {
			if (container != null)
				container.setProfile(user);
			return SUCCESS;
		}
		System.out.println("Failed verification check..");
		return INVALID_CREDENTIALS;
	}
	
	public final void saveUser(User user) {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getUserFile(user)))) {
			out.writeObject(user);
			System.out.println("Successfully saved user: "+user);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private final UserT loadUserFromFile(File userFile) throws IOException {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile))) {
			return (UserT) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
