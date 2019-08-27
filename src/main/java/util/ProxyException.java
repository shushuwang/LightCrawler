package util;

public class ProxyException extends Exception {
	private static final long serialVersionUID = 1L;

	//constructor
	public ProxyException(){
		super();
	}
	public ProxyException(String proxyList){	//"|" seperate if more than one
		super(proxyList);
	}
	
}
