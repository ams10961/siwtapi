package ams10961.siwt.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "USER")
@NamedQueries({
		@NamedQuery(name = "User.findAll", query = "select u from User u"),
		@NamedQuery(name = "User.findByUuid", query = "select u from User u where u.uuid = :uuid"),
		@NamedQuery(name = "User.findByHandle", query = "select u from User u where u.handle = :handle") })

/*
 * this object is persisted in a non-volatile database
 */
public class User {

	public static String FIND_ALL = "User.findAll";
	public static String FIND_BY_HANDLE = "User.findByHandle";
	public static String HANDLE = "handle";
	public static String FIND_BY_UUID = "User.findByUuid";
	public static String UUID = "uuid";
	
	public static final int ROLE_USER = 1;
	public static final int ROLE_ADMIN = 2;

	public static char TYPE_GUEST = 'G';
	public static char TYPE_TWITTER = 'T';

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	@Column(name = "UUID", unique = true, nullable = false, columnDefinition = "VARCHAR(32)")
	private String uuid;

	// TODO: reintroduce nullable=false
	@Column(name = "TYPE", columnDefinition = "CHAR")
	private char type;

	@Column(name = "ROLES")
	private int roles;

	@Column(name = "HANDLE", unique = true)
	private String handle;

	@Column(name = "CREATION_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date creationTime;

	@OneToMany(mappedBy = "user")
	private List<Session> sessions;

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[User");
		out.append("|").append(id);
		out.append("|").append(uuid);
		out.append("|").append(type);
		out.append("|").append(handle);
		out.append("]");
		return out.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public java.util.Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(java.util.Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getRoles() {
		return roles;
	}

	public void setRoles(int roles) {
		this.roles = roles;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}
	
	public boolean isCreator (User user) {
		return ( user.getId() == getId());
	}
	
	public boolean isAdmin () {
		return ((getRoles() & User.ROLE_ADMIN) > 0);
	}

}
