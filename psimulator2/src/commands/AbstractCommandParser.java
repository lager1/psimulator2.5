/*
 * Vytvoreno 30.1.2012.
 */
package commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstraktni parser prikazu spolecnej pro linux i pro cisco.
 *
 * Parser si bude muset pamatovat posledni spusteny prikaz, aby kdyz dostane signal SIG_INT, aby dokazal poslat prikaz
 * vypnuti posledni spustene aplikace/prikazu.
 *
 * @author neiss
 */
public abstract class AbstractCommandParser {

	protected List<String> words;
	private int ref; // ukazatel do seznamu slov

	/**
	 * Zpracuje radek.
	 *
	 * @param line radek ke zpracovani
	 * @param mode aktuali mod toho shellu (pro cisco)
	 * @return navratovy kod
	 */
	public abstract int processLine(String line, int mode);

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
	 * Jednoducha metoda pro vypsani pouzitelnejch prikazu. Slouzi k jednoduchemu napovidani.
	 *
	 * @param mode aktuali mod toho shellu (pro cisco)
	 * @return
	 */
	public abstract String[] getCommands(int mode);

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
	 * Tato metoda rozseka vstupni string na jednotlivy words (jako jejich oddelovac se bere mezera) a ulozi je do
	 * seznamu words, ktery dedi od Abstraktni. @autor Stanislav Řehák
	 */
	protected void splitLine(String line) {
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
}
