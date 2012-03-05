/*
 * Vytvoreno 30.1.2012.
 */
package commands;

import device.Device;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import shell.apps.CommandShell.CommandShell;

/**
 * Abstraktni parser prikazu spolecnej pro linux i pro cisco.
 *
 * Parser si bude muset pamatovat posledni spusteny prikaz, aby kdyz dostane signal SIG_INT, aby dokazal poslat prikaz
 * vypnuti posledni spustene aplikace/prikazu.
 *
 * @author neiss
 */
public abstract class AbstractCommandParser {

	protected CommandShell shell;
	protected Device networkDevice;
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
	private int ref;

	/**
	 * Vesmes docasne uloziste modu, aby se nemusel predavat do vsech funkci, kde je potreba.
	 * Vlastnikem modu je CommandShell, protoze preci Shell musi vedet, ve kterem stavu on sam je.
	 */
	protected int mode;

	public AbstractCommandParser(Device networkDevice, CommandShell shell) {
		this.networkDevice = networkDevice;
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
	public int processLine(String line, int mode) {
		this.line = line;
		this.mode = mode;
		splitLine(line);


		return processLineForParsers();
	}

	/**
	 * Slouzi k predavani uzivatelskyho vstupu prave spustenymu prikazu (kdyz se ten prikaz napr. na neco pta.
	 *
	 * @param userInput
	 */
	public abstract void catchUserInput(String userInput);

	/**
	 * Zavola parser se signalem od uzivatele. napr.: Ctrl+C, Ctrl+Z, ..
	 *
	 * @param sig
	 */
	public abstract void catchSignal(int sig);

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
	protected abstract int processLineForParsers();

	/**
	 * Tahle metoda postupne vraci words, podle vnitrni promenny ref. Pocita s tim, ze prazdny retezec ji nemuze prijit.
	 *
	 * @return prazdny retezec, kdyz je na konci seznamu
	 */
	protected String nextWord() {
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
	protected String nextWordPeek() {
		String res;
		if (ref < words.size()) {
			res = words.get(ref);
		} else {
			res = "";
		}
		return res;
	}

	protected int getRef() {
		return ref;
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
		words = new ArrayList<String>();
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
}
