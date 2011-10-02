package spider.testing.executables.devicegui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import spider.prototype.services.Controller;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.communication.addresses.TCPAddress;
import spider.prototype.services.communication.mappinglayer.TCPMapper;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.modules.IDataRequester;
import spider.prototype.services.modules.IServiceObserver;
import spider.prototype.services.modules.connection.XMLConnectionModule;
import spider.prototype.services.modules.position.FakeGPSModule;
import spider.prototype.services.modules.position.GPSModule;
import spider.prototype.services.modules.position.LatLon;
import spider.prototype.services.modules.rfid.FakeRFIDModule;
import spider.prototype.services.modules.temperature.FakeTemperatureSensor;
import spider.prototype.services.modules.temperature.MinMax;
import spider.prototype.services.modules.temperature.TempModule;
import spider.prototype.services.yellowpage.ServiceDescription;
import spider.prototype.services.yellowpage.YellowPage;
import spider.prototype.utils.BinaryTransformer;

public class Tester extends JFrame implements ILogger, IDataRequester,
		IServiceObserver {

	private static final long serialVersionUID = 1L;

	// gui elements
	final Tester t = this;
	DefaultListModel serviceModel = new DefaultListModel();
	JList listService = new JList(serviceModel);
	DefaultListModel ypsm = new DefaultListModel();
	JList yps = new JList(ypsm);
	JComboBox comboAddService = new JComboBox();
	JTextField txtAddServiceId = new JTextField();
	JTextArea txtLogger = new JTextArea();
	JButton butRun = new JButton();
	JButton butAddService = new JButton();
	JTextField txtMyAdd = new JTextField();
	JTextField txtKnownAdd = new JTextField();
	JSlider slideTemp = new JSlider(JSlider.HORIZONTAL, -20, 50, 0);

	// list of services
	List<AbstractModule> modules = new ArrayList<AbstractModule>();
	XMLConnectionModule mod = new XMLConnectionModule(234);

	// Controller
	TCPMapper mapper = new TCPMapper(6000);

	// values
	double temp = 10;
	
	public static void main(String args[]) {
		new Tester();
	}

	Tester() {
		Controller.setInstance(new Controller());
		Controller.getInstance().getCommunicationService().addMapper(mapper);
		initComponents();

	}

	private void initComponents() {

		setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel lblList = new JLabel("Init Service List");
		lblList.setBounds(5, 5, 100, 10);
		add(lblList);

		listService.setBounds(5, 20, 100, 100);
		add(listService);

		JLabel lblyp = new JLabel("Local Yellow Page");
		lblyp.setBounds(125, 5, 100, 10);
		add(lblyp);
		yps.setBounds(125, 20, 100, 100);
		add(yps);

		JLabel lblsettings = new JLabel("Settings");
		lblsettings.setBounds(245, 5, 100, 10);
		add(lblsettings);
		comboAddService.setBounds(245, 20, 100, 20);
		for (ServiceDescription sd : ServiceDescription.values()) {
			comboAddService.addItem(sd.toString());
		}
		add(comboAddService);

		JLabel lblservid = new JLabel("Service ID");
		lblservid.setBounds(245, 45, 100, 10);
		add(lblservid);
		txtAddServiceId.setBounds(245, 60, 100, 20);
		txtAddServiceId.setText(new Integer(new Random().nextInt(255))
				.toString());
		add(txtAddServiceId);

		butAddService.setBounds(5, 125, 100, 20);
		butAddService.setText("Add Service");
		butAddService.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				int serviceId = Integer.valueOf(txtAddServiceId.getText());
				serviceModel.addElement(comboAddService.getSelectedItem()
						.toString());

				if (comboAddService.getSelectedItem().toString() == "GPSPosition") {
					modules.add(new FakeGPSModule(serviceId));
				} else if (comboAddService.getSelectedItem().toString() == "RFIDNumber") {
					modules.add(new FakeRFIDModule(serviceId));
				} else if (comboAddService.getSelectedItem().toString() == "Temperature") {
					final int servid = serviceId;
					modules.add(new FakeTemperatureSensor(servid));/* {

						@Override
						public byte[] getValue() {

							// TODO: change this back sometime
							//return BinaryTransformer.toByta(slideTemp
							//		.getValue());
							temp += (new Random().nextDouble() * 4 - 2);
							return BinaryTransformer.toByta(temp);
						}
					});*/

					txtAddServiceId.setText(new Integer(new Random()
							.nextInt(255)).toString());
				}
			}
		});

		add(butAddService);

		butRun.setBounds(5, 150, 100, 20);
		butRun.setText("Run");
		butRun.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Controller.getInstance().initialize(modules, t);
				t.butAddService.setEnabled(false);
				t.butRun.setEnabled(false);
			}
		});
		add(butRun);

		JButton butRefYPS = new JButton();
		butRefYPS.setBounds(125, 125, 100, 20);
		butRefYPS.setText("Refresh YPS");

		butRefYPS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ypsm.removeAllElements();

				for (YellowPage yp : Controller.getInstance().getYellowPage(
						ServiceDescription.valueOf(comboAddService
								.getSelectedItem().toString()))) {

					ypsm.addElement(yp.getAddress().getAddressAsString());
				}

			}
		});
		add(butRefYPS);

		JLabel lblactions = new JLabel("Actions");
		lblactions.setBounds(365, 5, 100, 10);
		add(lblactions);

		JButton butBroadcast = new JButton();
		butBroadcast.setBounds(365, 20, 100, 20);
		butBroadcast.setText("BroadcastRequest");
		butBroadcast.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO hops & max hops
				Controller.getInstance().broadcastServiceRequest(
						ServiceDescription.valueOf(comboAddService
								.getSelectedItem().toString()), 0, 3);

			}
		});
		add(butBroadcast);

		JButton butAskValue = new JButton();
		butAskValue.setBounds(365, 45, 100, 20);
		butAskValue.setText("DataReq");
		butAskValue.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				Controller.getInstance().sendDataRequest(
						t,
						Controller.getInstance().getYellowPage(
								ServiceDescription.valueOf(comboAddService
										.getSelectedItem().toString()),
								yps.getSelectedIndex()).getAddress(),
						ServiceDescription.valueOf(comboAddService
								.getSelectedItem().toString()),
						Controller.getInstance().getYellowPage(
								ServiceDescription.valueOf(comboAddService
										.getSelectedItem().toString()),
								yps.getSelectedIndex()).getServiceId());

			}
		});
		add(butAskValue);

		JButton butobserve = new JButton();
		butobserve.setBounds(365, 70, 100, 20);
		butobserve.setText("Observe");
		butobserve.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				byte[] cond = TempModule.mmToByta(new MinMax(-10, 30));

				Controller.getInstance().sendServiceObservationRequest(
						t,
						Controller.getInstance().getYellowPage(
								ServiceDescription.valueOf(comboAddService
										.getSelectedItem().toString()),
								yps.getSelectedIndex()).getAddress(),
						ServiceDescription.valueOf(comboAddService
								.getSelectedItem().toString()),
						Controller.getInstance().getYellowPage(
								ServiceDescription.valueOf(comboAddService
										.getSelectedItem().toString()),
								yps.getSelectedIndex()).getServiceId(), 5000,
						cond);

			}
		});
		add(butobserve);

		JButton butconn = new JButton();
		butconn.setBounds(365, 95, 100, 20);
		butconn.setText("XML Con");
		butconn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				mod.start();
			}
		});
		add(butconn);

		JLabel lblmyadd = new JLabel("Own Address");
		lblmyadd.setBounds(485, 5, 100, 10);
		add(lblmyadd);
		txtMyAdd.setBounds(485, 20, 100, 20);
		txtMyAdd.setText(mapper.getAddress().getAddressAsString());
		add(txtMyAdd);

		JLabel lblothadd = new JLabel("Known Address");
		lblothadd.setBounds(485, 45, 100, 10);
		add(lblothadd);
		txtKnownAdd.setBounds(485, 60, 100, 20);
		txtKnownAdd.setText("");
		add(txtKnownAdd);

		JButton butAddAddr = new JButton();
		butAddAddr.setBounds(485, 85, 100, 20);
		butAddAddr.setText("Set Addr");
		butAddAddr.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (txtKnownAdd.getText().length() > 8) {
					mapper
							.addKnownAddress(new TCPAddress(txtKnownAdd
									.getText()));
					txtKnownAdd.setText("");
				} else {
					mapper.setAddress(new TCPAddress(txtMyAdd.getText()));
				}

			}
		});
		add(butAddAddr);

		JLabel lbltemp = new JLabel("Temp Ctrl");
		lbltemp.setBounds(365, 190, 100, 10);
		add(lbltemp);
		slideTemp.setBounds(365, 205, 100, 20);
		add(slideTemp);

		JLabel lblconsole = new JLabel("Console");
		lblconsole.setBounds(5, 190, 100, 10);
		add(lblconsole);

		txtLogger.setBounds(5, 205, 350, 90);
		JScrollPane scrPaneConsole = new JScrollPane(txtLogger);
		scrPaneConsole
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrPaneConsole.setBounds(5, 205, 350, 90);
		add(scrPaneConsole);

		this.setSize(610, 350);
		// pack();
		setVisible(true);
	}

	@Override
	public void onLog(String s) {

		txtLogger.append(s + "\n");
		// scroll
		txtLogger.setCaretPosition(txtLogger.getText().length());
	}

	@Override
	public void onDataResponse(ServiceDescription serviceDesc, int serviceId,
			byte[] response) {
		txtLogger.append("Data received: " + response + "\n");
		double lat = 0;
		double lon = 0;
		try {
			LatLon ll = FakeGPSModule.parseData(response);
			if (ll != null) {
				lat = ll.Lat;
				lon = ll.Lon;
			}
		} finally {

		}

		txtLogger.append("Data as int: " + BinaryTransformer.toInt(response)
				+ "... as LatLon: " + lat + "; " + lon + "\n");

		// scroll
		txtLogger.setCaretPosition(txtLogger.getText().length());

	}

	@Override
	public void onObservableResponse(byte[] value) {

		txtLogger.append("Data received: " + value + "\n");
		double lat = 0;
		double lon = 0;
		try {
			LatLon ll = FakeGPSModule.parseData(value);
			if (ll != null) {
				lat = ll.Lat;
				lon = ll.Lon;
			}
		} finally {

		}

		txtLogger.append("Data as int: " + BinaryTransformer.toInt(value)
				+ "... as LatLon: " + lat + "; " + lon + "\n");

		// scroll
		txtLogger.setCaretPosition(txtLogger.getText().length());

	}

}
