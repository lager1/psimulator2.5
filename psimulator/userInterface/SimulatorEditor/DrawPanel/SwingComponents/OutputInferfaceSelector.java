package psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import psimulator.dataLayer.DataLayerFacade;

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
    
    public OutputInferfaceSelector(DataLayerFacade dataLayer){
        this.dataLayer = dataLayer;

        // TODO - jeste napozicovat v ramci okna, kdyz bude cas
        
        realPcPanel = new JPanel();
        realPcPanel.setBorder(BorderFactory.createTitledBorder(dataLayer.getString("REAL_PC")));
        
        if(getInterfaces() != false) {
            createComboBox();
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
        List <String> devices = new ArrayList<String>();

        for (PcapIf device : alldevs) {
            devices.add(device.getName());
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
                //System.out.println(str);
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
        return selectedInterface;
    }
}
