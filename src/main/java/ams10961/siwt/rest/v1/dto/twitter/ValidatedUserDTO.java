package ams10961.siwt.rest.v1.dto.twitter;


/*
 * Non-persisted DTO used results
 */
public class ValidatedUserDTO {

	private String description;
	private String screenName;

	public ValidatedUserDTO() {
		// do nothing
	}
	
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[TwitterUserDTO");
		out.append("|").append(this.getScreenName());
		out.append("|").append(this.getDescription());
		out.append("]");
		return out.toString();
	}

}
