/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.geekstakulus.json.node;

import java.util.*;
import com.geekstakulus.json.analysis.*;

public final class AEmptyMember extends PMember
{


    public AEmptyMember (
    )
    {
    }

    public Object clone()
    {
        return new AEmptyMember (
        );
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAEmptyMember(this);
    }


    public String toString()
    {
        return ""
        ;
    }

    void removeChild(Node child)
    {
    }

    void replaceChild(Node oldChild, Node newChild)
    {
    }

}