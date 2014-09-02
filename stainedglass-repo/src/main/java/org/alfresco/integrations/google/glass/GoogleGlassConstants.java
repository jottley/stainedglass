
package org.alfresco.integrations.google.glass;


import java.util.Arrays;
import java.util.List;


public interface GoogleGlassConstants
{

    public static final String       REMOTE_SYSTEM    = "googleglass";

    public static final String       REDIRECT_URI     = "http://jared.ottleys.net/google-auth-return.html";

    public static final String       SCOPE            = "https://www.googleapis.com/auth/glass.timeline https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

    public static final List<String> SCOPES           = Arrays.asList("https://www.googleapis.com/auth/glass.timeline", "https://www.googleapis.com/auth/userinfo.profile");

    public static final String       APPLICATION_NAME = "Alfresco-GoogleGlass/1.0";

}
