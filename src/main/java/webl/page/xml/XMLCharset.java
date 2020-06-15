package webl.page.xml;

import java.util.*;

public class XMLCharset
{

    static private BitSet namechars;
    static private BitSet namestartchars;
    
    static {
        namechars = new BitSet(0x10000);
        Letter(namechars);
        Digit(namechars);
        bit(namechars, '.');
        bit(namechars, '-');
        bit(namechars, '_');
        bit(namechars, ':');
        CombiningChar(namechars);
        Extender(namechars);
        
        namestartchars = new BitSet(0x10000);
        Letter(namestartchars);
        bit(namestartchars, '_');
        bit(namestartchars, ':');
    }
    
    static public boolean NameChar(int ch) {
        return namechars.get(ch);
    }
    
    static public boolean NameStartChar(int ch) {
        return namestartchars.get(ch);
    }
    static private void bit(BitSet b, int p) {
        b.set(p);
    }
    
    static private void range(BitSet b, int beg, int end) {
        for (int i = beg; i <= end; i++)
            b.set(i);
    }
    
    static private void Letter(BitSet b) {
        BaseChar(b);
        Ideographic(b);
    }
    
    static private void BaseChar(BitSet b) {
        range(b, 0x0041, 0x005A); 
        range(b, 0x0061, 0x007A); 
        range(b, 0x00C0, 0x00D6); 
        range(b, 0x00D8, 0x00F6); 
        range(b, 0x00F8, 0x00FF); 
        range(b, 0x0100, 0x0131); 
        range(b, 0x0134, 0x013E); 
        range(b, 0x0141, 0x0148); 
        range(b, 0x014A, 0x017E); 
        range(b, 0x0180, 0x01C3); 
        range(b, 0x01CD, 0x01F0); 
        range(b, 0x01F4, 0x01F5); 
        range(b, 0x01FA, 0x0217); 
        range(b, 0x0250, 0x02A8); 
        range(b, 0x02BB, 0x02C1); 
        bit(b, 0x0386);
        range(b, 0x0388, 0x038A); 
        bit(b, 0x038C); 
        range(b, 0x038E, 0x03A1); 
        range(b, 0x03A3, 0x03CE); 
        range(b, 0x03D0, 0x03D6); 
        bit(b, 0x03DA); 
        bit(b, 0x03DC); 
        bit(b, 0x03DE); 
        bit(b, 0x03E0); 
        range(b, 0x03E2, 0x03F3); 
        range(b, 0x0401, 0x040C); 
        range(b, 0x040E, 0x044F); 
        range(b, 0x0451, 0x045C); 
        range(b, 0x045E, 0x0481); 
        range(b, 0x0490, 0x04C4); 
        range(b, 0x04C7, 0x04C8); 
        range(b, 0x04CB, 0x04CC); 
        range(b, 0x04D0, 0x04EB); 
        range(b, 0x04EE, 0x04F5); 
        range(b, 0x04F8, 0x04F9); 
        range(b, 0x0531, 0x0556); 
        bit(b, 0x0559); 
        range(b, 0x0561, 0x0586); 
        range(b, 0x05D0, 0x05EA); 
        range(b, 0x05F0, 0x05F2); 
        range(b, 0x0621, 0x063A); 
        range(b, 0x0641, 0x064A); 
        range(b, 0x0671, 0x06B7); 
        range(b, 0x06BA, 0x06BE); 
        range(b, 0x06C0, 0x06CE); 
        range(b, 0x06D0, 0x06D3); 
        bit(b, 0x06D5); 
        range(b, 0x06E5, 0x06E6); 
        range(b, 0x0905, 0x0939); 
        bit(b, 0x093D); 
        range(b, 0x0958, 0x0961); 
        range(b, 0x0985, 0x098C); 
        range(b, 0x098F, 0x0990); 
        range(b, 0x0993, 0x09A8); 
        range(b, 0x09AA, 0x09B0); 
        bit(b, 0x09B2); 
        range(b, 0x09B6, 0x09B9); 
        range(b, 0x09DC, 0x09DD); 
        range(b, 0x09DF, 0x09E1); 
        range(b, 0x09F0, 0x09F1); 
        range(b, 0x0A05, 0x0A0A); 
        range(b, 0x0A0F, 0x0A10); 
        range(b, 0x0A13, 0x0A28); 
        range(b, 0x0A2A, 0x0A30); 
        range(b, 0x0A32, 0x0A33); 
        range(b, 0x0A35, 0x0A36); 
        range(b, 0x0A38, 0x0A39); 
        range(b, 0x0A59, 0x0A5C); 
        bit(b, 0x0A5E); 
        range(b, 0x0A72, 0x0A74); 
        range(b, 0x0A85, 0x0A8B); 
        bit(b, 0x0A8D); 
        range(b, 0x0A8F, 0x0A91); 
        range(b, 0x0A93, 0x0AA8); 
        range(b, 0x0AAA, 0x0AB0); 
        range(b, 0x0AB2, 0x0AB3); 
        range(b, 0x0AB5, 0x0AB9); 
        bit(b, 0x0ABD); 
        bit(b, 0x0AE0); 
        range(b, 0x0B05, 0x0B0C); 
        range(b, 0x0B0F, 0x0B10); 
        range(b, 0x0B13, 0x0B28); 
        range(b, 0x0B2A, 0x0B30); 
        range(b, 0x0B32, 0x0B33); 
        range(b, 0x0B36, 0x0B39); 
        bit(b, 0x0B3D); 
        range(b, 0x0B5C, 0x0B5D); 
        range(b, 0x0B5F, 0x0B61); 
        range(b, 0x0B85, 0x0B8A); 
        range(b, 0x0B8E, 0x0B90); 
        range(b, 0x0B92, 0x0B95); 
        range(b, 0x0B99, 0x0B9A); 
        bit(b, 0x0B9C); 
        range(b, 0x0B9E, 0x0B9F); 
        range(b, 0x0BA3, 0x0BA4); 
        range(b, 0x0BA8, 0x0BAA); 
        range(b, 0x0BAE, 0x0BB5); 
        range(b, 0x0BB7, 0x0BB9); 
        range(b, 0x0C05, 0x0C0C); 
        range(b, 0x0C0E, 0x0C10); 
        range(b, 0x0C12, 0x0C28); 
        range(b, 0x0C2A, 0x0C33); 
        range(b, 0x0C35, 0x0C39); 
        range(b, 0x0C60, 0x0C61); 
        range(b, 0x0C85, 0x0C8C); 
        range(b, 0x0C8E, 0x0C90); 
        range(b, 0x0C92, 0x0CA8); 
        range(b, 0x0CAA, 0x0CB3); 
        range(b, 0x0CB5, 0x0CB9); 
        bit(b, 0x0CDE); 
        range(b, 0x0CE0, 0x0CE1); 
        range(b, 0x0D05, 0x0D0C); 
        range(b, 0x0D0E, 0x0D10); 
        range(b, 0x0D12, 0x0D28); 
        range(b, 0x0D2A, 0x0D39); 
        range(b, 0x0D60, 0x0D61); 
        range(b, 0x0E01, 0x0E2E); 
        bit(b, 0x0E30); 
        range(b, 0x0E32, 0x0E33); 
        range(b, 0x0E40, 0x0E45); 
        range(b, 0x0E81, 0x0E82); 
        bit(b, 0x0E84); 
        range(b, 0x0E87, 0x0E88); 
        bit(b, 0x0E8A); 
        bit(b, 0x0E8D); 
        range(b, 0x0E94, 0x0E97); 
        range(b, 0x0E99, 0x0E9F); 
        range(b, 0x0EA1, 0x0EA3); 
        bit(b, 0x0EA5); 
        bit(b, 0x0EA7); 
        range(b, 0x0EAA, 0x0EAB); 
        range(b, 0x0EAD, 0x0EAE); 
        bit(b, 0x0EB0); 
        range(b, 0x0EB2, 0x0EB3); 
        bit(b, 0x0EBD); 
        range(b, 0x0EC0, 0x0EC4); 
        range(b, 0x0F40, 0x0F47); 
        range(b, 0x0F49, 0x0F69); 
        range(b, 0x10A0, 0x10C5); 
        range(b, 0x10D0, 0x10F6); 
        bit(b, 0x1100); 
        range(b, 0x1102, 0x1103); 
        range(b, 0x1105, 0x1107); 
        bit(b, 0x1109); 
        range(b, 0x110B, 0x110C); 
        range(b, 0x110E, 0x1112); 
        bit(b, 0x113C); 
        bit(b, 0x113E); 
        bit(b, 0x1140); 
        bit(b, 0x114C); 
        bit(b, 0x114E);
        bit(b, 0x1150);
        range(b, 0x1154, 0x1155); 
        bit(b, 0x1159); 
        range(b, 0x115F, 0x1161); 
        bit(b, 0x1163); 
        bit(b, 0x1165); 
        bit(b, 0x1167); 
        bit(b, 0x1169); 
        range(b, 0x116D, 0x116E); 
        range(b, 0x1172, 0x1173); 
        bit(b, 0x1175); 
        bit(b, 0x119E); 
        bit(b, 0x11A8); 
        bit(b, 0x11AB); 
        range(b, 0x11AE, 0x11AF); 
        range(b, 0x11B7, 0x11B8); 
        bit(b, 0x11BA);
        range(b, 0x11BC, 0x11C2); 
        bit(b, 0x11EB); 
        bit(b, 0x11F0); 
        bit(b, 0x11F9); 
        range(b, 0x1E00, 0x1E9B); 
        range(b, 0x1EA0, 0x1EF9); 
        range(b, 0x1F00, 0x1F15); 
        range(b, 0x1F18, 0x1F1D); 
        range(b, 0x1F20, 0x1F45); 
        range(b, 0x1F48, 0x1F4D); 
        range(b, 0x1F50, 0x1F57); 
        bit(b, 0x1F59); 
        bit(b, 0x1F5B); 
        bit(b, 0x1F5D); 
        range(b, 0x1F5F, 0x1F7D); 
        range(b, 0x1F80, 0x1FB4); 
        range(b, 0x1FB6, 0x1FBC); 
        bit(b, 0x1FBE); 
        range(b, 0x1FC2, 0x1FC4); 
        range(b, 0x1FC6, 0x1FCC); 
        range(b, 0x1FD0, 0x1FD3); 
        range(b, 0x1FD6, 0x1FDB); 
        range(b, 0x1FE0, 0x1FEC); 
        range(b, 0x1FF2, 0x1FF4); 
        range(b, 0x1FF6, 0x1FFC); 
        bit(b, 0x2126); 
        range(b, 0x212A, 0x212B); 
        bit(b, 0x212E); 
        range(b, 0x2180, 0x2182); 
        range(b, 0x3041, 0x3094); 
        range(b, 0x30A1, 0x30FA); 
        range(b, 0x3105, 0x312C); 
        range(b, 0xAC00, 0xD7A3);  
    }

    static private void Ideographic(BitSet b) {
        range(b, 0x4E00, 0x9FA5); 
        bit(b, 0x3007); 
        range(b, 0x3021, 0x3029); 
    }

    static private void CombiningChar(BitSet b) {
        range(b, 0x0300, 0x0345); 
        range(b, 0x0360, 0x0361); 
        range(b, 0x0483, 0x0486); 
        range(b, 0x0591, 0x05A1); 
        range(b, 0x05A3, 0x05B9); 
        range(b, 0x05BB, 0x05BD); 
        bit(b, 0x05BF); 
        range(b, 0x05C1, 0x05C2); 
        bit(b, 0x05C4); 
        range(b, 0x064B, 0x0652); 
        bit(b, 0x0670); 
        range(b, 0x06D6, 0x06DC); 
        range(b, 0x06DD, 0x06DF); 
        range(b, 0x06E0, 0x06E4); 
        range(b, 0x06E7, 0x06E8); 
        range(b, 0x06EA, 0x06ED); 
        range(b, 0x0901, 0x0903); 
        bit(b, 0x093C); 
        range(b, 0x093E, 0x094C); 
        bit(b, 0x094D); 
        range(b, 0x0951, 0x0954); 
        range(b, 0x0962, 0x0963); 
        range(b, 0x0981, 0x0983); 
        bit(b, 0x09BC); 
        bit(b, 0x09BE); 
        bit(b, 0x09BF); 
        range(b, 0x09C0, 0x09C4); 
        range(b, 0x09C7, 0x09C8); 
        range(b, 0x09CB, 0x09CD); 
        bit(b, 0x09D7); 
        range(b, 0x09E2, 0x09E3); 
        bit(b, 0x0A02); 
        bit(b, 0x0A3C); 
        bit(b, 0x0A3E); 
        bit(b, 0x0A3F); 
        range(b, 0x0A40, 0x0A42); 
        range(b, 0x0A47, 0x0A48); 
        range(b, 0x0A4B, 0x0A4D); 
        range(b, 0x0A70, 0x0A71); 
        range(b, 0x0A81, 0x0A83); 
        bit(b, 0x0ABC); 
        range(b, 0x0ABE, 0x0AC5); 
        range(b, 0x0AC7, 0x0AC9); 
        range(b, 0x0ACB, 0x0ACD); 
        range(b, 0x0B01, 0x0B03); 
        bit(b, 0x0B3C); 
        range(b, 0x0B3E, 0x0B43); 
        range(b, 0x0B47, 0x0B48); 
        range(b, 0x0B4B, 0x0B4D); 
        range(b, 0x0B56, 0x0B57); 
        range(b, 0x0B82, 0x0B83); 
        range(b, 0x0BBE, 0x0BC2); 
        range(b, 0x0BC6, 0x0BC8); 
        range(b, 0x0BCA, 0x0BCD); 
        bit(b, 0x0BD7); 
        range(b, 0x0C01, 0x0C03); 
        range(b, 0x0C3E, 0x0C44); 
        range(b, 0x0C46, 0x0C48); 
        range(b, 0x0C4A, 0x0C4D); 
        range(b, 0x0C55, 0x0C56); 
        range(b, 0x0C82, 0x0C83); 
        range(b, 0x0CBE, 0x0CC4); 
        range(b, 0x0CC6, 0x0CC8); 
        range(b, 0x0CCA, 0x0CCD); 
        range(b, 0x0CD5, 0x0CD6); 
        range(b, 0x0D02, 0x0D03); 
        range(b, 0x0D3E, 0x0D43); 
        range(b, 0x0D46, 0x0D48); 
        range(b, 0x0D4A, 0x0D4D); 
        bit(b, 0x0D57); 
        bit(b, 0x0E31); 
        range(b, 0x0E34, 0x0E3A); 
        range(b, 0x0E47, 0x0E4E); 
        bit(b, 0x0EB1); 
        range(b, 0x0EB4, 0x0EB9); 
        range(b, 0x0EBB, 0x0EBC); 
        range(b, 0x0EC8, 0x0ECD); 
        range(b, 0x0F18, 0x0F19); 
        bit(b, 0x0F35); 
        bit(b, 0x0F37); 
        bit(b, 0x0F39); 
        bit(b, 0x0F3E); 
        bit(b, 0x0F3F); 
        range(b, 0x0F71, 0x0F84); 
        range(b, 0x0F86, 0x0F8B); 
        range(b, 0x0F90, 0x0F95); 
        bit(b, 0x0F97); 
        range(b, 0x0F99, 0x0FAD); 
        range(b, 0x0FB1, 0x0FB7); 
        bit(b, 0x0FB9); 
        range(b, 0x20D0, 0x20DC); 
        bit(b, 0x20E1); 
        range(b, 0x302A, 0x302F); 
        bit(b, 0x3099); 
        bit(b, 0x309A);  
    }

    static private void Digit(BitSet b) {  
        range(b, 0x0030, 0x0039); 
        range(b, 0x0660, 0x0669); 
        range(b, 0x06F0, 0x06F9); 
        range(b, 0x0966, 0x096F); 
        range(b, 0x09E6, 0x09EF); 
        range(b, 0x0A66, 0x0A6F); 
        range(b, 0x0AE6, 0x0AEF); 
        range(b, 0x0B66, 0x0B6F); 
        range(b, 0x0BE7, 0x0BEF); 
        range(b, 0x0C66, 0x0C6F); 
        range(b, 0x0CE6, 0x0CEF); 
        range(b, 0x0D66, 0x0D6F); 
        range(b, 0x0E50, 0x0E59); 
        range(b, 0x0ED0, 0x0ED9); 
        range(b, 0x0F20, 0x0F29);  
    }

    static private void Extender(BitSet b) {
        bit(b, 0x00B7);
        bit(b, 0x02D0); 
        bit(b, 0x02D1); 
        bit(b, 0x0387); 
        bit(b, 0x0640); 
        bit(b, 0x0E46); 
        bit(b, 0x0EC6); 
        bit(b, 0x3005); 
        range(b, 0x3031, 0x3035); 
        range(b, 0x309D, 0x309E); 
        range(b, 0x30FC, 0x30FE);  
    }


}