package spider.prototype.services.yellowpage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spider.prototype.services.Controller;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.utils.KeyValuePair;
import spider.testing.Logger;

public class YellowPageService {

	// Phonebook
	private HashMap<ServiceDescription, ArrayList<YellowPage>> herold = new HashMap<ServiceDescription, ArrayList<YellowPage>>();
	// atm handling requests
	private HashMap<ServiceDescription, ArrayList<Address>> blocker = new HashMap<ServiceDescription, ArrayList<Address>>();
	private Timer timer = new Timer();

	/**
	 * Handle Service Request
	 * 
	 * @param addr
	 * @param serviceDesc
	 * @param hops
	 * @param maxHops
	 */
	public void handleServiceRequest(Address addr,
			ServiceDescription serviceDesc, int hops, int maxHops) {

		Logger.getInstance().log("handling service request (requester=" + addr.getAddressAsString() 
				+ ", service description=" + serviceDesc.toString() + ", hops=" + hops 
				+ ", maxHops=" + maxHops + ")");
		
		// TODO martin: was passiert bei 2ter Anfrage auf selben service?

		// do this only if this servicerequest is not already requested
		if (blocker.containsKey(serviceDesc)) {

			if (!blocker.get(serviceDesc).contains(addr)) {
				blocker.get(serviceDesc).add(addr);
			}
			
			Logger.getInstance().log("already received service request - ignoring");
			
			return;
		}
		blocker.put(serviceDesc, new ArrayList<Address>());
		blocker.get(serviceDesc).add(addr);

		// start timer 2 delete this request and allow another one
		// TODO: read time 4 timer from file o.s.a.
		timer = new Timer();
		timer.schedule(new ProgressHandler(serviceDesc), 120 * 1000);

		// look for the requested service here
		if (herold.containsKey(serviceDesc)) {

			ArrayList<YellowPage> response = getYellowPagesForResponse(
					serviceDesc, addr);

			Logger.getInstance().log("service available - sending service request response");
			
			Controller.getInstance().sendServiceRequestResponse(addr, response);
		} else {

			// ask other devices if its not on maxhops
			if (hops < maxHops) {

				
				hops++;
				
				Logger.getInstance().log("forwarding service request (hops = " + hops + ")");
				
				Controller.getInstance().broadcastServiceRequest(serviceDesc,
						hops, maxHops);
			}
		}
	}

	/**
	 * handle Service Request Response
	 * 
	 * @param serviceDesc
	 * @param addressesToServiceId
	 */
	synchronized public void handleServiceRequestResponse(
			ServiceDescription serviceDesc,
			List<KeyValuePair<Address, Integer>> addressesToServiceId) {

		Logger.getInstance().log("handling service request response (service description=" + serviceDesc.toString() + ")");
		
		// add each address if not already in list
		for (KeyValuePair<Address, Integer> entry : addressesToServiceId) {

			Address ad = entry.getKey();
			int serviceId = entry.getValue();

			boolean isInList = false;
			if (herold.containsKey(serviceDesc)) {
				for (YellowPage yp : herold.get(serviceDesc)) {
					if (yp.getAddress().getAddressAsString().equals(
							ad.getAddressAsString())) {
						isInList = true;
					}

				}

				if (isInList == false) {
					addService(serviceDesc, ad, serviceId);
				}

			} else {
				addService(serviceDesc, ad, serviceId);
			}
		}

		// forward response
		if (blocker.containsKey(serviceDesc)) {
			for (Address addr : blocker.get(serviceDesc)) {

				ArrayList<YellowPage> response = getYellowPagesForResponse(
						serviceDesc, addr);

				Logger.getInstance().log("forwarding service request response to " + addr.getAddressAsString());
				
				Controller.getInstance().sendServiceRequestResponse(addr,
						response);
			}
			blocker.remove(serviceDesc);
		}

	}

	/**
	 * 
	 * If the given address doesn't support the network type in the yellow page,
	 * a fake yellow page will be generated and this yellow page service acts as
	 * gateway
	 * 
	 * @param serviceDesc
	 * @param addr
	 *            The source address of the requester
	 * @return
	 */
	private ArrayList<YellowPage> getYellowPagesForResponse(
			ServiceDescription serviceDesc, Address addr) {

		ArrayList<YellowPage> response = new ArrayList<YellowPage>(herold
				.get(serviceDesc));
		// check if receiver is able to request data for the
		// network types defined in the addresses in the yellow pages
		for (int i = 0; i < response.size(); i++) {
			YellowPage yp = response.get(i);
			if (!yp.getAddress().getNetworkType().equals(addr.getNetworkType())) {

				// search the correct address for the needed network
				// type
				Address fakeYPAddress = null;
				for (Address availableAddr : Controller.getInstance()
						.getCommunicationService().getAddresses()) {
					if (availableAddr.getNetworkType().equals(
							addr.getNetworkType())) {
						fakeYPAddress = availableAddr;
						break;
					}
				}

				if (fakeYPAddress == null) {
					// TODO [Protokollierung]
					// this shouldn't happen!
				} else {

					Logger.getInstance().log("faking yellow page for gateway functionality " +
							"(oldAddress=" + yp.getAddress().getAddressAsString()
							+", newAddress=" + fakeYPAddress.getAddressAsString() + ")");
					
					YellowPage fakeYellowPage = new YellowPage(serviceDesc,
							fakeYPAddress, yp.getServiceId());
					response.remove(yp);
					response.add(fakeYellowPage);
				}
			}
		}
		return response;
	}

	/**
	 * add a service to this yellowpage
	 * 
	 * @param serviceDesc
	 * @param addr
	 */
	public void addService(ServiceDescription serviceDesc, Address addr,
			int serviceId) {

		Logger.getInstance().log("adding yellow page (service description=" + serviceDesc
				+ ", address=" +  addr.getAddressAsString() + ", serviceId=" + serviceId + ")");
		
		// add service to herold hashmap
		if (!herold.containsKey(serviceDesc)) {
			herold.put(serviceDesc, new ArrayList<YellowPage>());
		}
		herold.get(serviceDesc).add(
				new YellowPage(serviceDesc, addr, serviceId));
	}

	public void removeService(AbstractModule module) {

		// remove yellow page with the service description and service id
		// specified in the module
		if (herold.containsKey(module.getServiceDescription())) {
			for (int i = 0; i < herold.get(module.getServiceDescription())
					.size();) {
				if (((YellowPage) herold.get(module.getServiceDescription())
						.get(i)).getServiceId() == module.getServiceId()) {
					herold.get(module.getServiceDescription()).remove(i);
				} else {
					i++;
				}
			}
		}
	}

	// private Timer Class 4 deleting blocked requests
	private class ProgressHandler extends TimerTask {

		private ServiceDescription serviceDesc;

		public ProgressHandler(ServiceDescription serviceDesc) {

			this.serviceDesc = serviceDesc;
		}

		@Override
		public void run() {

			blocker.remove(serviceDesc);
		}

	}

	public YellowPage get(ServiceDescription sd, int i) {

		if (herold.containsKey(sd) && herold.get(sd) != null
				&& i < herold.get(sd).size()) {
			return herold.get(sd).get(i);
		}
		return null;
	}

	public YellowPage getId(ServiceDescription sd, int serviceId) {

		if (herold.containsKey(sd) && herold.get(sd) != null) {

			for (YellowPage yp : herold.get(sd)) {
				if (yp.getServiceId().equals(serviceId)) {
					return yp;
				}
			}
		}

		return null;
	}

	public List<YellowPage> get(ServiceDescription sd) {

		if (herold.containsKey(sd) && herold.get(sd) != null) {
			return herold.get(sd);
		}
		return new ArrayList<YellowPage>();
	}
}
