package spider.prototype.services.communication.mappinglayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import spider.prototype.services.Controller;
import spider.prototype.services.communication.AbstractMapper;
import spider.prototype.services.communication.NetworkType;
import spider.prototype.services.communication.ProtocolTag;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.communication.addresses.TCPAddress;
import spider.prototype.utils.BinaryTransformer;

/***
 * 
 * The mapper for TCP/IP communication
 * 
 * @author Thomas
 *
 */
public class TCPMapper extends AbstractMapper {

	/***
	 * The port of the server socket
	 */
	private int port;
	
	/***
	 * The server socket that listens for incoming messages
	 */
	private ServerSocket serverSocket;
	
	/***
	 * Flag
	 */
	private boolean running;
	
	/***
<<<<<<< .mine
=======
	 * Known addresses (to enable simple broadcasting) & my address
	 */
	private List<TCPAddress> knownAddresses = new ArrayList<TCPAddress>();
	private TCPAddress myAddress = new TCPAddress("127.0.0.1");
	
	/***
>>>>>>> .r94
	 * Initializes the tcp mapper
	 * @param port The port for the server socket
	 */
	public TCPMapper(int port) {
		super(NetworkType.TCP);
		
		this.port = port;
	}
	
	/***
	 * 
	 * @return If the server is listing
	 */
	private synchronized boolean isRunning() {
		return running;
	}
	
	/***
	 * Sets the running flag
	 * @param value The value of the flag
	 */
	private synchronized void setRunning(boolean value) {
		running = value;
	}
	
	/***
	 * Returns a list of known addresses
	 */
	private List<Address> readKnownAddresses() {
		
		// TODO read from file
		List<Address> addresses = new ArrayList<Address>();
		//addresses.add(new TCPAddress("172.16.100.217"));

		return addresses;
	}
	
	public void addKnownAddress(TCPAddress tcpa) {
		
		knownAddresses.add(tcpa);
	}

	@Override
	public void startListening() {
		
		// don't start listening multiple times
		if(isRunning()) return;
		
		try {
			serverSocket = new ServerSocket(port);
			setRunning(true);
			while(isRunning()) {
				final Socket client = serverSocket.accept();
				
				// read data in own thread to avoid blocking sockets
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							// get byte stream
							DataInputStream in = new DataInputStream(client.getInputStream());
							// header is always 5 bytes (tag + length)
							byte[] header = new byte[5];
							// read header
							in.read(header);
							// parse length
							int length = BinaryTransformer.toInt(new byte[] { header[1], header[2], header[3], header[4]});
							// read data part
							byte[] data = new byte[length];
							in.read(data);
							
							in.close();
							
							// forward received data
							Controller.getInstance().processPacket(ProtocolTag.conv(header[0]), data);							
						} catch (IOException e) {
							// TODO [Protokollierung]
							e.printStackTrace();
						}
					}
				}).start();
			}
		} catch (IOException e) {
			// TODO [Protokollierung]
			e.printStackTrace();
		} finally {
			setRunning(false);
		}
	}

	@Override
	public void stopListening() {
		
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO [Protokollierung]
				e.printStackTrace();
			}
		}
		
		setRunning(false);
	}

	@Override
	public Address getAddress() {
		// TODO read from file
		return myAddress;
	}
	
	public void setAddress(TCPAddress tcpa) {
		
		myAddress = tcpa;
	}

	@Override
	public boolean sendAt(Address receiver, byte[] data) {
		
		try {
			Socket s = new Socket(receiver.getAddressAsString(), port);
			try {
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				out.write(data);
				out.flush();
			} finally {
				s.close();
			}
			
			return true;
		} catch (Exception e) {
			// TODO [Protokollierung]
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	protected List<Address> discoverNeighbours() {
		return readKnownAddresses();
	}
	
	
}
