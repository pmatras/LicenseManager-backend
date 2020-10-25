package io.licensemanager.backend.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String getServerBaseURL(final HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();

        return requestURL.substring(0, requestURL.indexOf(request.getServletPath()));
    }
}
