/*
 * created 28.10.2011
 */
package networkDevice;

import java.util.List;

/**
 * 
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ApplicationsList {
	
	int counterPID = 0;
	List<Application> applications;
	
	/**
	 * Try to stop the app with name parameter
	 * @param application name
	 * @return true if app exists and application has stopped
	 */
	public boolean stop(String application) {
		Application app = getApplication(application);
		return tryStop(app);
	}
	
	/**
	 * Try to stop the app with PID parameter
	 * @param application PID
	 * @return true if app exists and application has stopped
	 */
	public boolean stop(int application) {
		Application app = getApplication(application);
		return tryStop(app);
	}
	
	/**
	 * Try to stop the app.
	 * @param app application or null
	 * @return true if app exists and application has stopped
	 */
	private boolean tryStop(Application app) {
		if (app == null) {
			return false;
		}
		app.stop();
		return true;
	}
	
	public Application getApplication(int PID) {
		for (Application application : applications) {
			if (application.getPID() == PID) {
				return application;
			}
		}
		return null;
	}
	
	public Application getApplication(String name) {
		for (Application application : applications) {
			if (application.getName().equals(name)) {
				return application;
			}
		}
		return null;
	}
	
}
