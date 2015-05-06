package networkModule;

import dataStructures.MacAddress;
import dataStructures.packets.L2.EthernetPacket;
import dataStructures.packets.L3.STP.ConfigurationBPDU;
import dataStructures.packets.L3.STP.TopologyChangeNotificationBPDU;
import dataStructures.packets.L3Packet;
import networkModule.L2.EthernetInterface;
import networkModule.L2.Stp.StpPortStates;
import networkModule.L2.Stp.StpState;
import networkModule.L2.Stp.StpSwitchportState;
import shared.Timer.Timer;

/**
 * Class used as container for functions used in Spanning Tree Protocol
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STP
{
    public enum STPVersions
    {
        BASIC(0x00),
        RSTP(0x02),
        MSTP(0x03),
        SPT(0x04);

        private byte version;

        STPVersions(Integer version)
        {
            this.version = version.byteValue();
        }

        public byte getValue()
        {
            return version;
        }
    }

    public enum BPDUTypes
    {
        CONFIGURATION_BPDU(0),
        TOPOLOGY_CHANGE_NOTIFICATION_BPDU(128);

        private byte type;

        BPDUTypes(Integer type)
        {
            this.type = type.byteValue();
        }

        public byte getValue()
        {
            return this.type;
        }
    }

    /**
     * Method generates Configuration BPDU based on Port and Bridge State
     * @param portState
     * @param bridgeState
     * @param messageAge
     * @return
     */
    public static ConfigurationBPDU generateConfigurationBPDU(StpSwitchportState portState, StpState bridgeState, short messageAge)
    {
        byte flags = 0x00;
        if (portState.topologyChangeAck)
            flags |= 0x80;
        if (bridgeState.topologyChange)
            flags |= 0x01;

        ConfigurationBPDU out = new ConfigurationBPDU(STPVersions.BASIC.getValue(),
                BPDUTypes.CONFIGURATION_BPDU.getValue(),
                flags,
                bridgeState.designatedRoot,
                bridgeState.rootPathCost,
                bridgeState.bridgeIdentifier,
                portState.portIdentifier,
                messageAge,
                bridgeState.maxAge,
                bridgeState.helloTime,
                bridgeState.forwardDelay);
        return out;
    }

    /**
     * Method generates Topology Change Notification BPDU
     * @return
     */
    public static TopologyChangeNotificationBPDU generateTopologyChangeNotificationBPDU()
    {
        return new TopologyChangeNotificationBPDU(STPVersions.BASIC.getValue(),
                BPDUTypes.TOPOLOGY_CHANGE_NOTIFICATION_BPDU.getValue());
    }

    //

    /**
     * Method used for transmitting Configration BPDU out of specified port
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.1
     *
     * @param bridgeState
     * @param portState
     */
    public static void transmitConfigurationBPDU(StpState bridgeState,
                                                 StpSwitchportState portState)
    {

        synchronized (portState)
        {
            String holdTimerName = portState.getTimerPrefix() + "#StpHoldTimer";

            if (Timer.hasActiveTimer(holdTimerName))  // Hold timer enabled
            {
                portState.configurationPending = true;
                // System.out.println("[" + bridgeState.device.getName() + "][Port #" + portState.portIdentifier + "] Dropping configuration request, Configuration Pending");
                return;
            }

            SwitchNetworkModule module = (SwitchNetworkModule) bridgeState.device.getNetworkModule();
            EthernetInterface _interface = module.ethernetLayer.getIterfaceOfSwitchport(portState.portIdentifier);


            short messageAge = 0;
            if (bridgeState.designatedRoot != bridgeState.bridgeIdentifier)
            {
                messageAge = Timer.getTimerRemainingTime(portState.getTimerPrefix() + "#StpMessageAgeTimer").shortValue();
            }

            ConfigurationBPDU bpdu = generateConfigurationBPDU(portState, bridgeState, messageAge);
            // System.out.println("[" + bridgeState.device.getName() + "][Port #" + portState.portIdentifier + "] Transmit configuration BPDU Message age: " + bpdu.messageAge + " Bridge Max age: " + bridgeState.maxAge);
            if (bpdu.messageAge < bridgeState.maxAge)
            {
                System.out.println("[" + bridgeState.device.getName() + "][Port #" + portState.portIdentifier + "] Broadcasting Configuration BPDU bpdu: " + System.identityHashCode(bpdu)+
                                    "  Hold timer status: " + Timer.getTimerRemainingTime(holdTimerName) + " / " + Timer.hasActiveTimer(holdTimerName));
                portState.topologyChangeAck = false;
                bridgeState.topologyChange = false;

                EthernetPacket packet = new EthernetPacket(module.ethernetLayer.getSwitchport(portState.portIdentifier).getSwitchportMacAddress(),
                        MacAddress.switchGroupAddress(),
                        L3Packet.L3PacketType.STP,
                        bpdu);

                module.getPhysicMod().sendPacket(packet,
                        portState.portIdentifier);
                Timer.activateTimer(holdTimerName);
                //System.out.println("[" + bridgeState.device.getName() + "][Port #" + portState.portIdentifier + "] Broadcasting Configuration BPDU bpdu: " + System.identityHashCode(bpdu) +
                //        "  Hold timer status: " + Timer.getTimerRemainingTime(holdTimerName) + " / " + Timer.hasActiveTimer(holdTimerName));
            }
        }
    }

    /**
     * Method used for checking whenever received BPDU contains useful data
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.2.2 Record Configuration Information  Conditions
     *
     * @param bpdu
     * @param portState
     * @param bridgeState
     * @return
     */
    public static Boolean betterPath(ConfigurationBPDU bpdu, StpSwitchportState portState, StpState bridgeState)
    {
        /*
        System.out.println("Less or Equal: "  + bpdu.rootIdentifier.isLessOrEqual(bridgeState.designatedRoot) +
                "( BPDU: " + bpdu.rootIdentifier + " <=   Bridge: " + bridgeState.designatedRoot + " )" +  " " +
                "Higher Priority: " + (bpdu.rootIdentifier != bridgeState.designatedRoot));

        System.out.println("Lower root Path: " + (bpdu.rootPathCost < portState.designatedCost) +
                "( BPDU Cost: " + bpdu.rootPathCost + " < Port: " + portState.designatedCost + " )");

        System.out.println("Same root Path: " + (bpdu.rootPathCost == portState.designatedCost));
        System.out.println("Lower or same Designated Root Identifier: " + ( bpdu.bridgeIdentifier.isLessOrEqual(portState.designatedBridge)) +
                " ( BPDU Bridge: " + bpdu.bridgeIdentifier + " <= " + portState.designatedBridge + " )" );

        System.out.println("Not Equal Designated Bridge: " + (bpdu.bridgeIdentifier != portState.designatedBridge) +
                "( BPDU Bridge Identifier: " + bpdu.bridgeIdentifier + "  Port Designated Bridge: " + portState.designatedBridge + " )");

        System.out.println("Current bridge is not designated " + (bridgeState.bridgeIdentifier != portState.designatedBridge) +
                "( Bridge Identifier: " + bridgeState.bridgeIdentifier + "  Port Designated Bridge: " + portState.designatedBridge + " )");

        System.out.println("Higher port priority: " + ( bpdu.portIdentifier > portState.designatedPort) +
                "( BPDU Port Identifier: " + bpdu.portIdentifier + " > " + portState.designatedPort + " )");

        System.out.println("Total Outcome: " +
                (bpdu.rootIdentifier.isLessOrEqual(bridgeState.designatedRoot) &&
                        (bpdu.rootIdentifier != bridgeState.designatedRoot || // Higher priority case
                                bpdu.rootPathCost < portState.designatedCost || // Lower Root Path cost
                                (bpdu.rootPathCost == portState.designatedCost &&
                                        bpdu.bridgeIdentifier.isLessOrEqual(portState.designatedBridge) &&
                                        (bpdu.bridgeIdentifier != portState.designatedBridge ||   // Designated bridge has lower priority than current
                                                (bridgeState.bridgeIdentifier != portState.designatedBridge || // Current bridge is not designated Bridge
                                                        bpdu.portIdentifier < portState.designatedPort)) // Current Designated port has lower priority than port Identifier
                                )
                        )));
        */
        return bpdu.rootIdentifier.isLessOrEqual(bridgeState.designatedRoot) &&
                (bpdu.rootIdentifier != bridgeState.designatedRoot || // Higher priority case
                        bpdu.rootPathCost < portState.designatedCost || // Lower Root Path cost
                        (bpdu.rootPathCost == portState.designatedCost &&
                                bpdu.bridgeIdentifier.isLessOrEqual(portState.designatedBridge) &&
                                (bpdu.bridgeIdentifier != portState.designatedBridge ||   // Designated bridge has lower priority than current
                                (bridgeState.bridgeIdentifier != portState.designatedBridge || // Current bridge is not designated Bridge
                                        bpdu.portIdentifier < portState.designatedPort)) // Current Designated port has lower priority than port Identifier
                        )
                );
    }

    /**
     * Method for updating Port State
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.2 - Record Configuration Information
     *
     * @param packet
     * @param portState
     */
    public static void recordConfigurationInformation(ConfigurationBPDU packet, StpSwitchportState portState)
    {
        synchronized (portState)
        {
            portState.designatedRoot = packet.rootIdentifier;
            portState.designatedCost = packet.rootPathCost;
            portState.designatedBridge = packet.bridgeIdentifier;
            portState.designatedPort = packet.portIdentifier;

            Timer.resetTimer(portState.getTimerPrefix() + "#StpMessageAgeTimer", new Integer(packet.maxAge - packet.messageAge));


            /*
            System.out.println("Updating portstate of: [" + portState.bridgeState.device.getName() + "][Port #" + portState.portIdentifier + "]" +
                    " Designated root: " + portState.designatedRoot +
                    " Designated cost: " + portState.designatedCost +
                    " Designated Bridge: " + portState.designatedBridge +
                    " Designated port: " + portState.designatedPort +
                    " Reseting Message age timer to: " + (packet.maxAge - packet.messageAge) +
                    " Done: " + ((new Date()).getTime()) +
                    " Planned: " + ((new Date()).getTime() + (packet.maxAge - packet.messageAge) * 1000) +
                    " BPDU: " + packet.rand);
            */
        }
    }

    /**
     * Method used for updating Bridge Timeout values
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.3 - Update Brdige Timeout Values
     *
     * @param bpdu
     * @param bridgeState
     */
    public static void updateBridgeTimeoutValues(ConfigurationBPDU bpdu, StpState bridgeState)
    {
        synchronized (bridgeState)
        {
            bridgeState.helloTime = bpdu.helloTime;
            bridgeState.forwardDelay = bpdu.forwardDelay;
            bridgeState.maxAge = bpdu.maxAge;
        }
    }

    /**
     * Method used for broadcasting Configuration BPDU out of all Designated ports
     *
     * Implementation Based on IEEE 802.1d - 1998 -  8.6.4 - Configuration BPDU Generation
     *
     * @param bridgeState
     */
    public static void configurationBPDUGeneration(StpState bridgeState)
    {
        for (StpSwitchportState state : bridgeState.portsStates.values())
        {
/*
             System.out.println("[" + bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] " + "Port: " + state.portIdentifier +
                    "  DesignatedBridge: " + state.designatedBridge + "  Bridge Identifier: " + bridgeState.bridgeIdentifier +
                    "  DesignatedPort: " + state.designatedPort +
                    "  State: " + state.state);
*/
            if (state.designatedBridge == bridgeState.bridgeIdentifier &&
                    state.designatedPort == state.portIdentifier &&
                    state.state != StpPortStates.PORT_DISABLED)
            {
       //         System.out.println("[" + bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] " + "Transmiting configuration BPDU from 8.6.4");
                transmitConfigurationBPDU(bridgeState, state);
            }
        }
    }

    /**
     * Method used for sending Topology change Notification BPDU out of specific port
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.6 - Transmit Topology Change Notification BPDU
     * @param bridgeState
     */
    public static void transmitTopologyChangeNotificationBPDU(StpState bridgeState)
    {
        SwitchNetworkModule module = (SwitchNetworkModule) bridgeState.device.getNetworkModule();
        StpSwitchportState portState = bridgeState.portsStates.get(bridgeState.rootPort);

        if (portState == null)
            return;


        TopologyChangeNotificationBPDU bpdu = STP.generateTopologyChangeNotificationBPDU();

        EthernetPacket packet = new EthernetPacket(module.ethernetLayer.getSwitchport(portState.portIdentifier).getSwitchportMacAddress(),
                MacAddress.switchGroupAddress(),
                L3Packet.L3PacketType.STP,
                bpdu);

        module.getPhysicMod().sendPacket(packet,
                portState.portIdentifier);
    }

    /**
     * Method used for Configuration Update
     *
     * Implementation based on IEEE 802.1d - 1998 - 8.6.7 - Configuration Update
     *
     * @param bridgeState
     */
    public static void configurationUpdate(StpState bridgeState)
    {

        rootPortSelection(bridgeState);
        designatedPortSelection(bridgeState);
    }

    /**
     * Method used for Root Port Selection
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.8 - Root selection
     *
     * @param bridgeState
     * @return
     */
    public static StpSwitchportState rootPortSelection(StpState bridgeState)
    {
        // IEEE 802.1d - 1998 -  8.6.8 -- Root Selection
        StpSwitchportState rootPort = null;
        synchronized (bridgeState)
        {
            for (StpSwitchportState portState : bridgeState.portsStates.values())
            {
                /*
                 System.out.println("Port: #" + portState.portIdentifier +

                        "  Port designated port: " + portState.designatedPort +
                        "  Port Designated root: " + portState.designatedRoot +
                        "  Port Designated bridge: " + portState.designatedBridge +
                        "  Bridge Identifier: " + bridgeState.bridgeIdentifier +
                        "  Ignoring: " + (
                        (portState.designatedPort == portState.portIdentifier && portState.designatedBridge == bridgeState.bridgeIdentifier) ||
                                portState.state == StpPortStates.PORT_DISABLED ||
                                bridgeState.bridgeIdentifier.isLessOrEqual(portState.designatedRoot)
                ));
                */
                if ((portState.designatedPort == portState.portIdentifier && portState.designatedBridge == bridgeState.bridgeIdentifier) ||
                        portState.state == StpPortStates.PORT_DISABLED ||
                        bridgeState.bridgeIdentifier.isLessOrEqual(portState.designatedRoot))
                    continue;
                if (rootPort == null)
                    rootPort = portState;
                else if (portState.designatedRoot.isLessOrEqual(rootPort.designatedRoot))
                {
                    // System.out.println(portState.designatedRoot + " / " + rootPort.designatedRoot + "  ==  " + portState.designatedRoot.compareTo(rootPort.designatedRoot));
                    if (portState.designatedRoot.compareTo(rootPort.designatedRoot) < 0)
                    {
                        // Has the highest priority Root associated with it, i.e., recorded as the Designated Root for the Port
                        // System.out.println("Selecting root port: " + portState.portIdentifier + " instead of: " + rootPort.portIdentifier + " @ Higher priority designated root");
                        rootPort = portState;
                    } else
                    {
                        long portPathCost = portState.designatedCost + portState.pathCost;
                        long rootPathCost = rootPort.designatedCost + rootPort.pathCost;
                        if (portPathCost < rootPathCost)
                        {
                            /*
                            Of two or more Ports with the highest priority Designated Root parameter, has the lowest Root Path
                            Cost associated with it, i.e., the lowest sum of the Designated Cost and Path Cost parameters for any
                            Port;
                             */
                            // System.out.println("Selecting root port: " + portState.portIdentifier + " instead of: " + rootPort.portIdentifier + " @ Lower path cost");
                            rootPort = portState;
                        } else if (portPathCost == rootPathCost)
                        {
                            if (portState.designatedBridge.isLessOrEqual(rootPort.designatedBridge) &&
                                    portState.designatedBridge != rootPort.designatedBridge)
                            {
                                /*
                                Of two or more Ports with the highest priority Designated Root parameter and lowest value of
                                associated Root Path Cost, has the highest priority Bridge Identifier recorded as the Designated Bridge
                                for the LAN to which the Port is attached;
                                 */
                                // System.out.println("Selecting root port: " + portState.portIdentifier + " instead of: " + rootPort.portIdentifier + " @ Lower priority designated bridge");
                                rootPort = portState;
                            } else if (portState.designatedBridge.isLessOrEqual(rootPort.designatedBridge) &&
                                    portState.designatedBridge == rootPort.designatedBridge)
                            {
                                if (portState.designatedPort < rootPort.designatedPort)
                                {
                                    /*
                                    Of two or more Ports with the highest priority Designated Root parameter, lowest value of
                                    associated Root Path Cost, and highest priority Designated Bridge, has the highest priority Port Identifier
                                    recorded as the Designated Port for the LAN to which the Port is attached
                                     */
                                    // System.out.println("Selecting root port: " + portState.portIdentifier + " instead of: " + rootPort.portIdentifier + " @ Lower priority designated port ( " + portState.designatedPort + " / " + rootPort.designatedPort + " )");
                                    rootPort = portState;
                                } else if (portState.designatedPort == rootPort.designatedPort)
                                {
                                    if (portState.portIdentifier < rootPort.portIdentifier)
                                    {
                                    /*
                                    Of two or more Ports with the highest priority Designated Root parameter, lowest value of
                                    associated Root Path Cost, and highest priority Designated Bridge and Designated Port, has the highest
                                    priority Port Identifier.
                                     */
                                        // System.out.println("Selecting root port: " + portState.portIdentifier + " instead of: " + rootPort.portIdentifier + " @ Lower priority port identifier");
                                        rootPort = portState;
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        synchronized (bridgeState)
        {
            if (rootPort == null)
            {
                System.out.println("[" + bridgeState.device.getName() + "] Bridge: " + bridgeState.bridgeIdentifier + " became root Bridge");
                bridgeState.rootPort = -1;
                bridgeState.designatedRoot = bridgeState.bridgeIdentifier;
                bridgeState.rootPathCost = 0;
            } else
            {
                System.out.println("[" + bridgeState.device.getName() + "] Bridge: " + bridgeState.bridgeIdentifier + " became designated Bridge, Root Port: " + rootPort.portIdentifier);
                bridgeState.designatedRoot = rootPort.designatedRoot;
                bridgeState.rootPort = rootPort.portIdentifier;
                bridgeState.rootPathCost = rootPort.pathCost + rootPort.designatedCost;
            }
        }


        return rootPort;
    }

    /**
     * Method used for Designated port Selection
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.9 - Designated Port Selection
     *
     * @param bridgeState
     */
    public static void designatedPortSelection(StpState bridgeState)
    {
        for (StpSwitchportState portState : bridgeState.portsStates.values())
        {
            if (portState.state == StpPortStates.PORT_DISABLED)
                continue;

            if ((portState.designatedBridge == portState.bridgeState.bridgeIdentifier &&  // a)
                    portState.designatedPort == portState.portIdentifier) ||
                    (portState.designatedRoot != portState.bridgeState.designatedRoot) || // b)
                    (portState.designatedCost > bridgeState.rootPathCost) ||              // c)
                    (portState.designatedCost == bridgeState.rootPathCost &&              // d)
                            bridgeState.bridgeIdentifier.isLessOrEqual(portState.designatedBridge) == true &&
                            bridgeState.bridgeIdentifier != portState.designatedBridge) ||
                    (portState.designatedCost == bridgeState.rootPathCost &&              // e)
                            bridgeState.bridgeIdentifier == portState.designatedBridge &&
                            portState.portIdentifier > portState.designatedPort
                    )
                    )
            {
                /*
                System.out.println("Port: " + portState.portIdentifier + " is becoming designated port");
                System.out.println("Same designated Bridge as Bridge Identifier: " +
                        (portState.designatedBridge == portState.bridgeState.bridgeIdentifier &&
                                portState.designatedPort == portState.portIdentifier));
                System.out.println("Designated root Differs: " +
                        (portState.designatedRoot != portState.bridgeState.designatedRoot));
                System.out.println("Higher path cost: " +
                        (portState.designatedCost > bridgeState.rootPathCost));
                System.out.println("Higher priority bridge: " +
                        (portState.designatedCost == bridgeState.rootPathCost &&              // d)
                                bridgeState.bridgeIdentifier.isLessOrEqual(portState.designatedBridge) == true &&
                                bridgeState.bridgeIdentifier != portState.designatedBridge) +
                        "  Prio: " + bridgeState.bridgeIdentifier.compareTo(portState.designatedBridge) +
                        " My: " + portState.bridgeState.bridgeIdentifier +
                        "  designated: " + portState.designatedBridge);
                System.out.println("Higher priority port: " +

                        (portState.designatedCost == bridgeState.rootPathCost &&              // e)
                                bridgeState.bridgeIdentifier == portState.designatedBridge &&
                                portState.portIdentifier > portState.designatedPort
                        ));
                */
                becomeDesignatedPort(portState, portState.bridgeState);
            }
        }
    }

    /**
     * Method used for updating Designated port values
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.10 -- Becoming Designated Port
     * @param portState
     * @param bridgeState
     */
    public static void becomeDesignatedPort(StpSwitchportState portState, StpState bridgeState)
    {
        synchronized (portState)
        {
            synchronized (bridgeState)
            {
                portState.designatedRoot = bridgeState.designatedRoot;
                portState.designatedCost = bridgeState.rootPathCost;
                portState.designatedBridge = bridgeState.bridgeIdentifier;
                portState.designatedPort = portState.portIdentifier;
            }
        }
    }

    /**
     * Method used for Port state selection
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.11 Port State Selection
     *
     * @param bridgeState
     */
    public static void portStateSelection(StpState bridgeState)
    {
        for (StpSwitchportState state : bridgeState.portsStates.values())
        {
            if (state.state == StpPortStates.PORT_DISABLED)
                continue;
            /*
            System.out.println("[" + bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] Port State Selection:  " +
                                "  State: " + state.state +
                                "  Brdige Root Port: " + bridgeState.rootPathCost +
                                "  Port Designated Bridge: " + state.designatedBridge +
                                "  Bridge Identifier: " + bridgeState.bridgeIdentifier +
                                "  Port Designated Port: " + state.designatedPort);
            */
            if (bridgeState.rootPort == state.portIdentifier)
            {
                //System.out.println("[" + bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] Make forwarding   Root Port");
                state.configurationPending = false;
                state.topologyChangeAck = false;
                makeForwarding(state);
            }
            else if (state.designatedBridge == state.bridgeState.bridgeIdentifier &&
                    state.designatedPort == state.portIdentifier)
            {
                //System.out.println("[" + bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] Make forwarding   Designated Port");
                Timer.activateTimer(state.getTimerPrefix() + "#StpMessageAgeTimer");
                makeForwarding(state);
            }
            else
            {
                //System.out.println("[" + bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] Make Blocking  ");
                state.configurationPending = false;
                state.topologyChangeAck = false;
                makeBlocking(state);
            }

        }
    }

    /**
     * Method used to make port forward packets
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.12 Make Forwarding
     *
     * @param portState
     */
    public static void makeForwarding(StpSwitchportState portState)
    {
        if (portState.state == StpPortStates.PORT_BLOCKING)
        {
            portState.state = StpPortStates.PORT_LISTENING;
            Timer.activateTimer(portState.getTimerPrefix() + "#StpForwardDelayTimer");
        }
    }

    /**
     * Method used for making port blocking communication
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.13 Make Blocking
     * @param portState
     */
    public static void makeBlocking(StpSwitchportState portState)
    {
        if (portState.state != StpPortStates.PORT_DISABLED && portState.state != StpPortStates.PORT_BLOCKING)
        {
            if ((portState.state == StpPortStates.PORT_LEARNING ||
                    portState.state == StpPortStates.PORT_FORWARDING) &&
                    portState.changeDetectionEnabled
                    )
            {
                topologyChangeDetection(portState.bridgeState);
            }
            portState.state = StpPortStates.PORT_BLOCKING;
            Timer.deactivateTimer(portState.getTimerPrefix() + "#StpForwardDelayTimer");
        }
    }

    /**
     * Method used when Spanning tree detect topology change
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.14 Topology change detection
     * @param bridgeState
     */
    public static void topologyChangeDetection(StpState bridgeState)
    {
        if (bridgeState.designatedRoot == bridgeState.bridgeIdentifier)
        {
            bridgeState.topologyChange = true;
            Timer.activateTimer(bridgeState.device.getName() + "#StpTopologyChangeTimer");
        }
        else
        {
            transmitTopologyChangeNotificationBPDU(bridgeState);
            Timer.activateTimer(bridgeState.device.getName() + "#StpTopologyChangeNotificationTimer");
        }
        bridgeState.topologyChangeDetected = true;
    }

    /**
     * Method used when Spanning tree acknowledges topology change
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.15 Topology change acknowledged
     * @param bridgeState
     */
    public static void topologyChangeAcknowledged(StpState bridgeState)
    {
        bridgeState.topologyChangeDetected = false;
        Timer.deactivateTimer(bridgeState.device.getName() + "#StpTopologyChangeNotificationTimer");
    }

    /**
     * Method used to acknowledge topology change
     *
     * Implementation based on IEEE 802.1d - 1998 -  8.6.16 Acknowledge topology change
     *
     * @param bridgeState
     * @param portState
     */
    public static void acknowledgeTopologyChange(StpState bridgeState, StpSwitchportState portState)
    {
        SwitchNetworkModule module = (SwitchNetworkModule) bridgeState.device.getNetworkModule();

        portState.topologyChangeAck = true;
        STP.transmitConfigurationBPDU(bridgeState, portState);
    }
}
