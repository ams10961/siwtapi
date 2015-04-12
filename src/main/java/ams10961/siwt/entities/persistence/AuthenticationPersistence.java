package ams10961.siwt.entities.persistence;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

import ams10961.siwt.Constants;
import ams10961.siwt.entities.Authentication;
import ams10961.siwt.entities.Authentication.AuthenticationStatus;
import ams10961.siwt.entities.Session;

@Stateless
public class AuthenticationPersistence {

	// this is distinct to all the others, and for a database held in memory
	@PersistenceContext(unitName = "volatile")
	private EntityManager entityManager;

	@Inject
	private transient Logger logger;

	public AuthenticationPersistence() {
		super();
	}

	public void save(Authentication login) {
		entityManager.persist(login);
	}
	
	/*
	 * 
	 */
	public int countAll() {
		try {
			return ((Number)entityManager.createNamedQuery(Authentication.COUNT_ALL).getSingleResult()).intValue();
		} catch (NoResultException e) {
			return 0;
		}
	}

	/*
	 * 
	 */
	public List<Authentication> findAll() {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_ALL, Authentication.class);
			List<Authentication> results = query.getResultList();
			if (logger.isDebugEnabled()) {
				for (Authentication authentication : results) {
					logger.debug("found {}", authentication.toString());
				}
			}
			return results;
		} catch (NoResultException e) {
			return null;
		}
	}	

	public Authentication findById(Long id) {
		return entityManager.find(Authentication.class, id);
	}

	/*
	 * 
	 */
	public Authentication findByUuid(String uuid) {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_BY_UUID, Authentication.class);
			query.setParameter(Authentication.UUID, uuid);
			Authentication result = query.getSingleResult();
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
	public Authentication findBySessionId(Long sessionId) {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_BY_SESSION_ID, Authentication.class);
			query.setParameter(Authentication.SESSION_ID, sessionId);
			Authentication result = query.getSingleResult();
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
	public List<Authentication> findInactive () {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_INACTIVE, Authentication.class);
			query.setParameter(Authentication.STATUS, AuthenticationStatus.ACTIVE);
			List<Authentication> results = query.getResultList();
			if (logger.isDebugEnabled()) {
				for (Authentication authentication : results) {
					logger.debug("not validated recently enough:{}",  authentication.toString());
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
	public List<Authentication> findNotValidatedSince (Date time) {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_NOT_VALIDATED_SINCE, Authentication.class);
			query.setParameter(Authentication.TIME, time);
			List<Authentication> results = query.getResultList();
			if (logger.isDebugEnabled()) {
				for (Authentication authentication : results) {
					logger.debug("not validated recently enough:{}",  authentication.toString());
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
	public List<Authentication> findOlderThan (Date time) {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_OLDER_THAN, Authentication.class);
			query.setParameter(Authentication.TIME, time);
			List<Authentication> results = query.getResultList();
			if (logger.isDebugEnabled()) {
				for (Authentication authentication : results) {
					logger.debug("too old: {}", authentication.toString());
				}
			}
			return results;
		} catch (NoResultException e) {
			return null;
		}
	}

	/*
	 * Create an authentication object with no associated session
	 */
	public Authentication create(String ipAddress) throws PersistenceException {

		Authentication authentication = new Authentication();
		
		// IP address
		authentication.setIpAddress(ipAddress);

		// TODO: generate better authorization token
		StringBuilder uuid = new StringBuilder();
		uuid.append(PersistenceUtilities.generateUuid());
		uuid.append(PersistenceUtilities.generateUuid());
		authentication.setUuid(uuid.toString());
		
		// no associated session
		authentication.setSessionId(null);
		authentication.setSessionUuid(null);
		authentication.setUserId(null);

		// initial status
		authentication.setStatus(AuthenticationStatus.ACTIVE);

		// set last validation time and timeout
		Date now = new Date(System.currentTimeMillis());
		authentication.setCreationTime(now);
		authentication.setLastValidatedTime(now);
		
		authentication.setInactivityTimeout(Constants.INACTIVITY_EXPIRY_MS);

		// persist the authentication object (in volatile datasource)
		entityManager.persist(authentication);

		return authentication;
	}
	
	/*
	 * Create an authentication object linked to a session
	 */
	public Authentication create(Session session) throws PersistenceException {

		Authentication authentication = new Authentication();
		
		// IP address
		authentication.setIpAddress(session.getIpAddress());

		// set session ID
		authentication.setSessionId(session.getId());
		authentication.setSessionUuid(session.getUuid());

		// set user ID
		authentication.setUserId(session.getUser().getId());

		// TODO: generate better authorization token
		StringBuilder uuid = new StringBuilder();
		uuid.append(PersistenceUtilities.generateUuid());
		uuid.append(PersistenceUtilities.generateUuid());
		authentication.setUuid(uuid.toString());

		// initial status
		authentication.setStatus(AuthenticationStatus.ACTIVE);

		// set last validation time and timeout
		Date now = new Date(System.currentTimeMillis());
		authentication.setCreationTime(now);
		authentication.setLastValidatedTime(now);
		authentication.setInactivityTimeout(session.getInactivityTimeout());

		// persist the authentication object (in volatile datasource)
		entityManager.persist(authentication);

		return authentication;
	}
	

	/*
	 * 
	 */
	public void delete (Authentication authentication) throws PersistenceException {
		entityManager.remove(authentication);
	}

	/*
	 * When deleting a session, remove any associated authentication objects
	 */
	public void deleteAuthenticationObjects (Session session) throws PersistenceException {
		try {
			TypedQuery<Authentication> query = entityManager.createNamedQuery(Authentication.FIND_BY_SESSION_ID, Authentication.class);
			query.setParameter(Authentication.SESSION_ID, session.getId());
			List<Authentication> authentications = query.getResultList();
			for (Authentication authentication : authentications) {
				logger.debug("found and removing:{}", authentication.toString());
				entityManager.remove(authentication);
			}
			
		} catch (NoResultException e) {
			logger.warn("no authentication sessions found to remove");
		}
	}



}
