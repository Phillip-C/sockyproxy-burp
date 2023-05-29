package sockyProxy;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import burp.api.montoya.MontoyaApi;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.requests.*;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class GUI extends JPanel implements ActionListener, AncestorListener {
	private static final long serialVersionUID = 1L;
	public JTextField tbPort;
	public JTextField tbWebsocketURL;
	public JTextArea tbDebug;
	public MontoyaApi api;
	public sockyTableModel stm;
	public boolean ProxyServerStarted;
	/**
	 * Create the panel.
	 */
	public sockyTableModel getTableModel() {
		return stm;
	}
	
	public GUI(MontoyaApi api) {
		this.api = api;
		stm = new sockyTableModel();
		ProxyServerStarted = false;
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel pnlInventory = new JPanel();
		tabbedPane.addTab("Inventory", null, pnlInventory, null);
		pnlInventory.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlProxyServer = new JPanel();
		tabbedPane.addTab("Proxy Server", null, pnlProxyServer, null);
		pnlProxyServer.setLayout(null);
		
		JButton btnStartProxy = new JButton("Start Websocket Proxy");
		btnStartProxy.setBounds(20, 140, 415, 29);
		pnlProxyServer.add(btnStartProxy);
		
		JButton btnStopProxy = new JButton("Stop Websocket Proxy");
		btnStopProxy.setBounds(20, 180, 415, 29);
		pnlProxyServer.add(btnStopProxy);
		
		JPanel panel = new JPanel();
		panel.setForeground(UIManager.getColor("Button.foreground"));
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(20, 11, 415, 118);
		pnlProxyServer.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("sockyProxy HTTP Server Port");
		lblNewLabel.setBounds(10, 26, 222, 16);
		panel.add(lblNewLabel);
		
		tbPort = new JTextField();
		tbPort.setToolTipText("This is the local port used for the HTTP-to-Websockets proxy.");
		tbPort.setHorizontalAlignment(SwingConstants.CENTER);
		tbPort.setBounds(229, 21, 67, 26);
		panel.add(tbPort);
		tbPort.setText("9060");
		tbPort.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Target Websocket Endpoint");
		lblNewLabel_1.setBounds(10, 43, 212, 42);
		panel.add(lblNewLabel_1);
		
		tbWebsocketURL = new JTextField();
		tbWebsocketURL.setToolTipText("This is the target websocket server that you want to test. wss://example.com");
		tbWebsocketURL.setBounds(10, 81, 395, 26);
		panel.add(tbWebsocketURL);
		tbWebsocketURL.setColumns(10);
		btnStartProxy.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String port, socket;
        		port = tbPort.getText();
        		socket = tbWebsocketURL.getText();
        		if (!port.trim().isBlank() && !socket.trim().isBlank()) {
        			Utils.LaunchProxy(api, tbPort.getText(), tbWebsocketURL.getText());
        			ProxyServerStarted = true;
        			btnStopProxy.setEnabled(true);
        			btnStartProxy.setEnabled(false);
        			tbPort.setEnabled(false);
        			tbWebsocketURL.setEnabled(false);        			
        		} else {
        			Utils.infoBox("You must enter the Proxy Port Number and the Target Websocket URL on the Proxy Server tab.", "Enter required details");
        		}
        	}
        });
        btnStopProxy.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		if (Utils.ProcessLauncher.process != null) {
        			api.logging().logToOutput("[+] Websocket Proxy Server Stopped");
        			Utils.ProcessLauncher.process.destroy();
        			ProxyServerStarted = false;
        			btnStopProxy.setEnabled(false);
        			btnStartProxy.setEnabled(true);
        			tbPort.setEnabled(true);
        			tbWebsocketURL.setEnabled(true);
        			
        		}
        	}
        });
		
		JTable table = new JTable(stm)
        {
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
            {
            	wSocketMessage responseReceived = stm.get(rowIndex);
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        
        JPopupMenu ctxMenu = new JPopupMenu("Edit");
        JMenuItem item = new JMenuItem("Send to Intruder");
	    item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (ProxyServerStarted) {
	        		String val = (String) table.getModel().getValueAt(table.getSelectedRow(), 0);
	        		String req = "POST / HTTP/1.1\r\nHost: 127.0.0.1:"+tbPort.getText()+"\r\nAccept: application/json\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.127 Safari/537.36\r\nContent-Length: 0\r\n\r\n"+val+"\r\n\r\n";  
	        		HttpService service = HttpService.httpService("127.0.0.1", Integer.parseInt(tbPort.getText()), false);
	        		HttpRequest httpreq = HttpRequest.httpRequest(service,  req.toString());
	        		api.intruder().sendToIntruder(httpreq);
        		} else {
        			Utils.infoBox("Websocket proxy server is not running. Please start the proxy server.", "Start websocket proxy server");
        		}
        	}
        });
        ctxMenu.add(item);
        table.setInheritsPopupMenu(true);
        table.setComponentPopupMenu(ctxMenu);
		table.setBounds(25, 25, 472, 354);
		table.setFont(new Font("Cascadia Code", Font.PLAIN, 12));		
        pnlInventory.add(table);
        
        JPanel pnlHelp = new JPanel();
        tabbedPane.addTab("Help", null, pnlHelp, null);
	}
	
	public void debugMessage(String message) {
		api.logging().logToOutput("[-] "+message);
	}
	
	public class sockyTableModel extends AbstractTableModel {
		 private static final long serialVersionUID = 1L;
		private final List<wSocketMessage> log;
		 private final List<String> payloads;
		    public sockyTableModel()
		    {
		        this.log = new ArrayList<>();
		        this.payloads = new ArrayList<>();
		    }

		    @Override
		    public synchronized int getRowCount()
		    {
		        return log.size();
		    }

		    @Override
		    public int getColumnCount()
		    {
		        return 2;
		    }

		    @Override
		    public String getColumnName(int column)
		    {
		        return switch (column)
		                {
		                    case 0 -> "Websocket Payload";
		                    case 1 -> "Timestamp";
		                    default -> "";
		                };
		    }

		    @Override
		    public synchronized Object getValueAt(int rowIndex, int columnIndex)
		    {
		        wSocketMessage response = log.get(rowIndex);
		        return switch (columnIndex)
		                {
		                    case 0 -> response.getMessage().payload();
		                    case 1 -> response.getTimestamp();
		                    default -> "";
		                };
		    }

		    public synchronized void add(wSocketMessage responseReceived)
		    {
		        int index = log.size();
		        if (payloads.indexOf(responseReceived.getMessage().payload()) < 0) {
		        	log.add(responseReceived);
		        	payloads.add(responseReceived.getMessage().payload());
		        	fireTableRowsInserted(index, index);	        	
		        }
		    }

		    public synchronized wSocketMessage get(int rowIndex)
		    {
		        return log.get(rowIndex);
		    }
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		api.logging().logToOutput("Menu Click: " + e.getActionCommand() + "\n");
	}
	
	private JMenuItem makeMenuItem(String label) {
	    JMenuItem item = new JMenuItem(label);
	    item.addActionListener(this);
	    return item;
	  }

	@Override
	public void ancestorAdded(AncestorEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
		// TODO Auto-generated method stub
		
	}
}
