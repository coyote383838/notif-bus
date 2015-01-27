package fr.coyot.notifbus.model;


public class Message {
	
	/**
	 * Message Id
	 */
	public Integer id;
	
	/**
	 * The title of the message
	 */
	public String title;

	/**
	 * The content of the message
	 */
	public String content;
	
	/**
	 * The type of the message
	 */
	public String type;
	
	/**
	 * The importance of the message
	 */
	public String importance;
	
	/**
	 * The expiration date of the message
	 */
	public String expirationDate;
	
	/**
	 * Is message display
	 */
	public boolean isAlreadyDisplay;

	/**
	 * Empty constructor
	 */
	public Message() {
		super();
		this.id = -1;
		this.title = null;
		this.content = null;
		this.type = null;
		this.importance = null;
		this.expirationDate = null;
		this.isAlreadyDisplay = false;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return title + "\n\r" + content;
	}
	
	
}
