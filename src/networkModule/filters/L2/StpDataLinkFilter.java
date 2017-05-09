package networkModule.filters.L2;

import dataStructures.packets.L2.EthernetPacket;
import dataStructures.packets.L3.STP.BaseBPDU;
import dataStructures.packets.L3.STP.ConfigurationBPDU;
import dataStructures.packets.L3Packet;
import device.Device;
import networkModule.L2.EthernetInterface;
import networkModule.L2.Stp.StpState;
import networkModule.L2.Stp.StpSwitchportState;
import networkModule.NetworkModule;
import networkModule.STP;
import networkModule.SwitchNetworkModule;
import networkModule.filters.AbstractDataLinkFilter;
import physicalModule.Switchport;
import shared.Timer.Timer;

/**
 * Class used for reacting to STP packets and filtering communication based on STP protocol
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class StpDataLinkFilter extends AbstractDataLinkFilter {

    // Method handling recieved Configuration BPDU
    protected Boolean handleConfigurationBPDU(ConfigurationBPDU bpdu,
                                              EthernetInterface _interface,
                                              Switchport port,
                                              int switchportNumber,
                                              Device device,
                                              SwitchNetworkModule switchNetworkModule,
                                              StpState bridgeState,
                                              StpSwitchportState portState,
                                              StpSwitchportState rootPortState
                                                    )
    {
        System.out.println("[" + device.getName() + "][Port #" + switchportNumber + "][PreLearning] " + "Port State: " + portState.state.toString() + " BPDU Root: " + bpdu.rootIdentifier.toString());

        if (STP.betterPath(bpdu, portState, bridgeState)) {
            Boolean wasRoot = bridgeState.designatedRoot == bridgeState.bridgeIdentifier;
            System.out.println("[" + device.getName() + "][Port #" + switchportNumber + "][PreLearning] " + "Found Better Path, Me: " + bridgeState.designatedRoot.toString() + "/" + portState.portIdentifier + "   <-> BPDU: " + bpdu.rootIdentifier.toString() + "/" + bpdu.portIdentifier);
            STP.recordConfigurationInformation(bpdu, portState);
            STP.configurationUpdate(bridgeState);
            STP.portStateSelection(bridgeState);
           // System.out.println("[" + device.getName() + "][Port #" + switchportNumber + "][PreLearning] " + "Configuration done");

            Timer.activateTimer(switchNetworkModule.getDevice().getName() + "#Port_" + switchportNumber +"#StpMessageAgeTimer");

            if (bridgeState.designatedRoot != bridgeState.bridgeIdentifier &&
                    wasRoot) {
                System.out.println("[" + device.getName() + "][Port #"  + switchportNumber  + "][PreLearning] " + "Deactivating Hello Timer Of: " + switchNetworkModule.getDevice().getName());
                Timer.deactivateTimer(switchNetworkModule.getDevice().getName() + "#StpHelloTime");

                if (bridgeState.topologyChangeDetected)
                {
  //                  System.out.println("Topology Change Detected  ... Forcing Topology Change Notification");
                    Timer.deactivateTimer(switchNetworkModule.getDevice().getName() + "#StpTopologyChangeTimer");
                    STP.transmitTopologyChangeNotificationBPDU(bridgeState);
                    Timer.activateTimer(switchNetworkModule.getDevice().getName() + "#StpTopologyChangeNotificationTimer");
                }
            }



            if (bridgeState.rootPort == switchportNumber) {
          //      System.out.println("[" + device.getName() + "][Port #"  + switchportNumber  + "][PreLearning] " + "Updating Bridge timeout values");
                STP.updateBridgeTimeoutValues(bpdu, bridgeState);
                STP.configurationBPDUGeneration(bridgeState);
                if (portState.topologyChangeAck)
                    STP.topologyChangeAcknowledged(bridgeState); // 8.6.15
            }

            // System.out.println(bridgeState.designatedRoot + " !=  " + bridgeState.bridgeIdentifier + " = " + (bridgeState.designatedRoot != bridgeState.bridgeIdentifier));

        }
        else {
            System.out.println("[" + device.getName() + "][Port #"  + switchportNumber  + "][PreLearning] " + "Replying Configuration BPDU (My Root: " + bridgeState.designatedRoot.toString() +  "   /// BPDU root: " + bpdu.rootIdentifier.toString() + ")");
            STP.transmitConfigurationBPDU(bridgeState, portState);
        }

        return false;
    }


    // Method handling recieved Topology Change Notification BPDU
    protected Boolean handleTopologyChangeNotificationBPDU(StpState bridgeState,
                                                            StpSwitchportState portState)
    {
        STP.topologyChangeDetection(bridgeState);
        STP.acknowledgeTopologyChange(bridgeState, portState);
        return false;
    }

    @Override
    public Boolean preLearning(EthernetPacket packet, EthernetInterface intrfc, Switchport port, int switchportNumber, Device device) {
        NetworkModule module = device.getNetworkModule();
        if (module.isSwitch()) {
            SwitchNetworkModule switchNetworkModule = (SwitchNetworkModule) module;
            if (switchNetworkModule.stpEnabled == false)
                return true;

            StpState bridgeState = switchNetworkModule.stpState;
            StpSwitchportState portState = switchNetworkModule.stpState.portsStates.get(switchportNumber);
            StpSwitchportState rootPortState = switchNetworkModule.stpState.portsStates.get(bridgeState.rootPort);


            if (((L3Packet)packet.data).getType() == L3Packet.L3PacketType.STP) {

                BaseBPDU bpdu = (BaseBPDU) packet.data;
                System.out.println("[" + device.getName() + "][Port #" +switchportNumber + "] Recieved STP packet  Hash: " + System.identityHashCode(packet));
                if (bpdu.type == STP.BPDUTypes.CONFIGURATION_BPDU.getValue())
                    handleConfigurationBPDU((ConfigurationBPDU)bpdu, intrfc, port, switchportNumber, device, switchNetworkModule, bridgeState, portState, rootPortState);
                else if (bpdu.type == STP.BPDUTypes.TOPOLOGY_CHANGE_NOTIFICATION_BPDU.getValue() &&
                        portState.designatedPort == portState.portIdentifier &&
                        portState.designatedBridge == bridgeState.bridgeIdentifier)
                    handleTopologyChangeNotificationBPDU(bridgeState, portState);
                return false;
            }

            System.out.println("Filtering Packet type: " + ((L3Packet)packet.data).getType() + "  @ [" + portState.bridgeState.device.getName()  +  "][Port #" + portState.portIdentifier + "]  Port State: " + portState.state + " Packet code: " + System.identityHashCode(packet));
            switch (portState.state) {
                case PORT_DISABLED:
                    return false;

                case PORT_BLOCKING:
                    return false;

                case PORT_LISTENING:
                    return false;

                case PORT_LEARNING:
                case PORT_FORWARDING:
                    return true;
            }
        }
        return true;
    }

    @Override
    public Boolean postLearning(EthernetPacket packet, EthernetInterface intrfc, Switchport port, int switchportNumber, Device device) {
        NetworkModule module = device.getNetworkModule();
        if (module.isSwitch()) {
            SwitchNetworkModule switchNetworkModule = (SwitchNetworkModule) module;
            if (switchNetworkModule.stpEnabled == false)
                return true;

            StpSwitchportState portState = switchNetworkModule.stpState.portsStates.get(switchportNumber);
//            System.out.println("Filtering Packet type: " + ((L3Packet)packet.data).getType() + "  @ [" + portState.bridgeState.device.getName()  +  "][Port #" + portState.portIdentifier + "]  Port State: " + portState.state + " Packet code: " + packet.hashCode());
            switch (portState.state) {
                case PORT_LEARNING:
                    return false;
                case PORT_FORWARDING:
                    return true;
            }
        }
        return true;
    }
}
