/*
 * created 2.2.2012
 */

package logging;

import java.util.Map;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ConfigureSystemListener {

	public static void configure(Map<LoggingCategory, Integer> configuration) {
		for (LoggingCategory category : LoggingCategory.values()) {
			configuration.put(category, Logger.ERROR);
		}

		// zde si pridat vlastni pravidla

	}
}
