/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * třída pro uchování historií konsole +  přístup k nim
 * @author zaltair
 */
public class History {

    private ArrayList<String> acctualHistory;
    private List<ArrayList> historyList;
//    private int historyIterator = 0;
    private ListIterator<String> iterator;
    private ListIterator<ArrayList> iteratorList;

    public History() {
        this.acctualHistory = new ArrayList<String>();
        this.iterator = this.acctualHistory.listIterator(this.acctualHistory.size());

        this.historyList = new ArrayList<ArrayList>(2);
        this.historyList.add(acctualHistory);
        this.iteratorList = historyList.listIterator();
    }

    /**
     * pokud uživatel odešle příkaz, pak by se měl nastavit iterátor do výchozí
     * pozice, aby při dalším listování historií se začalo posledním odeslaným příkazem
     */
    private void resetIterator() {
        this.iterator = this.acctualHistory.listIterator(this.acctualHistory.size());
    }

    /**
     * metoda, která rotuje dvě různé historie
     */
    public void swapHistory() {
        if (historyList.size() == 1) {
            historyList.add(new ArrayList<String>());
        }

        if (!iteratorList.hasNext()) // pokud nemá další pak nastavím iterátor nastavím na začátek
        {
            this.iteratorList = historyList.listIterator();
        }

        this.acctualHistory = iteratorList.next();

        resetIterator();

    }

    public void add(String command) {

        command = command.trim();

        if (command.isEmpty() || command.equalsIgnoreCase("")) {
            return;
        }

        if (!acctualHistory.isEmpty()) {

            String lastCommand = acctualHistory.get(acctualHistory.size()-1).trim();

            if (command.equalsIgnoreCase(lastCommand)) {
                return;
            }
        }

        this.acctualHistory.add(command);
        resetIterator();

    }

    public String getPreviousCommand() {

        if (acctualHistory.isEmpty()) {
            return "";
        }

        if (this.iterator.hasPrevious()) {
            return this.iterator.previous();
        } else {
            return "";
        }

    }

    public String getNextCommand() {

        if (acctualHistory.isEmpty()) {
            return "";
        }

        if (this.iterator.hasNext()) {
            return this.iterator.next();
        } else {
            return "";
        }

    }
}
