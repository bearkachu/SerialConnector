package com.smk.serialcomm;

import java.awt.EventQueue;

import java.util.concurrent.CyclicBarrier;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Gui {

	private JFrame frame;
	private JButton btnOpen = new JButton("OPEN");
	private JButton btnClose = new JButton("CLOSE");
	private JComboBox<String> comboBoxPortList = new JComboBox<String>();
	private int defaultBaudrate, dataBits, stopBits, parity;
	private CommAdapter comm = new CommAdapter();
	private final JMenuBar menuBar = new JMenuBar();
	
	static SerialPort serialPort;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {		

		
		//Create thread for reciver
		//Runnable r = new Receiver(this.comm);
		//Thread t = new Thread(r);
		//t.start();
		
		//Initialize GUI components
		initialize(); 
		//Add avaailbe ports into combobox to be selected
		for(int i = 0; i < comm.getPortLists().length; i++)
			comboBoxPortList.addItem(comm.getPortLists()[i]);		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		comboBoxPortList.setEditable(true);
		comboBoxPortList.setMaximumRowCount(99);

		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(comboBoxPortList, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(btnOpen, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnClose, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap(273, Short.MAX_VALUE))
		);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				comm.closePort();
			}
		});
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOpen)
						.addComponent(comboBoxPortList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnClose)
					.addContainerGap(211, Short.MAX_VALUE))
		);
		btnOpen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {	
				byte[] buffer = new byte[] {0x02, 0x4a, 0x01, 0x01, 0x4d, 0x03};
				
				defaultBaudrate = CommAdapter.COMM_BAUDRATE_57600;
				dataBits        = CommAdapter.COMM_DATA_BITS_8;
				stopBits        = CommAdapter.COMM_STOP_BIT_1;
				parity          = CommAdapter.COMM_NONE_PARITY;			
				
				//Open with serial communication settings
				comm.OpenComm((String)comboBoxPortList.getSelectedItem(), defaultBaudrate, dataBits, stopBits, parity);
				
				//mask for serialPort and add SerialPortEventListner
				try {
					int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;					
					comm.serialPort.setEventsMask(mask);
					comm.serialPort.addEventListener(new SerialPortReader());
					comm.serialPort.writeBytes(buffer);
					for(int i = 0; i < buffer.length; i++)
						System.out.println(buffer[i]);
				} catch (SerialPortException e) {
					System.out.println(e);
				}
			}
		});
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		frame.getContentPane().setLayout(groupLayout);
		
		frame.setJMenuBar(menuBar);
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	static class SerialPortReader implements SerialPortEventListener {
		private CommAdapter comm;
		
		//Constructor
		public SerialPortReader () {
			
		}
		
		public SerialPortReader (CommAdapter comm) {
			this.comm = comm;
		}
		
		public void serialEvent(SerialPortEvent event) {
			//If data is available
			if(event.isRXCHAR()) {
				//Check bytes count in the input buffer
				if(event.getEventValue() == 10) {
				//Read data if 10 bytes available
					try {
						byte buffer[] = this.comm.serialPort.readBytes(10);
						for(int i = 0; i < buffer.length; i++)
							System.out.print(buffer[i] + "  ");
					} catch (SerialPortException e) {
						System.out.println(e);
					} finally {
						System.out.println();
					}
				}
					
				
			}
		}
	}	
}

