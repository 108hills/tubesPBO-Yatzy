package com.yatzy.model;

/**
 * Interface buat objek-objek apa aja yang ada di dalem game.
 * Nyediain aturan wajib buat nampilin info entitas game.
 */
public interface GameEntity {
    
    /**
     * Ngambil nama dari entitas ini.
     * @return nama entitasnya
     */
    String getName();
    
    /**
     * Nampilin info atau status entitas game ini.
     */
    void display();
}
