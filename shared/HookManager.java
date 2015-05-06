package shared;

import dataStructures.packets.L2.EthernetPacket;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.filters.*;
import physicalModule.Switchport;

import java.util.*;

/**
 * Class for calling methods at specific events
 * e.g  After all network modules were loaded, after simulation started, After Packet is received
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class HookManager {

    // Enumarion of existing Hoook types
    public enum HookType {
        /* GLOBAL HOOKS */
        BEFORE_SETUP, // Not Yet Implemented - NYI

        // Hook is called right before Network model is loaded
        BEFORE_MODEL_LOAD,

        // Hook is called right after Network model is loaded
        AFTER_MODEL_LOAD,
        // Hook is called after Network model is loaded and everything is prepared for simulation
        AFTER_SETUP,

        /* DEVICE HOOKS */
        // Hook is called after device is loaded from configuration file
        DEVICE_AFTER_LOAD,


        /* L1 FILTERING */
        PHYSICAL_INBOUND, // Not Yet Implemented - NYI
        PHYSICAL_OUTBOUND, // Not Yet Implemented - NYI

        /* L2 FILTERING */

        // Hook is called before packet source mac address is added to Switch table
        DATALINK_PRE_LEARNING,

        // Hook is called after packet source mac address is added to Switch table
        DATALINK_POST_LEARNING,

        // Hook is called before packet is forwarded out of port
        DATALINK_PRE_FORWARDING,

        // Hook is called after packet is forwarded out of port
        DATALINK_POST_FORWARDING,

    }

    private static HashMap<Object, EnumMap<HookType, List<IFilter>>> object_hooks = new HashMap<Object, EnumMap<HookType, List<IFilter>>>();

    /**
     * Method for registering hooks
     *
     * @param callingObject Object which requested hook registration
     * @param hookType  Hook type from enumeration
     * @param filter Object called on specified event
     */
    public static void registerHook(Object callingObject, HookType hookType, IFilter filter) {
        Logger.log(Logger.DEBUG, LoggingCategory.HOOK_MANAGER, "Registering Hook: "  + hookType + " From Object: " + callingObject.toString());
        EnumMap<HookType, List<IFilter>> hooks = object_hooks.get(callingObject);
        if (hooks == null)
            object_hooks.put(callingObject, hooks = new EnumMap<HookType, List<IFilter>>(HookType.class));
        List<IFilter> filters = hooks.get(hookType);
        if (filters == null)
            hooks.put(hookType, filters = new LinkedList<IFilter>());
        filters.add(filter);
    }


    /**
     * Method for calling Global Hooks
     *
     * @param hookType Global Hook Type
     * @param device Device which is calling Hook
     * @return Miscellaneous boolean used for different purposes
     */
    public static void callGlobalHook(HookType hookType, Device device) {
        EnumMap<HookType, List<IFilter>> hooks = object_hooks.get("Global");

        Logger.log(Logger.DEBUG, LoggingCategory.HOOK_MANAGER, "Called Hook: "  + hookType);
        if (hooks == null)
            return;

        switch (hookType) {
            case BEFORE_SETUP:
            case AFTER_SETUP:
            {
                List<IFilter> filters = hooks.get(hookType);
                if (filters == null)
                    return;
                for (IFilter f : filters) {
                    GlobalFilter filter = (GlobalFilter) f;
                    if (filter == null) {
                        Logger.log(Logger.WARNING, LoggingCategory.HOOK_MANAGER, "Invalid Filter class: "  + f.getClass().getName());
                        filters.remove(f);
                        continue;
                    }
                    switch (hookType) {
                        case BEFORE_SETUP:
                            filter.beforeSetup();
                            return;

                        case AFTER_SETUP:
                            filter.afterSetup();
                            return;
                    }
                }
                break;
            }
        }
        return;
    }

    /**
     * Method for calling Device Hooks
     *
     * @param object Device which is calling Hook
     * @param hookType Device Hook Type
     * @return Miscellaneous boolean used for different purposes
     */
    public static Boolean callDeviceHook(Device object, HookType hookType)
    {
        EnumMap<HookType, List<IFilter>> hooks = object_hooks.get(object);

        Logger.log(Logger.DEBUG, LoggingCategory.HOOK_MANAGER, "Called Hook: "  + hookType + " by device: " + object);
        if (hooks == null)
            return true;

        switch (hookType) {
            case DEVICE_AFTER_LOAD:
            {
                List<IFilter> filters = hooks.get(hookType);
                if (filters == null)
                    return true;
                for (IFilter f : filters) {
                    DeviceFilter filter = (DeviceFilter) f;
                    if (filter == null) {
                        Logger.log(Logger.WARNING, LoggingCategory.HOOK_MANAGER, "Invalid Filter class: "  + f.getClass().getName());
                        filters.remove(f);
                        continue;
                    }
                    filter.afterLoad(object);
                }
                break;
            }
        }
        return true;
    }

    /**
     * Method for calling Networking Hooks
     *
     * @param object Object from which is Hook called
     * @param hookType Networking Hook Type
     * @param packet Ethernet Packet which caused hook call
     * @param intfc Interface which is was to send/recieve packet
     * @param port Port which was used to send/recieve packet
     * @param switchportNumber Number of that port
     * @param object Device which is calling Hook
     * @return Miscellaneous boolean used for different purposes
     */
    public static Boolean callNetworkingHook(Object object, HookType hookType, EthernetPacket packet, EthernetInterface intfc, Switchport port, Integer switchportNumber, Device device) {
        EnumMap<HookType, List<IFilter>> hooks = object_hooks.get(object);

        Logger.log(Logger.DEBUG, LoggingCategory.HOOK_MANAGER, "Called Hook: "  + hookType + " From Object: " + object.toString());
        if (hooks == null)
            return true;

        switch (hookType) {
            case PHYSICAL_INBOUND:
            case PHYSICAL_OUTBOUND: {
                List<IFilter> filters = hooks.get(hookType);
                if (filters == null)
                    return true;
                for (IFilter f : filters) {
                    AbstractFilter filter = (AbstractFilter) f;
                    if (filter == null) {
                        Logger.log(Logger.WARNING, LoggingCategory.HOOK_MANAGER, "Invalid Filter class: "  + f.getClass().getName());
                        filters.remove(f);
                        continue;
                    }
                    switch (hookType) {
                        case PHYSICAL_INBOUND:
                            return filter.filterInbound(packet, intfc, port, switchportNumber, device);

                        case PHYSICAL_OUTBOUND:
                            return filter.filterOutbound(packet, intfc, port, switchportNumber, device);
                    }
                }
                break;
            }

            case DATALINK_PRE_LEARNING:
            case DATALINK_POST_LEARNING:
            case DATALINK_PRE_FORWARDING:
            case DATALINK_POST_FORWARDING: {
                List<IFilter> filters = hooks.get(hookType);
                if (filters == null)
                    return true;
                for (IFilter f : filters) {
                    AbstractDataLinkFilter filter = (AbstractDataLinkFilter) f;
                    if (filter == null) {
                        Logger.log(Logger.INFO, LoggingCategory.HOOK_MANAGER, "Invalid Filter class: "  + f.getClass().getName());
                        filters.remove(f);
                        continue;
                    }
                    switch (hookType) {
                        case DATALINK_PRE_LEARNING:
                            return filter.preLearning(packet, intfc, port, switchportNumber, device);

                        case DATALINK_POST_LEARNING:
                            return filter.postLearning(packet, intfc, port, switchportNumber, device);

                        case DATALINK_PRE_FORWARDING:
                            return filter.preForwarding(packet, intfc, port, switchportNumber, device);

                        case DATALINK_POST_FORWARDING:
                            return filter.postForwarding(packet, intfc, port, switchportNumber, device);
                    }
                }
                break;
            }
        }
        return true;
    }
}
