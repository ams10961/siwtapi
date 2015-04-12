package ams10961.siwt.entities.persistence;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

import ams10961.siwt.entities.User;

@Stateless
public class UserPersistence {

	// inject standard persistent entity manager, defined in util.Resources.
	@Inject
	private EntityManager entityManager;

	@Inject
	private transient Logger logger;

	public UserPersistence() {
		super();
	}

	/*
	 * 
	 */
	public List<User> findAll() {
		TypedQuery<User> query = entityManager.createNamedQuery(User.FIND_ALL, User.class);
		List<User> results = query.getResultList();
		if (logger.isDebugEnabled()) {
			for (User result : results) {
				logger.info(result.toString());
			}
		}
		return results;
	}

	/*
	 * 
	 */
	public User findById(Long id) {
		return entityManager.find(User.class, id);
	}

	/*
	 * 
	 */
	public User findByUuid(String uuid) {
		try {
			TypedQuery<User> query = entityManager.createNamedQuery(User.FIND_BY_UUID, User.class);
			query.setParameter(User.UUID, uuid);
			User result = query.getSingleResult();
			if (logger.isDebugEnabled()) {
				logger.debug("found:{}", result.toString());
			}
			return result;
		} catch (NoResultException e) {
			return null;
		}
	}

	/*
	 * 
	 */
	public User findByHandle(String handle) {
		try {
			TypedQuery<User> query = entityManager.createNamedQuery(User.FIND_BY_HANDLE, User.class);
			query.setParameter(User.HANDLE, handle);
			User result = query.getSingleResult();
			if (logger.isDebugEnabled()) {
				logger.debug("found:{}", result.toString());
			}
			return result;
		} catch (NoResultException e) {
			return null;
		}
	}

	/*
	 * 
	 */
	public User create(User user) throws PersistenceException {

		// set timestamp
		Date time = new Date();
		time.setTime(System.currentTimeMillis());
		user.setCreationTime(time);

		// set default role
		user.setRoles(User.ROLE_USER);
		
		// create uuid
		user.setUuid(PersistenceUtilities.generateUuid());
		
		// persist the Session
		entityManager.persist(user);
		
		return user;
	}

	/*
	 * 
	 */
	public void save(User user) {
		entityManager.persist(user);
		if (logger.isDebugEnabled()) {
			logger.debug("wrote:{}", user.toString());
		}

	}

}
