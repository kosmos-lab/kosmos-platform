package de.kosmos_lab.kosmos.platform.web;

import de.dfki.baall.helper.webserver.MyHttpServletRequest;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
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
public class KosmoSHttpServletRequest extends MyHttpServletRequest {
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSHttpServletRequest");
    


    public KosmoSHttpServletRequest(HttpServletRequest request) {
        super(request);
    }
    
    

    public JSONObject getJSONObject(String key) throws ParameterNotFoundException {
        String v = this.getParameter(key);
        if (v != null) {
            return new JSONObject(v);
        }
        try {
            return this.getBodyAsJSONObject().getJSONObject(key);
        } catch (JSONException ex) {
        
        }
        catch (NullPointerException ex ) {
    
        }
        throw new ParameterNotFoundException(key);
        
        
    }
    public JSONArray getJSONArray(String key) throws ParameterNotFoundException {
        String v = this.getParameter(key);
        if (v != null) {
            return new JSONArray(v);
        }
        try {
            return this.getBodyAsJSONObject().getJSONArray(key);
        } catch (JSONException ex) {
        
        }
        catch (NullPointerException ex ) {
    
        }
        throw new ParameterNotFoundException(key);
        
        
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
