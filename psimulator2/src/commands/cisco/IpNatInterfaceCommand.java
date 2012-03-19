/*
 * created 17.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import networkModule.L3.IPLayer;

/**
 * Class for handling: 'ip nat (inside|outside)'.
 * Outside rozhrani muze byt pouze jedno. V cisco jde sice i vice, ale pro nasi praci to neni potreba.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpNatInterfaceCommand extends CiscoCommand {

	private final boolean no;
	boolean inside = false;
    boolean outside = false;
	private IPLayer ipLayer;

	public IpNatInterfaceCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
		this.ipLayer = getNetMod().ipLayer;
	}

	@Override
	public void run() {
		boolean pokracovat = zpracujRadek();
        if (pokracovat) {
            vykonejPrikaz();
        }
	}

    private boolean zpracujRadek() { // sezrany: no ip nat

        String side = nextWord();
        if (side.equals("")) {
            ambiguousCommand();
            return false;
        }
        if (side.startsWith("i")) {
            if (kontrola("inside", side, 1)) {
                inside = true;
            } else {
                return false;
            }
        } else {
            if (kontrola("outside", side, 1)) {
                outside = true;
            } else {
                return false;
            }
        }

        return true;
    }

    private void vykonejPrikaz() {

        if (no) {
            if (inside) {
//				System.out.println("mazu inside");
                ipLayer.getNatTable().smazRozhraniInside(parser.configuredInterface);
            } else if (outside) {
				if (ipLayer.getNatTable().getOutside().name.equals(parser.configuredInterface.name)) {
//					System.out.println("mazu outside");
					ipLayer.getNatTable().smazRozhraniOutside();
				}
            }
            return;
        }

        if (inside) {
//			System.out.println("nastavuju inside");
			ipLayer.getNatTable().smazRozhraniOutside();
			ipLayer.getNatTable().pridejRozhraniInside(parser.configuredInterface);
        } else if (outside) {
            if (ipLayer.getNatTable().getOutside() != null && ! ipLayer.getNatTable().getOutside().name.equals(parser.configuredInterface.name)) {
				printService("Implementace nepovoluje mit vice nastavenych verejnych rozhrani. "
                        + "Takze se rusi aktualni verejne: " + ipLayer.getNatTable().getOutside().name+ " a nastavi se "+parser.configuredInterface.name);
            }
//			System.out.println("nastavuju outside");
			ipLayer.getNatTable().smazRozhraniInside(parser.configuredInterface);
			ipLayer.getNatTable().nastavRozhraniOutside(parser.configuredInterface);
        }
    }
}
