package dospring.payload.response;

/**
 * MessageResponse.
 *
 * <p>Enterprise V4+ documentation block.
 */
public class MessageResponse {
	  private String message;

	  public MessageResponse(String message) {
	    this.message = message;
	  }

	  public String getMessage() {
	    return message;
	  }

	  public void setMessage(String message) {
	    this.message = message;
	  }
	}
