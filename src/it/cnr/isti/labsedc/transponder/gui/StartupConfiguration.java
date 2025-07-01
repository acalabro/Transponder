package it.cnr.isti.labsedc.transponder.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Window.Type;

public class StartupConfiguration {

	private JFrame frmTransponderLauncher;
	private JTextField loradevice;
	private JTextField wifidevice;
	private JLabel lblGpsDevice;
	private JTextField gpsdevice;
	private JLabel lblMobileDevice;
	private JTextField mobiledevice;
	private JLabel lblPortSpeed_1;
	private JTextField smsrecipient;
	private JLabel lblPortSpeed;
	private JTextField homepath;
	private JLabel lblNewLabel_8;
	private JTextField pincode;
	private JLabel lblPortSpeed_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartupConfiguration window = new StartupConfiguration();
					window.frmTransponderLauncher.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public StartupConfiguration() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTransponderLauncher = new JFrame();
		frmTransponderLauncher.setResizable(false);
		frmTransponderLauncher.setType(Type.UTILITY);
		frmTransponderLauncher.setTitle("Transponder - Launcher");
		frmTransponderLauncher.setBounds(100, 100, 600, 340);
		frmTransponderLauncher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTransponderLauncher.getContentPane().setLayout(null);
		
	
		
		JLabel lblNewLabel = new JLabel("LoRa Device");
		lblNewLabel.setBounds(63, 33, 72, 17);
		frmTransponderLauncher.getContentPane().add(lblNewLabel);
		
		loradevice = new JTextField();
		loradevice.setText("/dev/ttyUSB0");
		loradevice.setBounds(153, 31, 114, 21);
		frmTransponderLauncher.getContentPane().add(loradevice);
		loradevice.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Wi-Fi Device");
		lblNewLabel_1.setBounds(61, 80, 74, 17);
		frmTransponderLauncher.getContentPane().add(lblNewLabel_1);
		
		wifidevice = new JTextField();
		wifidevice.setText("wlan0");
		wifidevice.setColumns(10);
		wifidevice.setBounds(153, 78, 114, 21);
		frmTransponderLauncher.getContentPane().add(wifidevice);
		
		lblGpsDevice = new JLabel("GPS Device");
		lblGpsDevice.setBounds(296, 80, 72, 17);
		frmTransponderLauncher.getContentPane().add(lblGpsDevice);
		
		gpsdevice = new JTextField();
		gpsdevice.setText("/dev/ttyACM0");
		gpsdevice.setColumns(10);
		gpsdevice.setBounds(386, 78, 114, 21);
		frmTransponderLauncher.getContentPane().add(gpsdevice);
		
		lblMobileDevice = new JLabel("Mobile Device");
		lblMobileDevice.setBounds(51, 174, 84, 17);
		frmTransponderLauncher.getContentPane().add(lblMobileDevice);
		
		mobiledevice = new JTextField();
		mobiledevice.setText("/dev/ttyS0");
		mobiledevice.setColumns(10);
		mobiledevice.setBounds(153, 172, 114, 21);
		frmTransponderLauncher.getContentPane().add(mobiledevice);
		
		lblPortSpeed_1 = new JLabel("SMS Recipient n°");
		lblPortSpeed_1.setBounds(34, 221, 101, 17);
		frmTransponderLauncher.getContentPane().add(lblPortSpeed_1);
		
		smsrecipient = new JTextField();
		smsrecipient.setText("+39347347347");
		smsrecipient.setColumns(10);
		smsrecipient.setBounds(153, 219, 114, 21);
		frmTransponderLauncher.getContentPane().add(smsrecipient);
		
		lblPortSpeed = new JLabel("Home Path");
		lblPortSpeed.setBounds(63, 127, 72, 17);
		frmTransponderLauncher.getContentPane().add(lblPortSpeed);
		
		homepath = new JTextField();
		homepath.setText("/home/acalabro/Desktop/");
		homepath.setColumns(10);
		homepath.setBounds(153, 125, 215, 21);
		frmTransponderLauncher.getContentPane().add(homepath);
		
		lblNewLabel_8 = new JLabel("Port Speed");
		lblNewLabel_8.setBounds(303, 33, 65, 17);
		frmTransponderLauncher.getContentPane().add(lblNewLabel_8);
		
		JComboBox<String> loraportspeed = new JComboBox<String>();
		loraportspeed.setModel(new DefaultComboBoxModel<String>(new String[] {
				"110", "300", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200"}));
		loraportspeed.setSelectedIndex(9);
		loraportspeed.setBounds(386, 28, 114, 26);
		frmTransponderLauncher.getContentPane().add(loraportspeed);
		
		JComboBox<String> mobiledevicespeed = new JComboBox<String>();
		mobiledevicespeed.setModel(new DefaultComboBoxModel<String>(new String[] {
				"110", "300", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200"}));
		mobiledevicespeed.setSelectedIndex(9);
		mobiledevicespeed.setBounds(386, 169, 114, 26);
		frmTransponderLauncher.getContentPane().add(mobiledevicespeed);
		
		JCheckBox pincodecheckbox = new JCheckBox("PIN code required");
		pincodecheckbox.setVerticalAlignment(SwingConstants.BOTTOM);
		pincodecheckbox.setHorizontalAlignment(SwingConstants.LEFT);
		pincodecheckbox.setBounds(386, 217, 134, 25);
		frmTransponderLauncher.getContentPane().add(pincodecheckbox);
		
		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setBounds(386, 122, 114, 27);
		frmTransponderLauncher.getContentPane().add(btnNewButton);
		
		JLabel lblNewLabel_8_2 = new JLabel("Port Speed");
		lblNewLabel_8_2.setBounds(303, 174, 65, 17);
		frmTransponderLauncher.getContentPane().add(lblNewLabel_8_2);
		
		pincode = new JTextField();
		pincode.setText("1234");
		pincode.setColumns(10);
		pincode.setBounds(153, 266, 114, 21);
		frmTransponderLauncher.getContentPane().add(pincode);
		
		lblPortSpeed_2 = new JLabel("PIN Code");
		lblPortSpeed_2.setBounds(79, 268, 56, 17);
		frmTransponderLauncher.getContentPane().add(lblPortSpeed_2);
		
		JButton btnNewButton_1 = new JButton("Start");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isOK()) {
//				Transponder launcher = new Transponder(
//						loradevice.getText(), 
//						Integer.parseInt(loraportspeed.getSelectedItem().toString()), 
//						wifidevice.getText(), 
//						homepath.getText(), 
//						gpsdevice.getText(), 
//						mobiledevice.getText(), 
//						Integer.parseInt(mobiledevicespeed.getSelectedItem().toString()),
//						smsrecipient.getText(),
//						Integer.parseInt(pincode.getText()),
//						pincodecheckbox.isSelected()
//						);
//				launcher.start();
			} else {
//				JOptionPane.showMessageDialog(frmTransponderLauncher,"Fill all the required fields before start.");
				JOptionPane.showConfirmDialog(frmTransponderLauncher,
						"Fill all the required fields before start.", "Warning",
						JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE);
			}
				}
		});
		btnNewButton_1.setBounds(386, 263, 114, 27);
		frmTransponderLauncher.getContentPane().add(btnNewButton_1);
		
		JLabel lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setIcon(new ImageIcon(StartupConfiguration.class.getResource("/logo.png")));
		lblNewLabel_2.setBounds(296, 214, 64, 45);
		frmTransponderLauncher.getContentPane().add(lblNewLabel_2);
	}

	protected boolean isOK() {
		if (loradevice.getText().length() != 0 ||
			wifidevice.getText().length() != 0 ||
			homepath.getText().length() != 0 ||
			gpsdevice.getText().length() != 0 || 
			mobiledevice.getText().length() != 0 || 
			smsrecipient.getText().length() != 0) {
		return true;
		} 
		else {		
			return false;
		}
	}
}
