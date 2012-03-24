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
		boolean pokracovat = process();
        if (pokracovat) {
            start();
        }
	}

    private boolean process() { // sezrany: no ip nat

        String side = nextWord();
        if (side.equals("")) {
            ambiguousCommand();
            return false;
        }
        if (side.startsWith("i")) {
            if (isCommand("inside", side, 1)) {
                inside = true;
            } else {
                return false;
            }
        } else {
            if (isCommand("outside", side, 1)) {
                outside = true;
            } else {
                return false;
            }
        }

        return true;
    }

    private void start() {

        if (no) {
            if (inside) {
//				System.out.println("mazu inside");
                ipLayer.getNatTable().deleteInside(parser.configuredInterface);
            } else if (outside) {
				if (ipLayer.getNatTable().getOutside().name.equals(parser.configuredInterface.name)) {
//					System.out.println("mazu outside");
					ipLayer.getNatTable().deleteOutside();
				}
            }
            return;
        }

        if (inside) {
//			System.out.println("nastavuju inside");
			ipLayer.getNatTable().deleteOutside();
			ipLayer.getNatTable().addInside(parser.configuredInterface);
        } else if (outside) {
            if (ipLayer.getNatTable().getOutside() != null && ! ipLayer.getNatTable().getOutside().name.equals(parser.configuredInterface.name)) {
				printService("Implementace nepovoluje mit vice nastavenych verejnych rozhrani. "
                        + "Takze se rusi aktualni verejne: " + ipLayer.getNatTable().getOutside().name+ " a nastavi se "+parser.configuredInterface.name);
            }
//			System.out.println("nastavuju outside");
			ipLayer.getNatTable().deleteInside(parser.configuredInterface);
			ipLayer.getNatTable().setOutside(parser.configuredInterface);
        }
    }
}
