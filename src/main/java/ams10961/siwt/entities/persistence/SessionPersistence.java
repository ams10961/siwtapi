package ams10961.siwt.entities.persistence;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

import ams10961.siwt.entities.Session;
import ams10961.siwt.entities.Session.SessionStatus;

@Stateless
public class SessionPersistence {

	@Inject
	// inject standard persistent entity manager, defined in util.Resources.
	private EntityManager entityManager;

	@Inject
	private transient Logger logger;

	public SessionPersistence() {
		super();
	}
	
	/*
	 * 
	 */
	public List<Session> findAll() {

		TypedQuery<Session> query = entityManager.createNamedQuery(Session.FIND_ALL, Session.class);
		return query.getResultList();
	}

	/*
	 * 
	 */
	public Session findById(Long id) {
		return entityManager.find(Session.class, id);
	}
	
	/*
	 * 
	 */
	public Session findByUuid(String uuid) {
		try {
			TypedQuery<Session> query = entityManager.createNamedQuery(Session.FIND_BY_UUID, Session.class);
			query.setParameter(Session.UUID, uuid);
			Session result = query.getSingleResult();
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
	public List<Session> findByExternalHandle(String externalHandle) {
		try {
			TypedQuery<Session> query = entityManager.createNamedQuery(Session.FIND_BY_EXTERNAL_HANDLE, Session.class);
			query.setParameter(Session.EXTERNAL_HANDLE, externalHandle);
			List<Session> results = query.getResultList();
			if (logger.isDebugEnabled()) {
				for (Session result : results) {
					logger.debug("found:" + result.toString());
				}
			}
			return results;
		} catch (NoResultException e) {
			return null;
		}
	}



	/*
	 * 
	 */
	public Session create(Session session) throws PersistenceException {

		// set timestamp
		Date time = new Date();
		time.setTime(System.currentTimeMillis());
		session.setCreationTime(time);
		session.setLastValidatedTime(time);

		// create uuid
		session.setUuid(PersistenceUtilities.generateUuid());

		// mark as active
		session.setStatus(SessionStatus.ACTIVE);

		// persist the Session
		entityManager.persist(session);

		return session;
	}
	
	/*
	 * 
	 */
	public Session save (Session session) throws PersistenceException {
		entityManager.persist(session);
		return session;
	}

	/*
	 * 
	 */
	public Session close(Session session) throws PersistenceException {

		// set timestamp
		Date time = new Date();
		time.setTime(System.currentTimeMillis());
		session.setClosureTime(time);

		// mark as active
		session.setStatus(SessionStatus.CLOSED);

		// persist the Session
		entityManager.persist(session);

		return session;
	}

	
}
