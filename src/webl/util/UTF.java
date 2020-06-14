package webl.util;

public class UTF {

    // Convert to UTF byte array
    public final static byte[] AsBytes(String s) {
    	int len = s.length();
    	int ulen = 0;	
    	for(int i = 0; i < len; i++) {
    		int c = s.charAt(i);
    		if ((c >= 0x0001) && (c <= 0x007F)) ulen++;
    		else if(c > 0x07FF) ulen += 3;
    		else ulen += 2;
    	}
    	
    	byte R[] = new byte[ulen];
    	int j = 0;
    	for (int i = 0 ; i < len ; i++) {
    		int c = s.charAt(i);
    		if ((c >= 0x0001) && (c <= 0x007F)) 
    		    R[j++] = (byte)c;
    		else if (c > 0x07FF) {
    			R[j++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
    			R[j++] = (byte)(0x80 | ((c >>  6) & 0x3F));
    			R[j++] = (byte)(0x80 | ((c >>  0) & 0x3F));
    		} else {
    			R[j++] = (byte)(0xC0 | ((c >>  6) & 0x1F));
    			R[j++] = (byte)(0x80 | ((c >>  0) & 0x3F));
    		}
    	}
    	return(R);
    }

    public final static int AsBytes(int c, byte[] R) {
    	int j = 0;
		if ((c >= 0x0001) && (c <= 0x007F)) 
		    R[j++] = (byte)c;
		else if (c > 0x07FF) {
			R[j++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
			R[j++] = (byte)(0x80 | ((c >>  6) & 0x3F));
			R[j++] = (byte)(0x80 | ((c >>  0) & 0x3F));
		} else {
			R[j++] = (byte)(0xC0 | ((c >>  6) & 0x1F));
			R[j++] = (byte)(0x80 | ((c >>  0) & 0x3F));
		}
    	return j;
    }
    
    public static String AsString(byte data[]) {
        int len = data.length;
        char str[] = new char[len];
    	int count = 0;
    	int strlen = 0;
    	
    	for(int i = 0; i < len && count < len; ) {
    	    int c = data[i++];
    	    
    	    int char2, char3;
	    	switch (c >> 4) { 
            	case 0:
            	case 1:
            	case 2:
            	case 3:
            	case 4:
            	case 5:
            	case 6:
            	case 7:
    		    	// 0xxxxxxx
    		    	count++;
    		    	str[strlen++] = (char)c;
    		    	break;
            	case 12:
            	case 13:
    		    	// 110x xxxx   10xx xxxx
    		    	count += 2;
    		    	if (count > data.length)
    		    	    return null;		  
    		    	char2 = data[i++];
    		    	if ((char2 & 0xC0) != 0x80)
    		    	    return null;		  
    		    	str[strlen++] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
    		    	break;
            	case 14:
    		    	// 1110 xxxx  10xx xxxx  10xx xxxx
    		    	count += 3;
    		    	if (count > data.length)
    		    	    return null;		  
    		    	char2 = data[i++];
    		    	char3 = data[i++];
    		    	if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
    					return null;		  
    		    	str[strlen++] = (char)(((c & 0x0F) << 12) |
    					   	((char2 & 0x3F) << 6) |
    					   	((char3 & 0x3F) << 0));
    		    	break;
            	default:
    		    	// 10xx xxxx,  1111 xxxx
    		    	return null;		  
			}
    	}
        return new String(str, 0, strlen);
    }
}