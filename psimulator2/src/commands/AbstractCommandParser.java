/*
 * Vytvoreno 30.1.2012.
 */
package commands;

/**
 * Abstraktni parser prikazu spolecnej pro linux i pro cisco.
 *
 * Parser si bude muset pamatovat posledni spusteny prikaz, aby kdyz dostane signal SIG_INT,
 * aby dokazal poslat prikaz vypnuti posledni spustene aplikace/prikazu.
 *
 * @author neiss
 */
public abstract class AbstractCommandParser {

    /**
     * Zpracuje radek.
     * @param line radek ke zpracovani
     * @param mode aktuali mod toho shellu (pro cisco)
     * @return navratovy kod
     */
    public int processLine(String line, int mode){
        return 0;
    }

    /**
     * Jednoducha metoda pro vypsani pouzitelnejch prikazu.
     * Slouzi k jednoduchemu napovidani.
     * @param mode aktuali mod toho shellu (pro cisco)
     * @return
     */
    public abstract String [] getCommands(int mode);

	/**
	 * Zavola parser se signalem od uzivatele.
	 * napr.: Ctrl+C, Ctrl+Z, ..
	 * @param sig
	 */
	public abstract void catchSignal(int sig);
}
