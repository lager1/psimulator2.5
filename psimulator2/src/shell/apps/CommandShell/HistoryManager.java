/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import device.Device;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * class that manage histories objects
 *
 * @author Martin Lukáš
 */
public class HistoryManager {

	public static String defaultHistoryPath = "/home/user/history";
	public static String secondHistoryPath = "/home/user/history2";
	
	/**
	 * curently used history
	 */
	private History activeHistory;
	
	/**
	 * internal list of available historys
	 */
	private List<History> listOfHistorys;
	/**
	 * internal historys iterator
	 */
	private ListIterator<History> historysIterator;
	
	
	private Device deviceReference;

	public HistoryManager(Device device) {
		
		this.deviceReference = device;
		this.activeHistory = new History(defaultHistoryPath, deviceReference);
		this.activeHistory.activate();

		this.listOfHistorys = new ArrayList<>(2);
		this.listOfHistorys.add(activeHistory);

		this.historysIterator = listOfHistorys.listIterator();

	}

	/**
	 * GETTER
	 * @return 
	 */
	public History getActiveHistory() {
		return activeHistory;
	}

	/**
	 * set current active history and activate it
	 * @param activeHistory 
	 */
	private void setActiveHistory(History activeHistory) {
		this.activeHistory = activeHistory;
		activeHistory.activate();
	}

	// @TODO rename historys => histories, english grammar failure
	/**
	 * method, that rotate two historys.	
	 */
	public void swapHistory() {
		if (listOfHistorys.size() == 1) {
			listOfHistorys.add(new History(secondHistoryPath,this.deviceReference));			// CREATION OF SECOND HISTORY OBJECT !!
		}

		if (!historysIterator.hasNext()) // reset historys iterator
		{
			this.historysIterator = listOfHistorys.listIterator();
		}

		this.setActiveHistory(historysIterator.next());

	}
	
	/**
	 * save all(two) used historys
	 */
	public void saveAllHistory(){
	
		for (History history : listOfHistorys) {
			history.save();
		}
	
	}
	
	
}
