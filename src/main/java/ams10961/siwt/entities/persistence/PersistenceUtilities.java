package ams10961.siwt.entities.persistence;

import java.util.UUID;

public class PersistenceUtilities {

	public static String generateUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}
