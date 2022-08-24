package de.kosmos_lab.platform.web;

import de.kosmos_lab.web.server.servlets.BaseServletRequest;
import de.kosmos_lab.web.server.servlets.MyHttpServletRequest;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;


/**
 * "small" wrapper for our Requests
 * primarily used to cache the body / jsonobject body objects and allow the direct getting of ints etc
 */
public class KosmoSHttpServletRequest extends BaseServletRequest {
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSHttpServletRequest");
    


    public KosmoSHttpServletRequest(HttpServletRequest request) {
        super(request);
    }
    
    



    @Nonnull
    public String getUUID() throws ParameterNotFoundException {
        String v = getParameter("uuid");
        if (v != null) {
            return v;
        }
        v = getParameter("id");
        if (v != null) {
            return v;
        }
        throw new ParameterNotFoundException("uuid");
        
    }
    public IUser getKosmoSUser() {
        return (IUser) getAttribute("user");
    }

}
