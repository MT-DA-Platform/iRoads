package com.vatichub.obd2.api;

import com.vatichub.obd2.bean.OBD2Event;

public interface OBD2EventListener {

    void receiveOBD2Event(OBD2Event e);
}