package spider.prototype.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import spider.prototype.services.communication.CommunicationService;
import spider.prototype.services.communication.ProtocolTag;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.modules.IDataRequester;
import spider.prototype.services.modules.IServiceObserver;
import spider.prototype.services.modules.position.FakeGPSModule;
import spider.prototype.services.yellowpage.ServiceDescription;
import spider.prototype.services.yellowpage.YellowPage;
import spider.prototype.services.yellowpage.YellowPageService;
import spider.prototype.utils.BinaryTransformer;
import spider.prototype.utils.KeyValuePair;
import spider.prototype.utils.PacketBuilder;
import spider.testing.Logger;
import spider.testing.executables.devicegui.ILogger;

/***
 * God class of martin
 * 
 * @author God
 * 
 */
public class Controller {

	private static Controller instance;

	public static Controller getInstance() {
		return instance;
	}

	public static void setInstance(Controller i) {
		instance = i;
	}

	// these modules are always available
	private CommunicationService communicator = new CommunicationService();
	private YellowPageService yps = new YellowPageService();

	// request0rs
	private HashMap<Integer, Queue<IDataRequester>> request0rs = new HashMap<Integer, Queue<IDataRequester>>();
	// service observat0rs
	private HashMap<Integer, List<IServiceObserver>> observat0rs = new HashMap<Integer, List<IServiceObserver>>();

	/***
	 * Optional modules
	 */
	private HashMap<ServiceDescription, HashMap<Integer, AbstractModule>> availableModules = new HashMap<ServiceDescription, HashMap<Integer, AbstractModule>>();

	public Controller() {

	}

	public void initialize(List<AbstractModule> modules, ILogger logger) {

		for (AbstractModule module : modules) {
			addModule(module);
		}
	}

	public void initialize() {
		// initialize controller

		// add modules
		// TODO read available modules from file
		// TODO service id
		FakeGPSModule gpsModule = new FakeGPSModule(1);
		availableModules.put(gpsModule.getServiceDescription(),
				new HashMap<Integer, AbstractModule>());
		availableModules.get(gpsModule.getServiceDescription()).put(
				gpsModule.getServiceId(), gpsModule);

		// initialize yellow page service
		for (Address address : communicator.getAddresses()) {
			yps.addService(gpsModule.getServiceDescription(), address,
					gpsModule.getServiceId());
		}
	}

	/***
	 * 
	 * @return The communication service
	 */
	public CommunicationService getCommunicationService() {
		return communicator;
	}

	/***
	 * Processes the given packet
	 * 
	 * @param tag
	 *            The ProtocolTag of the received packet
	 * @param packetData
	 *            The data part of the received packet
	 */
	public void processPacket(ProtocolTag tag, byte[] packetData) {

		Logger.getInstance().log("received packet (ProtocolTag=" + tag.toString() + ")");

		switch (tag) {
		case ServiceRequest:
			handleServiceRequest(packetData);
			break;
		case ServiceRequestResponse:
			handleServiceRequestResponse(packetData);
			break;
		case DataRequest:
			handleDataRequest(packetData);
			break;
		case UseRequest:
			handleUseRequest(packetData);
			break;
		case DataResponse:
			handleDataResponse(packetData);
			break;
		case ServiceObservationRequest:
			handleServiceObservationRequest(packetData);
			break;
		case Invalid:
		default:
			// TODO [Protokollierung]
			System.err
					.println("Received packet with invalid protocol tag. Tag="
							+ tag.toString());
		}

	}

	/**
	 * 
	 * @param packetData
	 */
	private void handleServiceObservationRequest(byte[] packetData) {

		// x-bytes: source-address
		// 1 byte: service-description
		// FIXME: Service-ID ist vier Byte lang!!!
		// 1 byte: service id
		// 4 bytes: interval
		// x bytes: value

		// parse source address
		Address sourceAddress = Address.createAddress(packetData);

		// get index for the service-description byte
		int index = sourceAddress.getOffset();

		ServiceDescription serviceDesc = ServiceDescription
				.convert(packetData[index++]);
		// FIXME: Service-ID ist vier Byte lang!!!
		int serviceId = BinaryTransformer.toInt(packetData[index++]);
		byte[] interval = new byte[] { packetData[index++],
				packetData[index++], packetData[index++], packetData[index++] };
		int intervalInMilliseconds = BinaryTransformer.toInt(interval);

		// x bytes: value --> x = packetData.length - sourceAddress.getOffset()
		// - 6
		byte[] conditions = new byte[packetData.length
				- sourceAddress.getOffset() - 6];
		for (int j = 0, i = index; i < packetData.length; i++, j++) {
			conditions[j] = packetData[i];
		}

		Logger.getInstance().log("handling service observation request (source address=" + sourceAddress.getAddressAsString() 
				+ ", service description=" + serviceDesc.toString() 
				+ ", serviceId=" + serviceId + ", interval(ms)=" + intervalInMilliseconds 
				+", condition=" + conditions.toString() + ")");
		
		// forward request
		if (availableModules.containsKey(serviceDesc)) {

			if (availableModules.get(serviceDesc).containsKey(serviceId)) {

				availableModules.get(serviceDesc).get(serviceId).observe(
						sourceAddress, intervalInMilliseconds, conditions);
				return;
			}
		}

		// TODO [Protokollierung]
		// TODO [Response]
		// send fail response?
	}

	/**
	 * 
	 * @param packetData
	 */
	private void handleDataResponse(byte[] packetData) {

		// s
		// 1 byte: service-description
		// 1 byte: service-id
		// x byte: value

		ServiceDescription serviceDesc = ServiceDescription
				.convert(packetData[0]);
		int serviceId = BinaryTransformer.toInt(packetData[1]);

		// extract data
		byte[] value = new byte[packetData.length - 2];
		for (int i = 0; i < value.length; i++) {
			value[i] = packetData[i + 2];
		}

		Logger.getInstance().log("handling data response (service description=" + serviceDesc.toString()
				+ ", serviceId=" + serviceId + ", value=" + value.toString() + ")");
		
		int requestId = Integer.valueOf(serviceDesc.ordinal() + "" + serviceId);
		// single requeset
		if (request0rs.containsKey(requestId)) {
			// get requesters

			if (!request0rs.get(requestId).isEmpty()) {
				request0rs.get(requestId).poll().onDataResponse(serviceDesc,
						serviceId, value);
			}
		}
		// observers
		if (observat0rs.containsKey(requestId)) {
			// get observators

			// send the received response to all observers
			for (IServiceObserver observer : observat0rs.get(requestId)) {
				observer.onObservableResponse(value);
			}
		}

		if (serviceDesc == ServiceDescription.KnownPlace) {
			// KnownPlace Service pushed place data - no request has been sent
			// before
			// TODO check if someone has registered for this kind of message and
			// forward it

		}
	}

	/**
	 * 
	 * @param packetData
	 */
	private void handleUseRequest(byte[] packetData) {

		// x-bytes: source-address
		// 1 byte: service-description
		// FIXME: Service-ID ist vier Byte lang!!!
		// 1 byte: service id
		// x bytes: value

		// parse source address
		Address sourceAddress = Address.createAddress(packetData);

		// get index for the service-description byte
		int index = sourceAddress.getOffset();

		ServiceDescription serviceDesc = ServiceDescription
				.convert(packetData[index++]);
		// FIXME: Service-ID ist vier Byte lang!!!
		int serviceId = BinaryTransformer.toInt(packetData[index++]);

		// x bytes: value --> x = packetData.length - sourceAddress.getOffset()
		// - 2
		byte[] parameters = new byte[packetData.length
				- sourceAddress.getOffset() - 2];
		for (int j = 0, i = index; i < packetData.length; i++, j++) {
			parameters[j] = packetData[i];
		}

		Logger.getInstance().log("handling service use request (source address=" + sourceAddress.getAddressAsString() 
				+ ", service description=" + serviceDesc.toString()  
				+", parameters=" + parameters.toString() + ")");
		
		// forward request
		if (availableModules.containsKey(serviceDesc)) {

			if (availableModules.get(serviceDesc).containsKey(serviceId)) {

				availableModules.get(serviceDesc).get(serviceId)
						.use(parameters);
				return;
			}
		}

		// TODO [Protokollierung]
		// TODO [Response]
		// send fail response?

	}

	/**
	 * 
	 * @param packetData
	 */
	private void handleDataRequest(byte[] packetData) {

		// x-bytes: source-address
		// 1 byte: service-description
		// FIXME: Service-ID ist vier Byte lang!!!
		// 1 byte: service id
		
		// parse source address
		final Address sourceAddress = Address.createAddress(packetData);

		// get index for the service-description byte
		int index = sourceAddress.getOffset();

		ServiceDescription serviceDesc = ServiceDescription
				.convert(packetData[index++]);

		// FIXME: Service-ID ist vier Byte lang!!!
		int serviceId = BinaryTransformer.toInt(packetData[index++]);

		Logger.getInstance().log("handling data response (requester=" + sourceAddress.getAddressAsString() +
				", service description=" + serviceDesc.toString() + ", serviceId=" +  serviceId  +")");
		
		// forward request
		if (availableModules.containsKey(serviceDesc)) {

			if (availableModules.get(serviceDesc).containsKey(serviceId)) {

				byte[] value = availableModules.get(serviceDesc).get(serviceId).getValue();
				
				Logger.getInstance().log("sending data response (receiver=" + sourceAddress.getAddressAsString() +
						", service description=" + serviceDesc.toString() + ", serviceId=" +  serviceId 
						+", value=" + value.toString() + ")");
				
				sendDataResponse(sourceAddress, serviceDesc, serviceId, value);
				return;

			}
		} else {
			YellowPage yp = yps.getId(serviceDesc, serviceId);
			if(yp != null) {
				// found entry in the yellow page service
				// forward the data request
				
				Logger.getInstance().log("forwarding data request to " + yp.getAddress().getAddressAsString());
				
				sendDataRequest(new IDataRequester() {
					
					@Override
					public void onDataResponse(ServiceDescription serviceDesc, int serviceId,
							byte[] response) {
						
						Logger.getInstance().log("forwarding data response to " + sourceAddress.getAddressAsString());
						
						// forward data response to original requester
						sendDataResponse(sourceAddress, serviceDesc, serviceId, response);
					}
				}, yp.getAddress(), serviceDesc, serviceId);
			}
		}

		// TODO [Protokollierung]
		// TODO [Response]
		// send fail response?
	}

	/**
	 * 
	 * @param data
	 */
	private void handleServiceRequestResponse(byte[] data) {

		// 1 byte = service description
		// 1 byte = countOfAddresses
		// (1 byte (network type flag) + address_length) * countOfAddresses
		// 4 byte service-id * countOfAddresses

		int index = 0;

		ServiceDescription serviceDesc = ServiceDescription
				.convert(data[index++]);
		int countOfAddresses = BinaryTransformer.toInt(data[index++]);

		List<KeyValuePair<Address, Integer>> pageAddressToServiceId = new ArrayList<KeyValuePair<Address, Integer>>();

		// parse addresses
		for (int i = 0; i < countOfAddresses; i++) {
			Address pageAddress = Address.createAddress(data, index);
			index += pageAddress.getOffset();

			pageAddressToServiceId.add(new KeyValuePair<Address, Integer>(
					pageAddress, YellowPage.NO_SERVICE_ID));
		}

		// parse service ids
		for (int i = 0; i < countOfAddresses; i++) {
			// service id = int = 4 bytes
			byte[] serviceIdArray = new byte[] { data[index++], data[index++],
					data[index++], data[index++] };

			// convert service id to int
			pageAddressToServiceId.get(i).setValue(
					BinaryTransformer.toInt(serviceIdArray));
		}

		yps.handleServiceRequestResponse(serviceDesc, pageAddressToServiceId);

	}

	/***
	 * Forwards the service request to the yellow page service
	 * 
	 * @param data
	 *            The packet data
	 */
	private void handleServiceRequest(byte[] data) {

		// x-bytes: source-address
		// 1 byte: service-description
		// 1 byte: hops
		// 1 byte: maxHops

		// parse source address
		Address sourceAddress = Address.createAddress(data);

		// get index for the service-description byte
		int index = sourceAddress.getOffset();

		ServiceDescription serviceDesc = ServiceDescription
				.convert(data[index++]);
		int hops = BinaryTransformer.toInt(data[index++]);
		int maxHops = BinaryTransformer.toInt(data[index++]);

		// forwared request
		yps.handleServiceRequest(sourceAddress, serviceDesc, hops, maxHops);
	}

	/***
	 * Sends a service request response to the given receiver
	 * 
	 * @param receiver
	 *            The receiver
	 * @param pages
	 *            The available addresses for the service that has been
	 *            requested
	 */
	public void sendServiceRequestResponse(Address receiver,
			ArrayList<YellowPage> pages) {

		StringBuilder log = new StringBuilder();
		log.append("sending service request response (receiver=");
		log.append(receiver.getAddressAsString());
		log.append(", ");
		
		// key = address of service; value = service id
		List<KeyValuePair<Address, Integer>> pageAddressToServiceId = 
			new ArrayList<KeyValuePair<Address, Integer>>();
		for (YellowPage page : pages) {
			pageAddressToServiceId.add(new KeyValuePair<Address, Integer>(page
					.getAddress(), page.getServiceId()));
			
			log.append("page: service description=");
			log.append(page.getServiceDescription().toString());
			log.append(", serviceId=");
			log.append(page.getServiceId());
			log.append(", address=");
			log.append(page.getAddress().getAddressAsString());
			log.append("; ");
		}

		
		
		Logger.getInstance().log(log.toString());
		
		// send response
		communicator.sendAt(receiver, PacketBuilder
				.buildServiceRequestResponse(pages.get(0)
						.getServiceDescription(), pageAddressToServiceId));
	}

	/**
	 * 
	 * @param receiver
	 * @param serviceDescription
	 * @param serviceId
	 * @param value
	 */
	public void sendDataResponse(Address receiver,
			ServiceDescription serviceDescription, int serviceId, byte[] value) {

		Logger.getInstance().log("sending data response (receiver=" + receiver.getAddressAsString() 
				+ ", service description=" + serviceDescription.toString() + ", serviceId=" + serviceId 
				+ ", value=" +  value.toString() + ")");
		
		communicator.sendAt(receiver, PacketBuilder.buildDataResponse(
				serviceDescription, serviceId, value));
	}

	/**
	 * 
	 * @param receiver
	 * @param serviceDescription
	 * @param serviceId
	 * @param value
	 */
	public void sendUseRequest(Address receiver,
			ServiceDescription serviceDescription, int serviceId, byte[] value) {

		Logger.getInstance().log("sending use request (receiver=" + receiver.getAddressAsString() 
				+", service description=" + serviceDescription.toString() 
				+", serviceId=" + serviceId 
				+", parameters=" + value.toString());
		
		communicator.sendAt(receiver, PacketBuilder.buildDataResponse(
				serviceDescription, serviceId, value), true);
	}

	/**
	 * 
	 * @param receiver
	 * @param serviceDescription
	 * @param serviceId
	 */
	public void sendDataRequest(IDataRequester requester, Address receiver,
			ServiceDescription serviceDescription, int serviceId) {

		int requestId = Integer.valueOf(serviceDescription.ordinal() + ""
				+ serviceId);

		if (!request0rs.containsKey(requestId)) {
			request0rs.put(requestId, new LinkedList<IDataRequester>());
		}
		
		Logger.getInstance().log("sending data request (receiver=" + receiver.getAddressAsString() 
				+ ", service description=" + serviceDescription.toString() + ", serviceId=" + serviceId + ")");

		communicator.sendAt(receiver, PacketBuilder.buildDataRequest(
				serviceDescription, serviceId), true);

		request0rs.get(requestId).add(requester);
	}

	/**
	 * 
	 * @param observer
	 * @param receiver
	 * @param serviceDescription
	 * @param serviceId
	 * @param intervalInMilliseconds
	 * @param alertConditions
	 */
	public void sendServiceObservationRequest(IServiceObserver observer,
			Address receiver, ServiceDescription serviceDescription,
			int serviceId, int intervalInMilliseconds, byte[] alertConditions) {

		int observeId = Integer.valueOf(serviceDescription.ordinal() + ""
				+ serviceId);

		if (!observat0rs.containsKey(observeId)) {
			observat0rs.put(observeId, new LinkedList<IServiceObserver>());

			Logger.getInstance().log("sending service observation request (receiver=" + receiver.getAddressAsString() 
					+ ", service description=" + serviceDescription.toString() + ", serviceId=" + serviceId 
					+ ", interval(ms)=" + intervalInMilliseconds + ", conditions=" + alertConditions.toString() + ")");
			
			communicator.sendAt(receiver,
					PacketBuilder.buildServiceObservationRequest(
							serviceDescription, serviceId,
							intervalInMilliseconds, alertConditions), true);
		}

		observat0rs.get(observeId).add(observer);
	}

	/***
	 * Boradcasts a service request
	 * 
	 * @param serviceDesc
	 *            The service description
	 * @param hops
	 *            The current hops
	 * @param maxHops
	 *            The maximum number of allowed hops
	 */
	public void broadcastServiceRequest(ServiceDescription serviceDesc,
			int hops, int maxHops) {

		Logger.getInstance().log("broadcasting service request (service description=" + serviceDesc.toString() + ")");
		
		communicator.broadcast(PacketBuilder.buildServiceRequest(serviceDesc,
				hops, maxHops));
	}

	public YellowPage getYellowPage(ServiceDescription sd, int i) {

		return yps.get(sd, i);
	}

	public List<YellowPage> getYellowPage(ServiceDescription sd) {

		return yps.get(sd);
	}

	public void addModule(AbstractModule module) {
		for (Address address : communicator.getAddresses()) {

			if (!availableModules.containsKey(module.getServiceDescription())) {
				availableModules.put(module.getServiceDescription(),
						new HashMap<Integer, AbstractModule>());
			}
			availableModules.get(module.getServiceDescription()).put(
					module.getServiceId(), module);
			yps.addService(module.getServiceDescription(), address, module
					.getServiceId());

		}
	}

	public void removeModule(AbstractModule module) {
		yps.removeService(module);
	}
}
