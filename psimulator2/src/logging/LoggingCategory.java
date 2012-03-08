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
	 * logovaci zpravy z mainu
	 */
	ABSTRACT_NETWORK,
	FILE_SYSTEM,
	/**
	 * zpravy z buildeni z Martinovy konfigurace
	 */
	LOADER_SAVER,
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
