//package gov.cdc.izgateway.transformation.security;
//
//import jakarta.annotation.security.RolesAllowed;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.ObjectUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.core.annotation.MergedAnnotation;
//import org.springframework.core.annotation.MergedAnnotations;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import java.lang.reflect.Method;
//import java.util.*;
//
///**
// * This component acts as a registry of methods that have @RolesAllowed annotations
// * to support Access controls.
// *
// */
//@Slf4j
//@Component("xformAccessControlRegistry")
//public class AccessControlRegistry {
//	private Map<String, List<String>> allowedMap = new LinkedHashMap<>();
//	private Map<String, List<String>> specialMap = new LinkedHashMap<>();
//	private static final String[] ALL_METHODS = { "*" };
//
//	public Map<String, List<String>> getControls() {
//		Map<String, List<String>> result = new TreeMap<>();
//
//		for (Map.Entry<String, List<String>> e: allowedMap.entrySet()) {
//			for (String group : e.getValue()) {
//				List<String> users = result.computeIfAbsent(group, k -> new ArrayList<>());
//				users.add(e.getKey());
//			}
//		}
//		for (Map.Entry<String, List<String>> e: specialMap.entrySet()) {
//			for (String group : e.getValue()) {
//				List<String> users = result.computeIfAbsent(group, k -> new ArrayList<>());
//				users.add(e.getKey());
//			}
//		}
//		return result;
//	}
//
//
//	/**
//	 * Register a controller in this registry. Each method and path associated with this controller
//	 * or methods within this controller that have a RolesAllowed annotation will allow access
//	 * to paths specified by the Controller or it's methods.  Methods are registered first so that
//	 * their annotations will take precedence over controller annotations.  Controller annotations
//	 * serve as defaults.
//	 *
//	 * @param controller	The @RestController object to register
//	 */
//	public void register(Object controller) {
//		String[] prefixes = getControllerPrefix(controller.getClass());
//		if (prefixes.length == 0) {
//			String[] noPrefixes = { "" };
//			prefixes = noPrefixes;
//		}
//		for (String prefix: prefixes) {
//			register(controller.getClass(), prefix);
//		}
//	}
//
//	private String[] getControllerPrefix(Class<?> controller) {
//		RequestMapping m = controller.getAnnotation(RequestMapping.class);
//		if (m == null) {
//			return new String[0];
//		}
//		String[] mapping = m.value();
//		if (mapping.length == 0) {
//			return new String[0];
//		}
//		return mapping;
//	}
//
//	public void register(Class<?> controllerClass, String prefix) {
//		RolesAllowed defaultRoles = controllerClass.getAnnotation(RolesAllowed.class);
//		RolesAllowed roles = null;
//		MergedAnnotation<RequestMapping> mapping = null;
//
//		Method[] methods = controllerClass.getMethods();
//		for (Method method: methods ) {
//			MergedAnnotations merged = MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS);
//			mapping = merged.get(RequestMapping.class);
//			if (mapping.equals(MergedAnnotation.missing())) {
//				continue;
//			}
//
//			roles = ObjectUtils.defaultIfNull(method.getAnnotation(RolesAllowed.class), defaultRoles);
//			if (roles == null) {
//				continue;
//			}
//
//			registerPathsAndRoles(prefix, roles, mapping, true);
//		}
//	}
//
//
//	/**
//	 * Register the paths and roles found on the class or one of its methods.
//	 * @param prefix	Prefix for the controller.
//	 * @param roles		Roles associated with the method or class.
//	 * @param mapping	Request Mappings associated with the method or class.
//	 */
//	private void registerPathsAndRoles(String prefix, RolesAllowed roles, MergedAnnotation<RequestMapping> mapping, boolean add) {
//		if (prefix != null && !prefix.endsWith("/")) {
//            // TODO: Paul - why is this here - seems to be causing a doube slash?
//			//prefix += "/";
//		}
//		if (mapping == null) {
//			return;
//		}
//
//		String[] paths = mapping.getStringArray("value");
//		if (paths.length == 0) {
//			String[] emptyPaths = { "" };
//			paths = emptyPaths;
//		}
//
//		String[] allowed = roles != null ? roles.value() : new String[0];
//		if (allowed == null || allowed.length == 0) {
//			return;
//		}
//
//		RequestMethod[] methods = mapping.getValue("method", RequestMethod[].class).orElse(null);
//
//		if (methods != null) {
//			registerPathsAndRoles(prefix, methods, allowed, paths, add);
//		} else {
//			registerPathsAndRoles(prefix, ALL_METHODS, allowed, paths, add);
//		}
//	}
//
//	/**
//	 * Dynamically add an access control for a path.
//	 * @param path	The path to add the access control for.
//	 * @param roles The roles to add the access control for.
//	 */
//
//	public void register(RequestMethod[] methods, String path, String ... roles  ) {
//		String[] paths = { path };
//		registerPathsAndRoles(null, methods, roles, paths, true);
//	}
//
//	/**
//	 * Dynamically remove an access control for a path.
//	 * @param path	The path to add the access control for.
//	 * @param roles The roles to add the access control for.
//	 */
//	public void unregister(RequestMethod[] methods, String path, String ... roles  ) {
//		String[] paths = { path };
//		registerPathsAndRoles(null, methods, roles, paths, false);
//	}
//
//
//	private void registerPathsAndRoles(String prefix, Object[] methods, String[] allowed, String[] paths, boolean add) {
//		if (methods == null || methods.length == 0) {
//			methods = new String[] { "*" };
//		}
//
//		for (String path: paths) {
//			if (prefix != null) {
//				path = prefix + path;
//			}
//			if (path.endsWith("/")) {
//				path = path.substring(0, path.length()-1);
//			}
//			boolean hasHead = false;
//			boolean hasGet = false;
//			for (Object method: methods) {
//				String m = method.toString();
//				updatePathAndMethod(allowed, add, method, path);
//				hasGet = hasGet || m.equals("GET");
//				hasHead = hasHead || m.equals("HEAD");
//			}
//			// Auto register HEAD if GET is registered.
//			if (hasGet && !hasHead) {
//				updatePathAndMethod(allowed, add, "HEAD", path);
//			}
//		}
//	}
//
//	private void updatePathAndMethod(String[] allowed, boolean add, Object method, String path) {
//		String key = getPathParts(method.toString(), path);
//		if (add) {
//			if (key.contains("*")) {
//				specialMap.put(key, Arrays.asList(allowed));
//			} else {
//				allowedMap.put(key, Arrays.asList(allowed));
//			}
//		} else {
//			if (key.contains("*")) {
//				specialMap.remove(key);
//			} else {
//				allowedMap.remove(key);
//			}
//		}
//	}
//
//	/**
//	 * Break a path down into its parts.  Replaces path variables in the form of {variable} with *.
//	 * Returns the list of parts, with any path variables replaced by *.
//	 *
//	 * @param method	The method.
//	 * @param path	The path to break down.
//	 * @return	The parts of the path.
//	 */
//	private String getPathParts(String method, String path) {
//		int end = path.length();
//		if (path.endsWith("/")) {
//			end--;
//		}
//		int start = 0;
//		if (path.startsWith("/")) {
//			start++;
//		}
//		path = path.substring(start, end);
//
//		path = method + " /" + path.replaceAll("/+", "/");
//		String[] parts = path.split("/");
//		for (int i = 0; i < parts.length; i++) {
//			if (parts[i].startsWith("{")) {
//				parts[i] = "*";
//			}
//		}
//		return StringUtils.join(parts, "/");
//	}
//
//	/**
//	 * Get the allowed roles for the specified path.
//	 * @param path	The path to check.
//	 * @return	The allowed roles.
//	 */
//	public List<String> getAllowedRoles(RequestMethod method, String path) {
//		String key = getPathParts(method.toString(), path);
//		List<String> value = allowedMap.get(key);
//		if (value != null) {
//			return value;
//		}
//		for (Map.Entry<String, List<String>> allowed: specialMap.entrySet()) {
//			if (pathMatches(key, allowed.getKey())) {
//				return allowed.getValue();
//			}
//		}
//		log.warn("NO path matching {} on {}", method, path);
//		return Collections.emptyList();
//	}
//
//	private boolean pathMatches(String testKey, String pathKey) {
//		// Simplest case, evaluate first.
//		if (testKey.equals(pathKey)) {
//			return true;
//		}
//		String[] testParts = testKey.split("/");
//		String[] pathParts = pathKey.split("/");
//		// Fix space after method
//		testParts[0] = testParts[0].trim();
//		pathParts[0] = pathParts[0].trim();
//		int i;
//		String pathPart = null;
//		for (i = 0; i < testParts.length && i < pathParts.length; i++) {
//			pathPart = pathParts[i];
//			String testPart = testParts[i];
//			if (!testPart.equals(pathPart) && !"*".equals(pathPart)) {
//				break;
//			}
//		}
//		// Consumed all of both
//		if (i == testParts.length && i == pathParts.length) {
//			return true;
//		}
//		// Last part of pathPart didn't match, but it is **
//		return i == (pathParts.length-1) && "**".equals(pathPart);
//	}
//}
