package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import device.Device;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import physicalModule.Switchport;
import psimulator.dataLayer.DataLayerFacade;
import psimulator2.Psimulator;

/**
 *
 * @author lager1
 */
public class OutputInferfaceSelector {
    private JComboBox combo;
    private JTextField txt;
    private JPanel realPcPanel;
    private List<PcapIf> alldevs;
    private DataLayerFacade dataLayer;
    private String selectedInterface = "";
    private String os;
    
    public OutputInferfaceSelector(DataLayerFacade dataLayer){
        this.dataLayer = dataLayer;

        // TODO - jeste napozicovat v ramci okna, kdyz bude cas
        
        realPcPanel = new JPanel();
        realPcPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("REAL_PC")));
        
        if(getInterfaces() != false) {
            createComboBox();
            //setDeviceRealInterface();
        }
        else {
            setErrorLabel();
        }
    }

    JPanel getrealPcPanel() {
        return realPcPanel;
    }

    private boolean getInterfaces() {
        alldevs = new ArrayList<PcapIf>();              // Will be filled with NICs  
        StringBuilder errbuf = new StringBuilder();     // For any error msgs  

        int r = Pcap.findAllDevs(alldevs, errbuf);  
        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
            return false;  
        }  

        return true;
    }

    private void createComboBox() {
        os = System.getProperty("os.name");

        List <String> devices = new ArrayList<String>();
        List <String> winDevNames = new ArrayList<String>();
        
        if(os.startsWith("Win")) {
            Enumeration<NetworkInterface> nets;
            try {
                nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
//                    if(!netint.isVirtual() && netint.isLoopback())
                    if(!netint.isVirtual() && !netint.isLoopback())
                        winDevNames.add(netint.getDisplayName());
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }

        
        for (PcapIf device : alldevs) {
            if(os.startsWith("Win")) {
                for(String s : winDevNames) {
                    if(device.getDescription().equals(s))
                        devices.add(s);
                }
            }
            else {
                devices.add(device.getName());
            }
        }

        JLabel realPcLabel = new JLabel(dataLayer.getString("REAL_PC_INTERFACE"));

        realPcPanel.add(realPcLabel);

        combo = new JComboBox(devices.toArray());
        combo.setSelectedItem(dataLayer.getRealInterface());
    
        realPcPanel.add(combo);
        
        combo.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent ie){
                String str = (String)combo.getSelectedItem();
                selectedInterface = str;
            }
        });
    }

    private void setErrorLabel() {
        JLabel realPcLabel = new JLabel();
        realPcLabel.setText("<html>"+ dataLayer.getString("FAILED_TO_GET_INTERFACES") +"</html>");  // html for text wrapping
        
        realPcPanel.add(realPcLabel);
    }

    String getSelectedInterface() {
        if(os.startsWith("Win")) {
            for (PcapIf device : alldevs) {
                if(device.getDescription().equals(selectedInterface))
                    return device.getName();
            }
        }

        return selectedInterface;
    }

    /*
    public void setDeviceRealInterface() {
        System.out.println("set device .. ");
        for (Device dev : Psimulator.getPsimulator().devices) {
            for (Switchport swport : dev.physicalModule.getSwitchports().values()) {
                if (swport.isReal()) {
                    System.out.println("nastavuji realny inteface zarizeni");
                    System.out.println(dev.getName());
                    dev.setRealInterface(selectedInterface);
                }
            }
        }
    }
    */
}
