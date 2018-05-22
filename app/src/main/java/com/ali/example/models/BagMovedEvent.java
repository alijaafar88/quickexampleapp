package com.ali.example.models;

/**
 * Immutable pojo representing one bag movement event
 *
 * Created by pdv on 2/28/17.
 */

public class BagMovedEvent {

    public final BagStatus origin;
    public final BagStatus result;
    public final Team team;

    public BagMovedEvent(BagStatus origin, BagStatus result, Team team) {
        this.origin = origin;
        this.result = result;
        this.team = team;
    }

}
