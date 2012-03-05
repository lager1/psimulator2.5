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
	CABEL_SENDING,	// zpravy od kabelu o posilani paketu
	PHYSICAL,	// zpravy od fysickyho modulu sitovejch zarizeni
	LINK,	// posilani paketu na linkovy vrstve
	NET,
	TRANSPORT,

	// vlastni tridy, ktere chceme take logovat:
	PACKET_FILTER,
	ETHERNET_LAYER, // zpravy z EthernetLayer sitovyho modulu
	TELNET,
	ABSTRACT_NETWORK,
	FILE_SYSTEM,
	// !!! PRI PRIDAVANI KATEGORII PROSIM UVEDTE KTRATKY POPIS, CO KATEGORIE ZNAMENA A KDE SE BUDE POUZIVAT !!!
}
