/*
 * created 2.2.2012
 */

package logging;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public enum LoggingCategory {
	// pouze k posilani paketu
	CABEL_SENDING,
	PHYSICAL,
	LINK,
	NET,
	TRANSPORT,

	// vlastni tridy, ktere chceme take logovat
	PACKET_FILTER,
	ETHERNET_LAYER,
        TELNET,
        
        ABSTRACT_NETWORK

	;

}
