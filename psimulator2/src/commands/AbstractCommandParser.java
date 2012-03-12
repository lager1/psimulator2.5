/*
 * Vytvoreno 30.1.2012.
 */
package commands;

import commands.LongTermCommand.Signal;
import device.Device;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import psimulator2.Psimulator;
import shell.apps.CommandShell.CommandShell;

/**
 * Abstraktni parser prikazu spolecnej pro linux i pro cisco.
 *
 * Parser si bude muset pamatovat posledni spusteny prikaz, aby kdyz dostane signal SIG_INT, aby dokazal poslat prikaz
 * vypnuti posledni spustene aplikace/prikazu.
 *
 * @author Stanislav Rehak
 */
public abstract class AbstractCommandParser implements Loggable {

	protected CommandShell shell;
	protected Device device;
	/**
	 * Seznam slov v prijatem radku.
	 */
	protected List<String> words;
	/**
	 * Obcas je potreba cely radek.
	 */
	protected String line;
	/**
	 * ukazatel do seznamu slov.
	 */
	protected int ref;

	/**
	 * Vesmes docasne uloziste modu, aby se nemusel predavat do vsech funkci, kde je potreba.
	 * Vlastnikem modu je CommandShell, protoze preci Shell musi vedet, ve kterem stavu on sam je.
	 */
	protected int mode;

	/**
	 * Kvuli prikazum, ktere bezi dlouho, ty budou muset bezet ve vlastnim vlakne a zde bude na ne odkaz.
	 * Az prikaz skonci, tak tohle bude null.
	 */
	public LongTermCommand runningCommand = null;

	public AbstractCommandParser(Device networkDevice, CommandShell shell) {
		this.device = networkDevice;
		this.shell = shell;
	}

	/**
	 * Zpracuje radek.
	 * Tuto metodu bude volat CommandShell
	 *
	 * @param line radek ke zpracovani
	 * @param mode aktualni mod toho shellu (pro cisco)
	 * @return navratovy kod
	 */
	public void processLine(String line, int mode) {
		try {	// chytani vyjimek, aby se nepropagovaly dal do telnetu

			if (runningCommand != null) {
				runningCommand.catchUserInput(line);
				return;
			}

			this.line = line;
			this.mode = mode;
			this.ref = 0;
			splitLine(line);

			if (line.isEmpty()) {
				return;
			}


			if (!processSharedCommands()) {	// zkusi se, jestli to neni spolecnej prikaz
				processLineForParsers();	// jinak se zavola konkretni parser
			}

		} catch (Exception ex) {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_COMMANDS, "Nekde byla hozena vyjimka.", ex);
		}
	}

	public void catchUserInput(String line) {
		if (runningCommand != null) {
			runningCommand.catchUserInput(line);
		} else {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_COMMANDS, "zavolan catchUserInput a pritom neni spusten zadny prikaz!!! Zahazuju vstup..", null);
		}
	}
	
	public abstract void catchSignal(Signal signal);

	/**
	 * Jednoducha metoda pro vypsani pouzitelnejch prikazu. Slouzi k jednoduch emu napovidani.
	 *
	 * @param mode aktualni mod toho shellu (pro cisco)
	 * @return
	 */
	public abstract String[] getCommands(int mode);

	public CommandShell getShell() {
		return shell;
	}

	/**
	 * Tuto metodu musi implementovat parsery.
	 * V tuto chvili uz jsou naplneny promenne words i mode.
	 *
	 */
	protected abstract void processLineForParsers();

//	@Override
//	public void catchSignal(Signal signal) {
//		if (runningCommand != null) {
//			runningCommand.catchSignal(signal);
//		} else {
//			this.catchSignal(signal);
//		}
//	}

	/**
	 * Prikaz notifikuje parser o svym skonceni.
	 * @param exitCode
	 */
	public void finishCommand(int exitCode) {
		this.runningCommand = null;
	}

	/**
	 * Vrati shellu informaci, zda ma vypisovat prompt.
	 * @return
	 */
	public boolean isCommandRunning() {
		if (runningCommand == null) {
			return true;
		}
		return false;
	}

	/**
	 * Slouzi k servisnim vypisum o napr nepodporovanych prikazech.
	 * @param line
	 */
	public void printService(String line) {
		shell.printLine(Psimulator.getNameOfProgram()+": "+line);
	}

	/**
	 * Tahle metoda postupne vraci words, podle vnitrni promenny ref. Pocita s tim, ze prazdny retezec ji nemuze prijit.
	 *
	 * @return prazdny retezec, kdyz je na konci seznamu
	 */
	public String nextWord() {
		String res;
		if (ref < words.size()) {
			res = words.get(ref);
			ref++;
		} else {
			res = "";
		}
		return res;
	}

	/**
	 * Tahle metoda postupne dela to samy, co horni, ale nezvysuje citac. Slouzi, kdyz je potreba zjistit, co je dal za
	 * slovo, ale zatim jenom zjistit.
	 *
	 * @return prazdny retezec, kdyz je na konci seznamu
	 */
	public String nextWordPeek() {
		String res;
		if (ref < words.size()) {
			res = words.get(ref);
		} else {
			res = "";
		}
		return res;
	}

	public List<String> getWords() {
		return words;
	}

	protected int getRef() {
		return ref;
	}

	/**
	 * Tomasova debugovaci metoda.
	 * @return
	 */
	public String getWordsAsString(){
		String vratit="Words: ";
		for(String s:words){
			vratit+="^"+s+"^ ";
		}
		return vratit;
	}

	/**
	 * V teto metode je se kontroluje, zda neprisel nejaky spolecny prikaz, jako napr. save ci v budoucnu jeste jine.
	 *
	 * @return vrati true, kdyz konkretni parser uz nema pokracovat dal v parsovani (tj. jednalo se o spolecny prikaz)
	 * @autor Stanislav Řehák
	 */
	protected boolean spolecnePrikazy(boolean debug) {
//        AbstraktniPrikaz prikaz;
//
//        if (slova.get(0).equals("uloz") || slova.get(0).equals("save")) {
//            prikaz = new Uloz(pc, kon, slova);
//            return true;
//        }
//        if (debug) {
//            if (slova.get(0).equals("nat")) {
//                kon.posliPoRadcich(pc.natTabulka.vypisZaznamyDynamicky(), 10);
//                kon.posliRadek("______________________________________________");
//                kon.posliPoRadcich(pc.natTabulka.vypisZaznamyCisco(), 10);
//                return true;
//            }
//        }
		return false;
	}

	/**
	 * Tato metoda rozseka vstupni string na jednotlivy words (jako jejich oddelovac se bere mezera) a ulozi je do
	 * seznamu words, ktery dedi od Abstraktni. @autor Stanislav Řehák
	 */
	private void splitLine(String line) {
		words = new ArrayList<>();
		line = line.trim(); // rusim bile znaky na zacatku a na konci
		String[] bileZnaky = {" ", "\t"};
		for (int i = 0; i < bileZnaky.length; i++) { // odstraneni bylych znaku
			while (line.contains(bileZnaky[i] + bileZnaky[i])) {
				line = line.replace(bileZnaky[i] + bileZnaky[i], bileZnaky[i]);
			}
		}
		String[] pole = line.split(" ");
		words.addAll(Arrays.asList(pole));
	}

	/**
	 * Obsluhuje prikazy spolecny pro linux i cisco.
	 * @return true, kdyz to byl spolecny prikaz a je tedy vyrizen.
	 */
	private boolean processSharedCommands() {

		String commandName = nextWordPeek();

		if(commandName.equals("save")||commandName.equals("uloz")){
			PsimulatorSave cmd = new PsimulatorSave(this);
			cmd.run();
			return true;
		}

		return false;
	}

	@Override
	public String getDescription() {
		return device.getName()+": AbstractCommandParser";
	}


}
