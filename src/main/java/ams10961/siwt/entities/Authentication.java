package ams10961.siwt.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "AUTHENTICATION")
@NamedQueries({
	@NamedQuery(name = "Authentication.countAll", query = "SELECT count (a) FROM Authentication a"),
	@NamedQuery(name = "Authentication.findAll", query = "SELECT a FROM Authentication a"),
	@NamedQuery(name = "Authentication.findBySessionId", query = "SELECT a FROM Authentication a WHERE a.sessionId = :sessionId"),
	@NamedQuery(name = "Authentication.findInactive", query = "SELECT a FROM Authentication a WHERE a.status <> :status"),
	@NamedQuery(name = "Authentication.findNotValidatedSince", query = "SELECT a FROM Authentication a WHERE a.lastValidatedTime < :time"),
	@NamedQuery(name = "Authentication.findOlderThan", query = "SELECT a FROM Authentication a WHERE a.creationTime < :time"),
	@NamedQuery(name = "Authentication.findByUuid", query = "SELECT a FROM Authentication a WHERE a.uuid = :uuid") })

/*
 * This object is stored in a volatile in-memory database for rapid access / request rejection
 */
public class Authentication {
	
	public static final String BEARER_PREFIX = "Bearer ";

	public String getSessionUuid() {
		return sessionUuid;
	}

	public void setSessionUuid(String sessionUuid) {
		this.sessionUuid = sessionUuid;
	}

	/* references to the named queries above */
	public static String COUNT_ALL = "Authentication.countAll";
	public static String FIND_ALL = "Authentication.findAll";
	public static String FIND_BY_UUID = "Authentication.findByUuid";
	public static String FIND_BY_SESSION_ID = "Authentication.findBySessionId";
	public static String FIND_INACTIVE = "Authentication.findInactive";
	public static String FIND_NOT_VALIDATED_SINCE = "Authentication.findNotValidatedSince";
	public static String FIND_OLDER_THAN = "Authentication.findOlderThan";
	public static String UUID = "uuid";
	public static String SESSION_ID = "sessionId";
	public static String TIME = "time";
	public static String STATUS = "status";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	// up to 256 chars
	@Column(name = "UUID", nullable = false, columnDefinition = "VARCHAR(256)")
	private String uuid;

	// inter-database reference
	@Column(name = "SESSION_ID", unique = true)
	private Long sessionId;
	
	// inter-database reference
	@Column(name = "SESSION_UUID", unique = true, columnDefinition = "VARCHAR(32)")
	private String sessionUuid;	

	// inter-database reference
	@Column(name = "USER_ID")
	private Long userId;

	public enum AuthenticationStatus {
		ACTIVE, EXPIRED, SUSPICIOUS, ABANDONED
	}

	@Column(name = "STATUS")
	private AuthenticationStatus status;

	@Column(name = "IP_ADDRESS", columnDefinition = "VARCHAR(16)", nullable=false)
	private String ipAddress;
	
	@Column(name = "CREATION_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date creationTime;

	@Column(name = "LAST_VALIDATED_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date lastValidatedTime;

	@Column(name = "INACTIVITY_TIMEOUT")
	private long inactivityTimeout;
	

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[Authentication");
		out.append("|").append(id);
		out.append("|").append(uuid);
		out.append("|").append(sessionId);
		out.append("|").append(sessionUuid);		
		out.append("|").append(ipAddress);
		out.append("|").append(inactivityTimeout);
		out.append("]");
		return out.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public AuthenticationStatus getStatus() {
		return status;
	}
	
	public String getStatusString() {
		switch (this.status) {
		case ACTIVE:
			return "active";
		case EXPIRED:
			return "expired";
		case SUSPICIOUS:
			return "suspicious";
		case ABANDONED:
			return "abandoned";			
		default:
			return "";
		}
	}

	public void setStatus(AuthenticationStatus active) {
		this.status = active;
	}

	public long getInactivityTimeout() {
		return inactivityTimeout;
	}

	public void setInactivityTimeout(long timeout) {
		this.inactivityTimeout = timeout;
	}
	

	public java.util.Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(java.util.Date creationTime) {
		this.creationTime = creationTime;
	}

	public java.util.Date getLastValidatedTime() {
		return lastValidatedTime;
	}

	public void setLastValidatedTime(java.util.Date lastValidatedTime) {
		this.lastValidatedTime = lastValidatedTime;
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
