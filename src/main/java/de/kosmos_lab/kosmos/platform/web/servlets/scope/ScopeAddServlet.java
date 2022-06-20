package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.data.Scope;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.ScopeAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.UserNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/scope/add"}, loadOnStartup = 1)
public class ScopeAddServlet extends AuthedServlet {
    
    
    public ScopeAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, ParameterNotFoundException, AlreadyExistsException, GroupNotFoundException {
        String sname = request.getString("name");
        
        try {
            Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
            throw new ScopeAlreadyExistsException(sname);
            
        } catch (NotFoundInPersistenceException e) {
            //e.printStackTrace();
            Scope scope = controller.addScope(sname, request.getKosmoSUser());
            
            try {
                JSONArray users = request.getJSONArray("users");
                if ( users != null ) {
                    for ( int i=0;i<users.length();i++) {
                        Object utoken = users.get(i);
    
                        if (utoken instanceof String) {
                            IUser u = controller.getUser(users.getString(i));
                            if (u != null ) {
                                scope.addUser(u);
                            }
                        }
                        if (utoken instanceof Integer) {
                            try {
                                scope.addUser(controller.getUser(users.getInt(i)));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (utoken instanceof JSONObject) {
    
                            try {
                                scope.addUser(controller.getUser(users.getJSONObject(i).getInt("id")));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }catch (ParameterNotFoundException ex) {
                ex.printStackTrace();
            
            }
            try {
                JSONArray users = request.getJSONArray("admins");
                if ( users != null ) {
                    for ( int i=0;i<users.length();i++) {
                        Object utoken = users.get(i);
                        if (utoken instanceof String) {
                            IUser u = controller.getUser(users.getString(i));
                            if (u != null ) {
                                scope.addAdmin(u);
                            }
                        }
                        if (utoken instanceof Integer) {
                            try {
                                scope.addAdmin(controller.getUser(users.getInt(i)));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (utoken instanceof JSONObject) {
        
                            try {
                                scope.addAdmin(controller.getUser(users.getJSONObject(i).getInt("id")));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }catch (ParameterNotFoundException ex) {
                ex.printStackTrace();
        
            }
            try {
                JSONArray users = request.getJSONArray("adminGroups");
                if ( users != null ) {
                    for ( int i=0;i<users.length();i++) {
                        Object utoken = users.get(i);
                        if (utoken instanceof String) {
                            
                                Group g = controller.getGroup(users.getString(i), CacheMode.CACHE_AND_PERSISTENCE);
                                if (g != null ) {
                                    scope.addAdminGroup(g);
                                }

                            
                        }
                        if (utoken instanceof Integer) {
                            
                            
                                scope.addAdminGroup(controller.getGroup(users.getInt(i), CacheMode.CACHE_AND_PERSISTENCE));
                            

                        }
                        if (utoken instanceof JSONObject) {
                    

                                scope.addAdminGroup(controller.getGroup(users.getJSONObject(i).getInt("id"), CacheMode.CACHE_AND_PERSISTENCE));

                        }
                    }
                }
            }catch (ParameterNotFoundException ex) {
                ex.printStackTrace();
        
            }
            try {
                JSONArray users = request.getJSONArray("userGroups");
                if ( users != null ) {
                    for ( int i=0;i<users.length();i++) {
                        Object utoken = users.get(i);
                        if (utoken instanceof String) {
                    
                                Group g = controller.getGroup(users.getString(i), CacheMode.CACHE_AND_PERSISTENCE);

                                    scope.addUserGroup(g);


                    
                        }
                        if (utoken instanceof Integer) {
                    
                    

                                scope.addUserGroup(controller.getGroup(users.getInt(i), CacheMode.CACHE_AND_PERSISTENCE));
                        

                        }
                        if (utoken instanceof JSONObject) {
                    

                                scope.addUserGroup(controller.getGroup(users.getJSONObject(i).getInt("id"), CacheMode.CACHE_AND_PERSISTENCE));

                        }
                    }
                }
            }catch (ParameterNotFoundException ex) {
                ex.printStackTrace();
        
            }
            sendJSON(request, response, scope.toJSON());
    
            response.setStatus(STATUS_OK);
            
            
            
            
            return;
        }
        
        
    }
    
    
}

