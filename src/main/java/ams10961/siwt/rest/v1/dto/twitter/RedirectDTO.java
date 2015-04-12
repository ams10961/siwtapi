package ams10961.siwt.rest.v1.dto.twitter;


/*
 * Non-persisted results DTO 
 */
public class RedirectDTO {

	private String redirectURL;

	public RedirectDTO() {
		// do nothing
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[TwitterRedirectDTO");
		out.append("|").append(this.getRedirectURL());
		out.append("]");
		return out.toString();
	}
	
}
