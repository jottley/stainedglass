package org.alfresco.integrations.google.glass.exceptions;

public class GoogleGlassRefreshTokenException extends Exception {
	
    /**
     * @author Jared Ottley <jared.ottley@alfresco.com>
     */
    private static final long serialVersionUID = 1L;


    public GoogleGlassRefreshTokenException()
    {
        super();
    }


    public GoogleGlassRefreshTokenException(String message)
    {
        super(message);
    }


    public GoogleGlassRefreshTokenException(Throwable cause)
    {
        super(cause);
    }


    public GoogleGlassRefreshTokenException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
