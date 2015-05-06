/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import dataStructures.DropItem;
import dataStructures.MacAddress;
import dataStructures.packets.L2.EthernetPacket;
import dataStructures.packets.L2Packet;
import dataStructures.BridgeIdentifier;
import device.Device;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetLayer;
import networkModule.L2.Stp.StpSwitchportState;
import networkModule.L2.Stp.StpState;
import networkModule.L2.SwitchportSettings;
import networkModule.Timers.STP.*;
import networkModule.filters.Device.STPDeviceFilter;
import networkModule.filters.L2.StpDataLinkFilter;
import networkModule.filters.Global.STPGlobalFilter;
import shared.HookManager;
import shared.Timer.Timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of network module of generic simple switch.
 * Predpoklada protokol ethernet na vsech rozhranich, ostatni pakety zahazuje.
 *
 * @author neiss
 */
public class SwitchNetworkModule extends NetworkModule implements Loggable {

    public final EthernetLayer ethernetLayer;
    public StpState stpState;
    public Boolean stpEnabled;

    /**
     * Konstruktor sitovyho modulu predpoklada uz hotovej fysickej modul, protoze zkouma jeho nastaveni.
     *
     * @param device
     */
    public SwitchNetworkModule(Device device) {
        super(device);
        ethernetLayer = new EthernetLayer(this);
        stpEnabled = false;
    }

    /**
     * Prijimani od fysickyho modulu.
     *
     * @param packet
     * @param switchportNumber
     */
    @Override
    public void receivePacket(L2Packet packet, int switchportNumber) {
        if (packet.getClass() != EthernetPacket.class) {    //kontrola spravnosti paketu
            Logger.log(getDescription(), Logger.WARNING, LoggingCategory.ETHERNET_LAYER,
                    "Dropping packet: It is not Ethernet packet, it is " + packet.getClass().getName());
            Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getDevice().configID));
        } else {
            ethernetLayer.receivePacket((EthernetPacket) packet, switchportNumber);
        }
    }

    @Override
    public String getDescription() {
        return device.getName() + ": " + getClass().getName();
    }

    @Override
    public boolean isSwitch() {
        return true;
    }

    /**
     * Method used for base configuration of Spanning Tree Protocol
     * Called after all interfaces are created
     * Initializes Port States, Bridge State, Creates timers and Hooks used in Spanning tree protocol
     *
     * @param enabled
     * @param maxAge Bridge Maximal message age
     * @param helloTime Bridge Hello time
     * @param forwardDelay Bridge Forward Delay
     * @param priority Bridge Priority
     */
    public void setupSTP(Boolean enabled, Integer maxAge, Integer helloTime, Integer forwardDelay, Integer priority) {
        stpEnabled = enabled == null ? false : enabled;
        if (stpEnabled == false)
            return;
        Map<Integer, StpSwitchportState> switchportStates = new HashMap<Integer, StpSwitchportState>();
        MacAddress bridgeMacAddress = null;
        for (int i = 0; i < ethernetLayer.getSwitchports().size(); ++i)
        {
            SwitchportSettings s = ethernetLayer.getSwitchports().get(i);
            MacAddress portMacAddress = s.getSwitchportMacAddress();

            StpSwitchportState portState = new StpSwitchportState((short)i);
            // if (getPhysicMod().isSwitchportConnected(i) == false)
            //    portState.state = StpPortStates.PORT_DISABLED;

            switchportStates.put(i, portState);
            if (bridgeMacAddress == null)
                bridgeMacAddress = portMacAddress;
            else if (portMacAddress.isLessOrEqualThan(bridgeMacAddress))
                bridgeMacAddress = portMacAddress;
        }
        BridgeIdentifier bridgeIdentifier = new BridgeIdentifier(bridgeMacAddress, priority.shortValue());

        this.stpState = new StpState(bridgeIdentifier, maxAge.shortValue(), helloTime.shortValue(), forwardDelay.shortValue(), null, this.getDevice());

        for (int i = 0; i < switchportStates.size(); ++i) {
            StpSwitchportState state = switchportStates.get(i);

            state.bridgeState = stpState;
            STP.becomeDesignatedPort(state, stpState);

            String prefix = state.getTimerPrefix();

            Timer.registerIntervalCallback(prefix + "#StpHoldTimer", 1, new STPHoldTimer(state, stpState, this), false);
            Timer.registerIntervalCallback(prefix + "#StpMessageAgeTimer", maxAge.shortValue(), new STPMessageAgeTimer(state, this), false);
            Timer.registerIntervalCallback(prefix + "#StpForwardDelayTimer", forwardDelay.shortValue(), new STPForwardDelayTimer(state), false);
        }

        this.stpState.portsStates = switchportStates;

        Timer.registerIntervalCallback(this.device.getName() + "#StpHelloTime", 1, new STPHelloTimer(stpState, this), true);
        Timer.registerIntervalCallback(this.device.getName() + "#StpTopologyChangeNotificationTimer", 1, new STPTopologyChangeNotificationTimer(this.stpState), false);
        Timer.registerIntervalCallback(this.device.getName() + "#StpTopologyChangeTimer", maxAge.shortValue() + forwardDelay.shortValue(), new STPTopologyChangeTimer(this.stpState), false);

        StpDataLinkFilter filter = new StpDataLinkFilter();

        HookManager.registerHook(ethernetLayer, HookManager.HookType.DATALINK_PRE_LEARNING, filter);
        HookManager.registerHook(ethernetLayer, HookManager.HookType.DATALINK_POST_LEARNING, filter);

        HookManager.registerHook(this.getDevice(), HookManager.HookType.DEVICE_AFTER_LOAD, new STPDeviceFilter(stpState));
        HookManager.registerHook(ethernetLayer, HookManager.HookType.AFTER_SETUP, new STPGlobalFilter(stpState));


        Timer.registerIntervalCallback(this.device.getName() + "#Output", 5, new STPDebugOutput(stpState), true);

        System.out.println("Stp Configured");
    }
}
