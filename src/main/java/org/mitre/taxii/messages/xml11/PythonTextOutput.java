package org.mitre.taxii.messages.xml11;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.mitre.taxii.query.DefaultQuery;
import org.mitre.taxii.query.DefaultQueryInfo;

/**
 * This class replicates the Python libtaxii to_text() output.
 * 
 * The cleaner way to implement this would be for each TAXII class have a toText()
 * method. However, the TAXII classes are generated by XJC and I would much rather
 * avoid post-processing the generated code.
 * 
 * @author jasenj1
 */
public class PythonTextOutput {
    private static final String STD_INDENT = "  "; // A "Standard Indent" to use for to_text() methods
    
    public static String toText(Object obj) {
        return toText(obj,"");        
    }

    public static String toText(Object obj, String line_prepend) {
        String s = new String();
        if (null == line_prepend) {
            line_prepend = "";
        }
        
        if (obj instanceof SupportedQueryType) {
            SupportedQueryType self = (SupportedQueryType) obj;
            s = line_prepend + "=== Supported Query Information ===\n";
            s += line_prepend + String.format("  Query Format: %s\n", toStringOrNone(self.getFormatId()));
            for (Object child : self.getContent()) {
                if (child instanceof DefaultQueryInfo) {
                    s += org.mitre.taxii.query.PythonTextOutput.toText(child, line_prepend);                    
                }
            }
            return s;
        }
        
        if (obj instanceof QueryType) {
            QueryType self = (QueryType) obj;
            s = line_prepend + "=== Query ===\n";
            s += line_prepend + String.format("  Query Format: %s\n", toStringOrNone(self.getFormatId()));
            /* QueryType contains an AnyMixedContentType child - which is a List of Object.
               The format id tells us what the child is at a semantic level, but not in an XML/Object way.
               From an XML/object perspective all we can do is see what the children are instanceof.
            
                Because on the Python side, to_text() is a method on the object, libtaxii can simply obj.to_text()
                the children of the Query.
            */
            for (Object child : self.getContent()) {
                if (child instanceof DefaultQuery) { // Is it a TAXII Default Query? It will only be this if the query JAXB context was added to the TaxiiXml object.
                    s+= org.mitre.taxii.query.PythonTextOutput.toText(child, line_prepend);
                }
            }
            return s;
        }
                
        if (obj instanceof ContentBindingIDType) {
            ContentBindingIDType self = (ContentBindingIDType)obj;
            s = line_prepend;
            s += self.getBindingId();
            if (!self.getSubtypes().isEmpty()) {
                List<String> subtypes = new ArrayList();
                for (SubtypeType st : self.getSubtypes()) {
                    subtypes.add(st.getSubtypeId());
                }                
                s += ">" + join(",",subtypes);
            }            
            return s;
        }
        
        if (obj instanceof RecordCountType) {
            RecordCountType self = (RecordCountType)obj;
            s = line_prepend + "=== Record Count ===\n";
            s += line_prepend + String.format("  Record Count: %s\n", toStringOrNone(self.getValue()));
            s += line_prepend + String.format("  Partial Count: %s\n", booleanString(self.isPartialCount()));            
            return s;
        }
        
        // GenericParameters handled in a method call.
        
        if (obj instanceof SubscriptionParametersType) {
            return genericParameters(obj, "Subscription_Parameters", line_prepend);
        }
        
        if (obj instanceof PollParametersType) {
            PollParametersType self = (PollParametersType)obj;
            s = genericParameters(self, "Poll_Parameters", line_prepend);
            
            s += line_prepend + String.format("  Allow Asynch: %s\n", booleanString(self.isAllowAsynch()));
            if(null != self.getDeliveryParameters()) {
                s += toText(self.getDeliveryParameters(), line_prepend + STD_INDENT);
            }
            return s;            
        }
        
        if (obj instanceof ContentBlock) {
            ContentBlock self = (ContentBlock)obj;
            
            s = line_prepend + "=== Content Block ===\n";
            s += line_prepend + String.format("  Content Binding: %s\n", toText(self.getContentBinding(), "")); // ContentInstanceType
            /* TODO: On the Python side the below is the length of the marshaled/serialized content.
                libtaxii stores the content internally as a string. On the Java side the content is
                a list of objects (see AnyMixedContentType). So to really get the
                length would require marshaling the content - an expensive operation
                that requires a TaxiiXml object with the proper JAXB context.
                i.e. It's a pain to do. Skip it for now.
            */
            s += line_prepend + String.format("  Content length: %s\n", "unknown");
                        
            s += line_prepend + "  (Content not printed for brevity)\n";
            if (null != self.getTimestampLabel()) {
                s += line_prepend + String.format("  Timestamp Label: %s\n", self.getTimestampLabel().toXMLFormat());
            }
            s += line_prepend + String.format("  Message: %s\n", toStringOrNone(self.getMessage()));
            s += line_prepend + String.format("  Padding: %s\n", toStringOrNone(self.getPadding()));
            
            return s;            
        }
        
        if (obj instanceof ContentInstanceType) {
            ContentInstanceType self = (ContentInstanceType)obj;
            s = line_prepend;
            s += toStringOrNone(self.getBindingId());
            if (null != self.getSubtype()) {
                s += ">" + self.getSubtype().getSubtypeId();
            }
        }

        if (obj instanceof PushParameterType) {
            PushParameterType self = (PushParameterType) obj;
            s = line_prepend + "=== Push Parameters ===\n";
            s += line_prepend + String.format("  Protocol Binding: %s\n", toStringOrNone(self.getProtocolBinding()));
            s += line_prepend + String.format("  Inbox Address: %s\n", toStringOrNone(self.getAddress()));
            s += line_prepend + String.format("  Message Binding: %s\n", toStringOrNone(self.getMessageBinding()));
            return s;
        }
        
        /*
            And here we see the problem with this approach. MessageType is a common
            ancestor for all messages. What happens when an object is an instanceof 
            multiple class types?
            Should the ifs be stacked from most specific to least specific type?
            That's going to be a pain to track.
        */
        if (obj instanceof MessageType) {
            MessageType self = (MessageType) obj;

            XmlRootElement root = self.getClass().getAnnotation(XmlRootElement.class);
            s = line_prepend + String.format("Message Type: %s\n", root.name());
            s += line_prepend + String.format("Message ID: %s", toStringOrNone(self.getMessageId()));
            if (self instanceof ResponseMessageType) {       
                ResponseMessageType rmt = (ResponseMessageType)self;                
                s += String.format("; In Response To: %s", toStringOrNone(rmt.getInResponseTo()));
            }
            s += "\n";
                    
            ExtendedHeadersType eht = self.getExtendedHeaders();
            if (null != eht) {
                List<ExtendedHeaderType> ehtList = eht.getExtendedHeaders();
                for (ExtendedHeaderType eh : ehtList) {
                    // TODO: Note, this is another case where the Python stores the value as a string, but it really is AnyMixedContentType (See ContentBlockType above).
                    s += line_prepend + String.format("Extended Header: %s = %s\n", eh.getName(), "unknown");
                }                
            }                
            // Intentional fall through. MessageType is a superclass of many other message types.
            // return s; 
        }
        
        if (obj instanceof DiscoveryResponse) {
            DiscoveryResponse self = (DiscoveryResponse)obj;
            // s is populated by MessageType match already.
            for( ServiceInstanceType si : self.getServiceInstances()) {
                s += toText(si, line_prepend + STD_INDENT);                
            }
            return s;
        }
        
        if (obj instanceof ServiceInstanceType) {
            ServiceInstanceType self = (ServiceInstanceType)obj;
            s = line_prepend +  "=== Service Instance ===\n";
            s += line_prepend +  String.format("  Service Type: %s\n", toStringOrNone(self.getServiceType().name()));
            s += line_prepend +  String.format("  Service Version: %s\n", toStringOrNone(self.getServiceVersion()));
            s += line_prepend +  String.format("  Protocol Binding: %s\n", toStringOrNone(self.getProtocolBinding()));
            s += line_prepend +  String.format("  Service Address: %s\n", toStringOrNone(self.getAddress()));            
            for (String mb : self.getMessageBindings()) {
                s += line_prepend +  String.format("  Message Binding: %s\n", mb);
            }            
            if (ServiceTypeEnum.INBOX == self.getServiceType()) {
                List<String> bindings = new ArrayList();
                for (ContentBindingIDType binding : self.getContentBindings()) {
                    bindings.add("'"+toText(binding, "")+"'");
                }
                s += line_prepend + String.format("  Inbox Service AC: [%s]\n", join(",", bindings)); 
            }            
            s += line_prepend +  String.format("  Available: %s\n", booleanString(self.isAvailable()));
            s += line_prepend +  String.format("  Message: %s\n",toStringOrNone(self.getMessage()));
            
            for (SupportedQueryType q : self.getSupportedQueries()) {
                s += toText(q, line_prepend + STD_INDENT);
            }
            return s;            
        }

        if (obj instanceof CollectionInformationResponse) {
            CollectionInformationResponse self = (CollectionInformationResponse) obj;
            // s is populated by MessageType match already.
            
            s += line_prepend + String.format("Contains %s Collection Informations\n", self.getCollections().size());
            for(CollectionRecordType crt : self.getCollections()) {
                s += toText(crt, line_prepend + STD_INDENT);
            }
            return s;
        }
        
        if (obj instanceof CollectionRecordType) {
            CollectionRecordType self = (CollectionRecordType) obj;

            s = line_prepend + "=== Data Collection Information ===\n";
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            s += line_prepend + String.format("  Collection Type: %s\n", self.getCollectionType().name());
            s += line_prepend + String.format("  Available: %s\n", booleanString(self.isAvailable()));
            s += line_prepend + String.format("  Collection Description: %s\n", toStringOrNone(self.getDescription()));
            if ((null != self.getCollectionVolume()) && (0 != BigInteger.ZERO.compareTo(self.getCollectionVolume()))) {
                s += line_prepend + String.format("  Volume: %s\n", self.getCollectionVolume());
            }
            if (self.getContentBindings().isEmpty()) { // All contents supported:
                s += line_prepend + String.format("  Supported Content: %s\n", "All");
            }        
            for ( ContentBindingIDType cb : self.getContentBindings()) {
                s += line_prepend + String.format("  Supported Content: %s\n", toText(cb, line_prepend + STD_INDENT));
            }
            for (ServiceContactInfoType sci : self.getPollingServices()) {
                String name = "Polling Service";
                s += line_prepend + STD_INDENT + String.format("=== %s Instance ===\n",name);
                s += toText(sci, line_prepend + STD_INDENT);
            }
            for ( PushMethodType pm : self.getPushMethods()) {
                s += toText(pm, (line_prepend + STD_INDENT));
            }
            for (InboxServiceBindingsType isb: self.getReceivingInboxServices()) {
                s += toText(isb, line_prepend + STD_INDENT);
            }
            s += line_prepend + "==================================\n\n";
            return s;
        }
        
        if (obj instanceof PushMethodType) {
            PushMethodType self = (PushMethodType) obj;

            s = line_prepend + "=== Push Method ===\n";
            s += line_prepend + String.format("  Protocol Binding: %s\n", toStringOrNone(self.getProtocolBinding()));
            for (String mb : self.getMessageBindings()) {
                s += line_prepend +  String.format("  Message Binding: %s\n", mb);                
            }
            return s;            
        }
                
        if (obj instanceof InboxServiceBindingsType) {
            InboxServiceBindingsType self = (InboxServiceBindingsType)obj;
            
            s = line_prepend + "=== Receiving Inbox Service ===\n";
            s += line_prepend + String.format("  Protocol Binding: %s\n", toStringOrNone(self.getProtocolBinding()));
            s += line_prepend + String.format("  Address: %s\n", toStringOrNone(self.getAddress()));
            for (String mb : self.getMessageBindings()) {
                s += line_prepend +  String.format("  Message Binding: %s\n", mb);
            }
            if (self.getContentBindings().isEmpty()) {
                s += line_prepend + "  Supported Contents: All\n";
            }            
            for (ContentBindingIDType cb : self.getContentBindings())
                s += line_prepend + String.format("  Supported Content: %s\n", toText(cb,""));
            return s;            
        }

        if (obj instanceof PollRequest) {
            PollRequest self = (PollRequest)obj;
            
            /* s will be populated by MessageType match above */            
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            if (null !=self.getSubscriptionID()) {
                s += line_prepend + String.format("  Subscription ID: %s\n", toStringOrNone(self.getSubscriptionID()));
            }
            if (null != self.getExclusiveBeginTimestamp().toXMLFormat()) {
                s += line_prepend + String.format("  Excl. Begin TS Label: %s\n", self.getExclusiveBeginTimestamp().toXMLFormat());
            }
            if (null != self.getInclusiveEndTimestamp().toXMLFormat()) {
                s += line_prepend + String.format("  Incl. End TS Label: %s\n", self.getInclusiveEndTimestamp().toXMLFormat());
            }
            if (null != self.getPollParameters()) {
                s += toText( self.getPollParameters() , line_prepend + STD_INDENT);
            }

            return s;            
        }
        
        if (obj instanceof PollResponse) {
            PollResponse self = (PollResponse)obj;
            
            /* s will be populated by MessageType match */
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            s += line_prepend + String.format("  More: %s\n", booleanString(self.isMore()));
            s += line_prepend + String.format("  Result ID: %s\n", toStringOrNone(self.getResultId()));
            if (null != self.getResultPartNumber()) {
                s += line_prepend + String.format("  Result Part Num: %s\n", self.getResultPartNumber());
            }
            if (null != self.getRecordCount()) {
                s += toText(self.getRecordCount(), line_prepend + STD_INDENT);
            }
            if (null != self.getSubscriptionID()) {
                s += line_prepend + String.format("  Subscription ID: %s\n", self.getSubscriptionID());
            }                        
            if (null != self.getMessage()) {
                s += line_prepend + String.format("  Message: %s\n", self.getMessage());
            }            
            if (null != self.getExclusiveBeginTimestamp()) {
                s += line_prepend + String.format("  Excl. Begin TS Label: %s\n", self.getExclusiveBeginTimestamp().toXMLFormat());
            }
            if (null != self.getInclusiveEndTimestamp()) {
                s += line_prepend + String.format("  Incl. End TS Label: %s\n", self.getInclusiveEndTimestamp().toXMLFormat());
            }                        
            for (ContentBlock cb : self.getContentBlocks()) {
                s += toText(cb, line_prepend + STD_INDENT);
            }
            return s;            
        }
        
        if (obj instanceof StatusMessage) {
            StatusMessage self = (StatusMessage)obj;
            
            /* s will be populated by MessageType match */
            s += line_prepend + String.format("Status Type: %s\n", toStringOrNone(self.getStatusType()));
                        
            StatusDetailType sdt = self.getStatusDetail();
            if (null != sdt) {
                for (StatusDetailDetailType sddt : sdt.getDetails()) {
            /* TODO: On the Python side the below is the raw content.
                libtaxii stores the content internally as a string. On the Java side the content is
                a list of objects (see AnyMixedContentType). So to really get the
                value would require marshaling the content - an expensive operation
                that requires a TaxiiXml object with the proper JAXB context.
                i.e. It's a pain to do. Skip it for now.
            */
                    s += line_prepend + String.format("Status Detail: %s = %s\n", sddt.getName(), sddt.getContent());
                }
            }
                
            if (null != self.getMessage()) {
                s += line_prepend + String.format("Message: %s\n", toStringOrNone(self.getMessage()));
            }
            return s;            
        }
        
        if (obj instanceof InboxMessage) {
            InboxMessage self = (InboxMessage)obj;
            
            /* s is populated by MessageType match */            
            if (null != self.getResultId()) {
                s += line_prepend + String.format("  Result ID: %s\n", toStringOrNone(self.getResultId()));
            }
            for (String dcn : self.getDestinationCollectionNames()) {
                s += line_prepend + String.format("  Destination Collection Name: %s\n", toStringOrNone(dcn));
            }
            s += line_prepend + String.format("  Message: %s\n", toStringOrNone(self.getMessage()));
            if (null != self.getSourceSubscription()) {
                s += toText(self.getSourceSubscription(), line_prepend + STD_INDENT);
            }
            if (null != self.getRecordCount()) {
                s += toText(self.getRecordCount(), line_prepend + STD_INDENT);
            }
       
            s += line_prepend + String.format("  Message has %s Content Blocks\n", self.getContentBlocks().size());
            
            for (ContentBlock cb : self.getContentBlocks()) {
                s += toText(cb, line_prepend + STD_INDENT);
            }
            return s;            
        }
        
        if (obj instanceof SourceSubscriptionType) { // Python libtaxii calls this SubscriptionInformation
            SourceSubscriptionType self = (SourceSubscriptionType)obj;
            
            s = line_prepend + "=== Source Subscription ===\n";
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            s += line_prepend + String.format("  Subscription ID: %s\n", toStringOrNone(self.getSubscriptionID()));

            if (null != self.getExclusiveBeginTimestamp()) {
                s += line_prepend + String.format("  Excl. Begin TS Label: %s\n", self.getExclusiveBeginTimestamp().toXMLFormat());
            } else {
                s += line_prepend + String.format("  Excl. Begin TS Label: %s\n", "None");
            }

            if (null != self.getInclusiveEndTimestamp()) {
                s += line_prepend + String.format("  Incl. End TS Label: %s\n", self.getInclusiveEndTimestamp());
                        } else {
                s += line_prepend + String.format("  Incl. End TS Label: %s\n", "None");
            }
            return s;            
        }
        
        if (obj instanceof SubscriptionManagementRequest) { // Python libtaxii calls this ManageCollectionSubscriptionRequest
            SubscriptionManagementRequest self = (SubscriptionManagementRequest) obj;
            
            /* s will be prepopulated by MessageType match */
            CollectionActionEnum action = self.getAction();                
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            s += line_prepend + String.format("  Action: %s\n", toStringOrNone(action.name()));                
            s += line_prepend + String.format("  Subscription ID: %s\n", toStringOrNone(self.getSubscriptionID()));
        
            if  (CollectionActionEnum.SUBSCRIBE == action) {
                s += toText(self.getSubscriptionParameters(), line_prepend + STD_INDENT);
            }

            if ((CollectionActionEnum.SUBSCRIBE == action) && (null != self.getPushParameters())) {
                s += toText(self.getPushParameters(), line_prepend + STD_INDENT);
            }
            return s;
            
        }
        
        if (obj instanceof SubscriptionManagementResponse) { // Python libtaxii calls this ManageCollectionSubscriptionResponse
            SubscriptionManagementResponse self = (SubscriptionManagementResponse)obj;
            
            /* s will be prepopulated by MessageType match */
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            s += line_prepend + String.format("  Message: %s\n", toStringOrNone(self.getMessage()));
            for (SubscriptionRecordType srt: self.getSubscriptions()) {
                s += toText(srt, line_prepend + STD_INDENT);
            }
            return s;            
        }
        
        if (obj instanceof SubscriptionRecordType) { // Python libtaxii calls this SubscriptionInstance
            SubscriptionRecordType self = (SubscriptionRecordType)obj;

            s = line_prepend + "=== Subscription Instance ===\n";
            s += line_prepend + String.format("  Status: %s\n", self.getStatus().name());
            s += line_prepend + String.format("  Subscription ID: %s\n", toStringOrNone(self.getSubscriptionID()));
            if (null != self.getSubscriptionParameters()) {
                s += toText(self.getSubscriptionParameters(), line_prepend + STD_INDENT);
            }
            if (null != self.getPushParameters()) {
                s += toText(self.getPushParameters(), line_prepend + STD_INDENT);
            }
            for (ServiceContactInfoType sci : self.getPollInstances()) {
                String name = "Poll";
                s += line_prepend + STD_INDENT + String.format("=== %s Instance ===\n",name);                
                s += toText(sci, line_prepend + STD_INDENT);
            }
            return s;        
        }
        
        if (obj instanceof ServiceContactInfoType) {
            ServiceContactInfoType self = (ServiceContactInfoType)obj;
            
            s += line_prepend + String.format("  Protocol Binding: %s\n", toStringOrNone(self.getProtocolBinding()));
            s += line_prepend + String.format("  Address: %s\n", toStringOrNone(self.getAddress()));
            for (String mb : self.getMessageBindings()) {
                s += line_prepend +  String.format("  Message Binding: %s\n", mb);
            } 
            return s;
        }
        
        if (obj instanceof PollFulfillment){
            PollFulfillment self = (PollFulfillment)obj;
            
            /* s will be populated by MessageType match */
            s += line_prepend + String.format("  Collection Name: %s\n", toStringOrNone(self.getCollectionName()));
            s += line_prepend + String.format("  Result ID: %s\n", toStringOrNone(self.getResultId()));
            s += line_prepend + String.format("  Result Part Number: %s\n", toStringOrNone(self.getResultPartNumber()));
            return s;            
        }
                           
        if (s.isEmpty()) {
            s = "Sorry, I do not know how to render a " + obj.getClass().getName();
        }
        return s;        
    }
    
    private static String genericParameters(Object obj, String name, String line_prepend) {
        StringBuilder sb = new StringBuilder();            
        sb.append(line_prepend).append(String.format("=== %s ===\n", name));
        
        /* 
         * Use reflection to get the common fields.
         * This would be easier in Groovy.
         */
        try {
            // Content Bindings
            Method getCB = obj.getClass().getMethod("getContentBindings");
            Object rawCBs = getCB.invoke(obj);
            List<ContentBindingIDType> cbList = (List)rawCBs;
            for (ContentBindingIDType cb : cbList) {
                sb.append(String.format("  Content Binding: %s\n", toText(cb, "")));
            }
            
            // Query
            Method getQ = obj.getClass().getMethod("getQuery");
            Object rawQ = getQ.invoke(obj);
            QueryType q = (QueryType)rawQ;
            if (null != q) {
                sb.append(toText(q,line_prepend + STD_INDENT));
            }                        
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(PythonTextOutput.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(PythonTextOutput.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PythonTextOutput.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(PythonTextOutput.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(PythonTextOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sb.toString();
    }
    
    /**
     * Implement my own join() method. Java 8 does this out of the box, and
     * many libraries implement it too (Apache's StringUtil). But in an attempt
     * to reduce dependencies and support as many versions of Java as possible,
     * I'll write my own here.
     * 
     * @return 
     */
    private static String join(String separator, List<String> list) {
        if (null == list) return null;
        
        int size = list.size();        
        if (0 == size) return null;
        StringBuilder sb = new StringBuilder();
        
        sb.append(list.get(0));
        for(int x = 1; x < size; ++x) {
            sb.append(separator).append(list.get(x));
        }
        
        return sb.toString();
    }

    /**
     * Turn a Boolean into a Python string representation.
     * 
     * @ return "None", "True", or "False".
     */
    private static String booleanString(Boolean value) {
        if (null == value) return "None";        
        return value ? "True" : "False";
    }
    
    private static String toStringOrNone(Object obj) {
        if (null == obj) return "None";
        return obj.toString();
    }
    
}
