package com.yatzy.model;

/**
 * Interface representing any entity that exists in the game world.
 * Provides a contract for displaying game entities.
 */
public interface GameEntity {
    
    /**
     * Gets the name of this game entity.
     * @return the entity name
     */
    String getName();
    
    /**
     * Displays information about this game entity.
     */
    void display();
}
