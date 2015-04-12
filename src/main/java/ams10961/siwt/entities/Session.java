package ams10961.siwt.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "SESSION")
@NamedQueries({
		@NamedQuery(name = "Session.findAll", query = "select s from Session s"),
		@NamedQuery(name = "Session.findByExternalHandle", query = "select s from Session s where s.externalHandle = :externalHandle"),
		@NamedQuery(name = "Session.findByUuid", query = "select s from Session s where s.uuid = :uuid") })

/*
 * this object is persisted in a non-volatile database
 */
public class Session {

	public static String FIND_ALL = "Session.findAll";
	public static String FIND_BY_UUID = "Session.findByUuid";
	public static String FIND_BY_EXTERNAL_HANDLE = "Session.findByExternalHandle";
	public static String FIND_BY_AUTHENTICATION_ID = "Session.findByAuthenticationId";
	public static String UUID = "uuid";
	public static String EXTERNAL_HANDLE = "externalHandle";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	@Column(name = "UUID", columnDefinition = "VARCHAR(32)")
	private String uuid;

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User user;
	
	@Column(name = "IP_ADDRESS", columnDefinition = "VARCHAR(16)", nullable = false)
	private String ipAddress;

	@Column(name = "EXTERNAL_HANDLE", columnDefinition = "VARCHAR(256)")
	private String externalHandle;

	@Column(name = "EXTERNAL_ID", columnDefinition = "VARCHAR(32)")
	private String externalId;

	public enum SessionStatus {
		ACTIVE, EXPIRED, CLOSED, SUSPICIOUS, SUPERSEDED, ABANDONED
	}

	@Column(name = "STATUS")
	private SessionStatus status;

	@Column(name = "CREATION_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date creationTime;

	@Column(name = "LAST_VALIDATED_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date lastValidatedTime;

	@Column(name = "CLOSURE_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date closureTime;

	@Column(name = "TIMEOUT")
	private long inactivityTimeout;

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[Session");
		out.append("|").append(id);
		out.append("|").append(uuid);
		out.append("|").append(ipAddress);
		out.append("|").append(user);
		out.append("|").append(creationTime);
		out.append("|").append(closureTime);
		out.append("|").append(inactivityTimeout);
		out.append("|").append(getStatusString(status));
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

	public void setCreationTime(java.util.Date creationTime) {
		this.creationTime = creationTime;
	}

	public java.util.Date getCreationTime() {
		return creationTime;
	}
	
	public java.util.Date getLastValidatedTime() {
		return lastValidatedTime;
	}

	public void setLastValidatedTime(java.util.Date lastValidatedTime) {
		this.lastValidatedTime = lastValidatedTime;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getExternalHandle() {
		return externalHandle;
	}

	public void setExternalHandle(String externalHandle) {
		this.externalHandle = externalHandle;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	public SessionStatus getStatus() {
		return status;
	}

	public String getStatusString(SessionStatus status) {
		switch (status) {
		case ACTIVE:
			return "active";
		case EXPIRED:
			return "expired";
		case SUSPICIOUS:
			return "suspicious";				
		case SUPERSEDED:
			return "superseded";			
		case ABANDONED:
			return "abandoned";
		default:
			return "undefined";
		}
	}

	public void setStatus(SessionStatus status) {
		this.status = status;
	}

	public long getInactivityTimeout() {
		return inactivityTimeout;
	}

	public void setInactivityTimeout(long timeout) {
		this.inactivityTimeout = timeout;
	}

	public boolean isCreator (User requester) {
		return ( this.user.getId() == requester.getId());
	}

	public java.util.Date getClosureTime() {
		return closureTime;
	}

	public void setClosureTime(java.util.Date closureTime) {
		this.closureTime = closureTime;
	}
	
}
