package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        //List<String> playlists = List.of("avion", "bruit", "burger", "caillou", "cheminee", "concentration", "deviant", "funky", "kebab", "marche", "nuage", "otage", "pizza", "rose", "satellite", "tacos");
        List<String> playlists = List.of("funky");
        final int size = playlists.size();
        TaskThread[] tasks = new TaskThread[playlists.size()];

        for (int i=0;i<size;++i) {
            tasks[i] = new TaskThread();
            tasks[i].setName(playlists.get(i));
            tasks[i].start();
        }

        try {
            for (int i=0;i<size;++i) {
                tasks[i].join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

