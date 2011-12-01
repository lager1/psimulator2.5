/*
 * created 28.10.2011
 */
package device;

import exceptions.ApplicationNotFoundException;
import java.util.List;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ApplicationsList {

	private int counterPID = 0;
	List<Application> applications;

	/**
	 * Try to stop the app with name parameter
	 * @param application name
	 * @return true if app exists and application has stopped
	 */
	public boolean stop(String application) throws ApplicationNotFoundException {
		Application app = getApplication(application);
		return tryStop(app);
	}

	/**
	 * Try to stop the app with PID parameter
	 * @param pid PID
	 * @return true if app exists and application has stopped
	 */
	public boolean stop(int pid) throws ApplicationNotFoundException {
		Application app = getApplication(pid);
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
		return app.stop();
	}

	public Application getApplication(int PID) throws ApplicationNotFoundException {
		for (Application application : applications) {
			if (application.getPID() == PID) {
				return application;
			}
		}
		throw new ApplicationNotFoundException();
	}

	public Application getApplication(String name) throws ApplicationNotFoundException {
		for (Application application : applications) {
			if (application.getName().equals(name)) {
				return application;
			}
		}
		throw new ApplicationNotFoundException();
	}

}
