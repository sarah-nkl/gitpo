package com.example.githubrepo.services;

/**
 * Created by sarah_neo on 22/02/2017.
 */

import com.example.githubrepo.services.event.BusEvent;
import com.example.githubrepo.services.event.BusEventMapping;
import com.squareup.otto.Bus;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.example.githubrepo.services.event.BusEvent.EventType;
import static com.example.githubrepo.services.event.BusEventMapping.*;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more efficient means
 * such as through injection directly into interested classes.
 */
@Singleton
public class BusProvider {

    final Bus bus;

    private HashMap<String, Object> busEventHashMap;
    private HashMap<Class, EventType[]> busEventZoneMapping;

    @Inject
    public BusProvider( Bus bus ) {
        busEventHashMap = new HashMap<String, Object>();
        busEventZoneMapping = BusEventMapping.getZones();

        this.bus = bus;
    }

    public void reset( Class mappingZone) {
        EventType[] types = busEventZoneMapping.get( mappingZone );

        for(EventType type: types){
            setHashMap( getType( type ));
        }
    }

    public void resetAll( ) {
        for (EventType eventType: EventType.values()){
            BusEvent event = null;

            try{
                event = getType(eventType);
            }catch(IllegalArgumentException e){  }

            if (event != null){
                setHashMap(event);
            }
        }
    }

    public void register( Object object ) {
        bus.register( object );
    }

    public void unregister( Object object ) {
        bus.unregister( object );
    }

    public void post( Object event ) {
        if (busEventZoneMapping.containsKey( event.getClass() )){
            reset( event.getClass() );
        }
        bus.post( event );
    }

    public void setEvent( BusEvent event ) {
        setHashMap( event );
    }

    public Object getEvent( EventType type ) {
        HashMap<String, Object> hashMap = busEventHashMap;
        if ( hashMap.containsKey( type.name() ) ) {
            return hashMap.get( type.name() );
        }

        BusEvent event = getType( type );
        setHashMap( event );
        return event;
    }

    private BusEvent getType( EventType type ) {
        return type.getEmptyEvent();
    }

    private void setHashMap( BusEvent event ) {
        busEventHashMap.put( event.getName(), event );
    }
}
