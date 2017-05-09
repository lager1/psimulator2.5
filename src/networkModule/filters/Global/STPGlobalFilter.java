package networkModule.filters.Global;

import networkModule.L2.Stp.StpState;
import networkModule.STP;
import networkModule.filters.GlobalFilter;

/**
 * Class used for starting STP protocol upon configuration of entire network is done
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPGlobalFilter extends GlobalFilter
{
    protected final StpState state;

    public STPGlobalFilter(StpState state)
    {
        this.state = state;
    }

    @Override
    public void afterSetup()
    {
        System.out.println("Test");
        STP.configurationBPDUGeneration(state);
    }
}
