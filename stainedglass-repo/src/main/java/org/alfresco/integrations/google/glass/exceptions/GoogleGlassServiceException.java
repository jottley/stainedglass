package org.alfresco.integrations.google.glass.exceptions;

public class GoogleGlassServiceException extends Exception {


    /**
     * @author Jared Ottley <jared.ottley@alfresco.com>
     */
    private static final long serialVersionUID = 1L;

    private int               passedStatusCode = -1;


    /**
     * Returns the status code passed to the exception. Return -1 if no status
     * code passed.
     * 
     * @return
     */
    public int getPassedStatusCode()
    {
        return passedStatusCode;
    }


    public GoogleGlassServiceException(String message)
    {
        super(message);
    }


    public GoogleGlassServiceException(String message, int passedStatusCode)
    {
        super(message);
        this.passedStatusCode = passedStatusCode;
    }


    public GoogleGlassServiceException(Throwable cause)
    {
        super(cause);
    }


    public GoogleGlassServiceException(Throwable cause, int passedStatusCode)
    {
        super(cause);
        this.passedStatusCode = passedStatusCode;
    }


    public GoogleGlassServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public GoogleGlassServiceException(String message, Throwable cause, int passedStatusCode)
    {
        super(message, cause);
        this.passedStatusCode = passedStatusCode;
    }
}
