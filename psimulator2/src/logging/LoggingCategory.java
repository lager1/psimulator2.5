/*
 * created 2.2.2012
 */
package logging;

/**
 * Kategorie logovani.
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public enum LoggingCategory {

// pouze k posilani paketu:

	/**
	 * zpravy od kabelu o posilani paketu
	 */
	CABEL_SENDING,
	/**
	 * zpravy od fysickyho modulu sitovejch zarizeni
	 */
	PHYSICAL,
	/**
	 * posilani paketu na linkovy vrstve
	 */
	LINK,
	/**
	 * posilani paketu na sitovy vrstve
	 */
	NET,
	/**
	 * posilani paketu na transportni vrstve
	 */
	TRANSPORT,


// vlastni tridy, ktere chceme take logovat:

	PACKET_FILTER,
	/**
	 * zpravy z EthernetLayer sitovyho modulu
	 */
	ETHERNET_LAYER,
	/**
	 * zpravy z ip layer
	 */
	IP_LAYER,
	TELNET,
	/**
	 * nacitani a ukladani ukladacich struktur do xml souboru (povetsinou balicek psimulator2)
	 */
	XML_LOAD_SAVE,
	/**
	 * nacitani a ukladani nastaveni samotnyho simulatoru do ukladacich struktur (balicek config.configTransformer)
	 */
	NETWORK_MODEL_LOAD_SAVE,
	FILE_SYSTEM,
	/**
	 * zpravy z buildeni z Martinovy konfigurace
	 */
	LOADER_SAVER,
	/**
	 * Zpravy ze simulatoru pro tridu, kterou sem nechci pridavat.
	 */
	GENERIC,
	/**
	 * Zpravy z AbstractCommandParser a AbstractCommand, tedy spolecny pro linux i cisco.
	 */
	GENERIC_COMMANDS,
	/**
	 * Zpravy z aplikaci, kdyz nechci urcit konretni aplikaci.
	 */
	GENERIC_APPLICATION,

	/**
	 * Zpravy command parseru na linuxu.
	 */
	LINUX_COMMAND_PARSER,

	/**
	 * Zpravy z jednotlivych cisco prikazu.
	 */
	CISCO_COMMAND_PARSER,

	// !!! PRI PRIDAVANI KATEGORII PROSIM UVEDTE KTRATKY JAVADOC, CO KATEGORIE ZNAMENA A KDE SE BUDE POUZIVAT !!!

}
