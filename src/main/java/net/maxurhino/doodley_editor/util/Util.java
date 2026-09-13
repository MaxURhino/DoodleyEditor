package net.maxurhino.doodley_editor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.maxurhino.doodley_editor.util.json.SongsMetadata;
import net.maxurhino.doodley_editor.util.multiples.pairs.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class Util {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T loadJsonFromPath(Path path, Class<T> tClass) {
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            return GSON.fromJson(reader, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Pair<SongsMetadata.Album, SongsMetadata.Album.Song> getAlbumAndSong(SongsMetadata songsMetadata, String albumId, String songId) {
        for (SongsMetadata.Album album : songsMetadata.getAlbums()) {
            if (album.getId().equals(albumId)) {
                System.out.printf("Found album with id \"%s\", name: \"%s\".%n", albumId, album.getName());
                for (SongsMetadata.Album.Song song : album.getSongs()) {
                    if (song.getId().equals(songId)) {
                        System.out.printf("Found song with id \"%s\", name: \"%s\".%n", songId, song.getName());
                        return new Pair<>(album, song);
                    }
                }
                break;
            }
        }

        System.err.printf("Couldn't find a song with album id \"%s\" and song id \"%s\"%n", albumId, songId);

        return new Pair<>(new SongsMetadata.Album(), new SongsMetadata.Album.Song());
    }

    public static Pair<SongsMetadata.Album, SongsMetadata.Album.Song> getAlbumAndSong(SongsMetadata songsMetadata, String albumId, int songNum) {
        for (SongsMetadata.Album album : songsMetadata.getAlbums()) {
            if (album.getId().equals(albumId)) {
                System.out.printf("Found album with id \"%s\", name: \"%s\".%n", albumId, album.getName());
                for (SongsMetadata.Album.Song song : album.getSongs()) {
                    if (song.getNum() != null && song.getNum() == songNum) {
                        System.out.printf("Found song with num \"%s\", name: \"%s\".%n", songNum, song.getName());
                        return new Pair<>(album, song);
                    }
                }
            }
        }

        System.err.printf("Couldn't find a song with album id \"%s\" and song num \"%s\"%n", albumId, songNum);

        return new Pair<>(new SongsMetadata.Album(), new SongsMetadata.Album.Song());
    }

    public static float map01(float t, float min, float max) {
        return min + t * (max - min);
    }

    public static float map01Clamped(float t, float min, float max) {
        return map01(Math.clamp(t, 0f, 1f), min, max);
    }

    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        float t = (value - inMin) / (inMax - inMin);
        return map01(t, outMin, outMax);
    }

    public static float getEasedValue(float start, float end, float time, Function<Double, Double> ease) {
        float eased = (float) Math.clamp(ease.apply((double) time), 0, 1);
        return map01(eased, start, end);
    }
}
