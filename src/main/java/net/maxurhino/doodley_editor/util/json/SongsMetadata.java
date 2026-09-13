package net.maxurhino.doodley_editor.util.json;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class SongsMetadata {
    private List<Album> Albums;

    public List<Album> getAlbums() {
        return Albums;
    }

    public SongsMetadata setAlbums(List<Album> Albums) {
        this.Albums = Albums;
        return this;
    }

    public static class Album {
        private String Id;
        @Nullable private String Name;
        @Nullable private String Artist;
        private ResourceInfo ResourceInfo;

        public static class ResourceInfo {
            private String Author;
            private String Album;

            public String getAuthor() {
                return Author;
            }

            public void setAuthor(String author) {
                Author = author;
            }

            public String getAlbum() {
                return Album;
            }

            public void setAlbum(String album) {
                Album = album;
            }
        }

        private List<Song> Songs;

        public static class Song {
            @Nullable private Integer Num;
            private String Id;
            @Nullable private String Name;
            private String FileName;

            public @Nullable Integer getNum() {
                return Num;
            }

            public void setNum(@Nullable Integer num) {
                Num = num;
            }

            public String getId() {
                return Id;
            }

            public void setId(String id) {
                Id = id;
            }

            public @Nullable String getName() {
                return Name;
            }

            public void setName(@Nullable String name) {
                Name = name;
            }

            public String getFileName() {
                return FileName;
            }

            public void setFileName(String fileName) {
                FileName = fileName;
            }
        }

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public @Nullable String getName() {
            return Name;
        }

        public void setName(@Nullable String name) {
            Name = name;
        }

        public @Nullable String getArtist() {
            return Artist;
        }

        public void setArtist(@Nullable String artist) {
            Artist = artist;
        }

        public ResourceInfo getResourceInfo() {
            return ResourceInfo;
        }

        public void setResourceInfo(ResourceInfo resourceInfo) {
            ResourceInfo = resourceInfo;
        }

        public List<Song> getSongs() {
            return Songs;
        }

        public void setSongs(List<Song> songs) {
            Songs = songs;
        }
    }
}
