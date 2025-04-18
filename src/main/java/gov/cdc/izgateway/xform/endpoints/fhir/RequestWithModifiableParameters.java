package gov.cdc.izgateway.xform.endpoints.fhir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * This is a request wrapper with a modifiable parameter map.
 * 
 * It creates a copy of the original parameters, allowing them to be modified.
 * It it intended for use with methods that operate directly upon an HttpServletRequest.
 * 
 * @author Audacious Inquiry
 */
public class RequestWithModifiableParameters extends HttpServletRequestWrapper {
    Map<String, List<String>> map;

    RequestWithModifiableParameters(HttpServletRequest req) {
        super(req);
        map = initParameterMap(req);
    }

    private static Map<String, List<String>> initParameterMap(HttpServletRequest req) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e: req.getParameterMap().entrySet()) {
            List<String> l = new ArrayList<>();
            l.addAll(Arrays.asList(e.getValue()));
            m.put(e.getKey(), l);
        }
        return m;
    }

    /**
     * Add a new value the named parameter.  If the named parameter doesn't 
     * exist, it will be created.
     *  
     * @param name    The parameter name.
     * @param value    The parameter value.
     */
    public void addParameter(String name, String value) {
        List<String> l = map.computeIfAbsent(name, k -> new ArrayList<>());
        l.add(value);
    }
    
    /**
     * Remove all parameters for the given parameter.
     * @param name    The parameter
     * @return    The values for this parameter.
     */
    public List<String> removeParameters(String name) {
        return map.remove(name);
    }
    
    /**
     * Remove the specified value for the named parameter.
     * @param name    The parameter name
     * @param value    The value to remove
     * @return    true if the value existed.
     */
    public boolean removeParameter(String name, String value) {
        List<String> l = map.get(name);
        if (l != null) {
            return l.remove(value);
        }
        return false;
    }
    
    @Override
    public String getParameter(String name) {
        List<String> l = map.get(name);
        return l == null || l.isEmpty() ? null : l.get(0); 
    }

    /**
     * Get the parameter map in list form.
     * @return The parameter map in list form instead of array form.
     */
    public Map<String, List<String>> getParameters() {
        return Collections.unmodifiableMap(map);
    }
    
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e: map.entrySet()) {
            result.put(e.getKey(), e.getValue().toArray(new String[0]));
        }
        return result;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(map.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        List<String> l = map.get(name);
        return l == null ? null : l.toArray(new String[0]);
    }

    /**
     * Reset all parameters.
     */
    public void resetParameters() {
        map.clear();
    }
}
