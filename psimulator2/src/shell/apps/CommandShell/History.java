/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * třída pro uchování historií konsole + přístup k nim
 *
 * @author Martin Lukáš
 */
public class History {

	private List<String> activeHistory;
	private List<List> listOfHistorys;
//    private int historyIterator = 0;
	private ListIterator<String> commandIterartor;
	private ListIterator<List> historysIterator;
	String activeHistoryLine;
	private boolean calledNext = false;
	private boolean calledPrevious = false;

	public History() {
		this.activeHistory = new ArrayList<String>();
		this.commandIterartor = this.activeHistory.listIterator(this.activeHistory.size());

		this.listOfHistorys = new ArrayList<>(2);
		this.listOfHistorys.add(activeHistory);
		this.historysIterator = listOfHistorys.listIterator();

	}

	/**
	 * pokud uživatel odešle příkaz, pak by se měl nastavit iterátor do výchozí pozice, aby při dalším listování
	 * historií se začalo posledním odeslaným příkazem
	 */
	private void resetIterator() {
		this.commandIterartor = this.activeHistory.listIterator(this.activeHistory.size());
	}

	/**
	 * metoda, která rotuje dvě různé historie
	 */
	public void swapHistory() {
		if (listOfHistorys.size() == 1) {
			listOfHistorys.add(new ArrayList<String>());
		}

		if (!historysIterator.hasNext()) // pokud nemá další pak nastavím iterátor nastavím na začátek
		{
			this.historysIterator = listOfHistorys.listIterator();
		}

		this.activeHistory = historysIterator.next();

		resetIterator();

	}

	/**
	 * command entered
	 *
	 * @param command
	 */
	public void add(String command) {

		command = command.trim();

		if (command.isEmpty() || command.equalsIgnoreCase("")) { // do not add empty command
			return;
		}

		if (!activeHistory.isEmpty()) {

			String lastCommand = activeHistory.get(activeHistory.size() - 1).trim();

			if (command.equalsIgnoreCase(lastCommand)) { // do not add two same commands
				return;
			}
		}

		this.calledNext = false;
		this.calledPrevious = false;
		this.activeHistoryLine = null;
		this.activeHistory.add(command);
		resetIterator();

	}

	/**
	 * iterating in history to the oldest command
	 *
	 * @param sb
	 * @return
	 */
	public String handlePrevious(StringBuilder sb) {

		String ret = "";

		if (activeHistory.isEmpty()) {
			return ret;
		}

		if (!this.commandIterartor.hasNext()) // there is no next = iterator pointing at the end => store active commandLine
		{
			this.activeHistoryLine = sb.toString();
		}

		if (calledNext) { // double iterate
			if (this.commandIterartor.hasPrevious()) {
				this.commandIterartor.previous();
			}

		}

		if (this.commandIterartor.hasPrevious()) {

			ret = this.commandIterartor.previous();
			calledNext = false;
			calledPrevious = true;


			sb.setLength(0);
			sb.append(ret);

		}

		return ret;

	}

	/**
	 * iterating in history to the newest command
	 *
	 * @param sb
	 * @return
	 */
	public String handleNext(StringBuilder sb) {

		String ret = "";

		if (activeHistory.isEmpty()) {
			return ret;
		}

		if (calledPrevious) { // double iterate
			if (this.commandIterartor.hasNext()) {
				this.commandIterartor.next();
			}
		}


		if (this.commandIterartor.hasNext()) {
			ret = this.commandIterartor.next();

			this.calledNext = true;
			this.calledPrevious = false;

			sb.setLength(0);
			sb.append(ret);

		} else if (this.activeHistoryLine != null) {
			ret = this.activeHistoryLine;
			this.activeHistoryLine = null;

			this.calledNext = false;
			this.calledPrevious = false;
			
			sb.setLength(0);
			sb.append(ret);

		}

		return ret;
	}
}
