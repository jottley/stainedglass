package org.alfresco.integrations.google.glass.exceptions;

public class GoogleGlassAuthenticationException extends Exception {

    /**
     * @author Jared Ottley <jared.ottley@alfresco.com>
     */
    private static final long serialVersionUID = 1L;


    public GoogleGlassAuthenticationException()
    {
        super();
    }


    public GoogleGlassAuthenticationException(final String message)
    {
        super(message);
    }


    public GoogleGlassAuthenticationException(final Throwable cause)
    {
        super(cause);
    }


    public GoogleGlassAuthenticationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
