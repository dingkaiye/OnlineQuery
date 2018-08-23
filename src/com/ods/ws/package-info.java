@XmlSchema( 
   xmlns={  
		   @XmlNs(prefix="soap", namespaceURI = NameSpace.SOAP_URL),
		   @XmlNs(prefix="s", namespaceURI = NameSpace.ODS_URL) ,
	       @XmlNs(prefix="tns", namespaceURI = NameSpace.ODS_WSDL ) 
   }  
)  
package com.ods.ws;  
import javax.xml.bind.annotation.XmlNs;  
import javax.xml.bind.annotation.XmlSchema;

import com.ods.common.NameSpace;

