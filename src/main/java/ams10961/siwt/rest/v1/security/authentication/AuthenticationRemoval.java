package ams10961.siwt.rest.v1.security.authentication;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import ams10961.siwt.Constants;
import ams10961.siwt.entities.Authentication;
import ams10961.siwt.entities.Session;
import ams10961.siwt.entities.Session.SessionStatus;
import ams10961.siwt.entities.persistence.AuthenticationPersistence;
import ams10961.siwt.entities.persistence.PersistenceException;
import ams10961.siwt.entities.persistence.SessionPersistence;

/*
 * Remove non active authentication objects from volatile, in-memory database
 */

@Stateless
public class AuthenticationRemoval {

	@Inject
	private transient Logger logger;

	@EJB
	private AuthenticationPersistence authenticationPersistence;

	@EJB
	private SessionPersistence sessionPersistence;

	@Resource
	TimerService timerService;

	@Schedule(hour = "*", minute = "*/1", persistent = false)
	public void automaticTimeout() {
		
		Date now = new Date(System.currentTimeMillis());

		// REMOVE EXPIRED OR ABANDONED
		// search using this reference point
		List<Authentication> inactive = authenticationPersistence.findInactive();
		for (Authentication authentication : inactive) {
			try {
				logger.info("deleting, not active: {}", authentication.toString());
				authenticationPersistence.delete(authentication);

				// mark any corresponding session as expired
				if (authentication.getSessionId() != null) {
					Session session = sessionPersistence.findById(authentication.getSessionId());
					if (session != null) {
						logger.info("marking corresponding session as expired {}", session.toString());
						session.setStatus(SessionStatus.EXPIRED);
						session.setClosureTime(now);
						sessionPersistence.save(session);
					} else {
						logger.info("no session found with ID {}", authentication.getSessionId());
					}
				}
			} catch (PersistenceException e) {
				logger.error("Couldn't delete authentication (too old) {}", authentication.toString(), e);
			}
		}

		// REMOVE TOO OLD
		// calculate reference point
		Date oldestCreationDate = new Date();
		oldestCreationDate.setTime(oldestCreationDate.getTime() - Constants.AUTHENTICATION_MAX_AGE_MS);

		// search using this reference point
		List<Authentication> old = authenticationPersistence.findOlderThan(oldestCreationDate);
		for (Authentication authentication : old) {
			try {
				logger.info("deleting, too old: {}", authentication.toString());
				authenticationPersistence.delete(authentication);

				// mark corresponding session as expired
				if (authentication.getSessionId() != null) {

					Session session = sessionPersistence.findById(authentication.getSessionId());
					if (session != null) {
						logger.info("marking corresponding session as expired {}", session.toString());
						session.setStatus(SessionStatus.EXPIRED);
						session.setClosureTime(now);						
						sessionPersistence.save(session);
					} else {
						logger.info("no session found with ID {}", authentication.getSessionId());
					}
				}
			} catch (PersistenceException e) {
				logger.error("Couldn't delete authentication (too old) {}", authentication.toString(), e);
			}
		}

		// REMOVE INVALIDATED TOO LONG AGO
		// calculate reference point
		Date maxLastValidated = new Date();
		maxLastValidated.setTime(maxLastValidated.getTime() - Constants.INACTIVITY_EXPIRY_MS);

		// search using this reference point
		List<Authentication> notValidatedRecently = authenticationPersistence.findNotValidatedSince(maxLastValidated);
		for (Authentication authentication : notValidatedRecently) {
			try {
				logger.info("deleting, inactive: {}", authentication.toString());
				authenticationPersistence.delete(authentication);

				// mark corresponding session as expired
				if (authentication.getSessionId() != null) {

					Session session = sessionPersistence.findById(authentication.getSessionId());
					if (session != null) {
						logger.info("marking corresponding session as expired {}", session.toString());
						session.setStatus(SessionStatus.EXPIRED);
						session.setClosureTime(now);						
						sessionPersistence.save(session);
					} else {
						logger.info("no session found with ID {}", authentication.getSessionId());
					}
				}
			} catch (PersistenceException e) {
				logger.error("Couldn't delete authentication (inactive too long) {}", authentication.toString(), e);
			}
		}
	}
}
