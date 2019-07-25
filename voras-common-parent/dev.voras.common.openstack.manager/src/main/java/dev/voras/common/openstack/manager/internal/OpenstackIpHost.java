package dev.voras.common.openstack.manager.internal;

import javax.validation.constraints.NotNull;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.ipnetwork.IpNetworkManagerException;

public class OpenstackIpHost implements IIpHost {
	
	private final String hostname;
	private final ICredentials credentials; 
	
	public OpenstackIpHost(String hostname, @NotNull ICredentials credentials) {
		this.hostname    = hostname;
		this.credentials = credentials;
	}

	@Override
	public String getHostname() {
		return this.hostname;
	}

	@Override
	public String getIpv4Hostname() {
		return this.hostname;
	}

	@Override
	public String getIpv6Hostname() {
		return null;
	}

	@Override
	public int getTelnetPort() throws IpNetworkManagerException {
		return 22;
	}

	@Override
	public boolean isTelnetPortTls() throws IpNetworkManagerException {
		return false;
	}

	@Override
	public int getFtpPort() throws IpNetworkManagerException {
		return 21;
	}

	@Override
	public boolean isFtpPortTls() throws IpNetworkManagerException {
		return false;
	}

	@Override
	public int getSshPort() throws IpNetworkManagerException {
		return 22;
	}

	@Override
	public @NotNull ICredentials getDefaultCredentials() throws IpNetworkManagerException {
		return this.credentials;
	}

}
