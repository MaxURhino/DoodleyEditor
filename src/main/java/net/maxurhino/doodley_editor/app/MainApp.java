package net.maxurhino.doodley_editor.app;

import net.maxurhino.doodley_editor.Window;
import net.maxurhino.doodley_editor.util.*;
import net.maxurhino.doodley_editor.util.color.ColorTheme;
import net.maxurhino.doodley_editor.util.commons.CommonColors;
import net.maxurhino.doodley_editor.util.enums.SlideState;
import net.maxurhino.doodley_editor.util.json.SongsMetadata;
import net.maxurhino.doodley_editor.util.movement.Easings;
import net.maxurhino.doodley_editor.util.multiples.pairs.Pair;
import net.maxurhino.doodley_editor.util.render.Renderer;
import net.maxurhino.doodley_editor.util.render.font.*;
import net.maxurhino.doodley_editor.util.render.font.Font;
import net.maxurhino.doodley_editor.util.render.texture.*;
import net.maxurhino.doodley_editor.util.sound.*;
import org.joml.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MainApp implements Runnable {
    public final Window window = new Window(
            1280,
            720,
            "Doodley Editor"
    );
    public final AudioContext audio = new AudioContext();
    public Sound music;

    public Renderer renderer;

    private float checkerboardFrame, musicToastFrame, frame;

    public FontFamily futuraFont;

    public SongsMetadata songsMetadata;

    private Pair<SongsMetadata.Album, SongsMetadata.Album.Song> albumAndSong;

    private final Map<String, Texture> images = new HashMap<>();

    boolean shouldWasteYourResourcesForPlayingMusicYouProbablyDontWantToHearLol = true;

    private SlideState slideState = SlideState.SLIDING_IN;
    private float musicPopupY;
    
    public ColorTheme theme;

    @Override
    public void run() {
        this.theme = CommonColors.DEFAULT;
        
        window.create();

        renderer = new Renderer(window);
        renderer.setClearColor(Color.WHITE);

        try {
            this.images.put(
                    "SVG_MusicPopup_Notes",
                    new Texture(Paths.image.SVG("MusicPopup/Notes"))
            );
            this.images.put(
                    "SVG_Pencil",
                    new Texture(Paths.image.SVG("Pencil"))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        audio.create();

        songsMetadata = Util.loadJsonFromPath(
                Paths.music("SongsMetadata.json"),
                SongsMetadata.class
        );

        String song;
        if (this.theme.name().equals(CommonColors.HALLOWEEN.name())) {
            song = "PMP";
        } else {
            song = "CEM";
        }

        this.albumAndSong = Util.getAlbumAndSong(
                songsMetadata,
                "DS1",
                song
        );

        SongsMetadata.Album.ResourceInfo resourceInfo = albumAndSong.getFirst().getResourceInfo();

        music = new Sound(Paths.music(resourceInfo.getAuthor(), resourceInfo.getAlbum(), albumAndSong.getSecond().getFileName()));
        music.setLooping(true);
        if (shouldWasteYourResourcesForPlayingMusicYouProbablyDontWantToHearLol) {
            music.play();
        }

        futuraFont = new FontFamily(
                "FuturaCyrillic",
                List.of("Bold", "Book", "Demi", "ExtraBold", "Heavy", "Light", "Medium"),
                Paths::font,
                32
        );

        checkerboardFrame = 0;
        musicToastFrame = 0;

        window.setOnFrame(this::renderFrame);

        while (window.running()) {
            window.poll();
            renderFrame();
        }

        renderer.destroy();
        window.destroy();
    }

    private void renderFrame() {
        float dt = window.getDeltaTime();

        frame += dt * 25f;

        checkerboardFrame += dt * 25f;

        musicToastFrame += dt * 50f;

        int frameIg = 75;

        if (((int)frame) == frameIg) {
            slideState = SlideState.SLIDING_IN;
        } else if ((int)frame < frameIg) {
            musicToastFrame = 0;
            slideState = SlideState.HIDDEN;
        }

        renderer.clear();

        renderer.pose().pushMatrix();

        int rectSize = 150;

        Vector2f scaleFactor = this.window.getScaleFactor();

        renderer.pose().scale(scaleFactor);

        renderBackground(rectSize);

        renderWindow(730, 410, "Title", "Subtitle", (w, h) -> {
            renderer.draw.rect(
                    new Vector2f(),
                    new Vector2f(w, h),
                    Color.DARK_GRAY
            );

            Font font = this.futuraFont.getStyle("Book");

            String text = "PLACEHOLDER TEXT, yet to be changed";

            renderer.draw.text(
                    font,
                    text,
                    new TextPlacement(
                            font,
                            text,
                            new Vector2f(),
                            new Vector2f(w, h)
                    ),
                    TextPlacement.Placement.SECOND,
                    TextPlacement.Placement.SECOND,
                    Color.WHITE
            );
        });

        renderMusicPopup();

        renderer.pose().popMatrix();
    }

    private void animateMusicPopup() {
        int hiddenY = -100;

        int whenToSwitchStateForTheFirstTime = 50;
        int whenToSwitchStateForTheSecondTime = 125;
        switch (slideState) {
            case SLIDING_IN  -> {
                musicPopupY = Util.getEasedValue(hiddenY, 0, this.musicToastFrame / whenToSwitchStateForTheFirstTime, Easings::easeOutCubic);
                if (this.musicToastFrame > whenToSwitchStateForTheFirstTime) {
                    slideState = SlideState.SHOWN;
                }
            }
            case SHOWN       -> {
                musicPopupY = 0;
                if (this.musicToastFrame > whenToSwitchStateForTheSecondTime) {
                    slideState = SlideState.SLIDING_OUT;
                }
            }
            case SLIDING_OUT -> {
                musicPopupY = Util.getEasedValue(0, hiddenY, (this.musicToastFrame - whenToSwitchStateForTheSecondTime) / whenToSwitchStateForTheFirstTime, Easings::easeInCubic);
                if (this.musicToastFrame > whenToSwitchStateForTheSecondTime + whenToSwitchStateForTheFirstTime) {
                    slideState = SlideState.HIDDEN;
                }
            }
            case HIDDEN      -> musicPopupY = hiddenY;
        }
    }

    private void renderMusicPopup() {
        if (this.slideState == SlideState.HIDDEN) return;

        renderer.pose().pushMatrix();

        Vector2f pos = new Vector2f(this.window.setupSize.x - 635, 0).add(-25, 25);

        renderer.pose().translate(pos);

        animateMusicPopup();

        renderer.pose().translate(0, musicPopupY);

        renderer.pose().scale(0.85f, 0.85f);

        renderer.draw.roundedRect(
                new Vector2f(5, 5),
                new Vector2f(750, 75),
                20,
                this.theme.popupShadow()
        );

        renderer.draw.roundedRect(
                new Vector2f(),
                new Vector2f(750, 75),
                20,
                this.theme.popup()
        );

        int minorSpacing = 35;
        int majorSpacing = 50;

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(512.5f, 21.5f));

        renderer.pose().pushMatrix();

        renderer.draw.triangle(
                new Vector2f(0, 16),
                new Vector2f(16, 0),
                new Vector2f(16, 32),
                this.theme.control()
        );

        renderer.pose().popMatrix();

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(16, 0));

        renderer.draw.triangle(
                new Vector2f(0, 16),
                new Vector2f(16, 0),
                new Vector2f(16, 32),
                this.theme.control()
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(minorSpacing, 0));

        renderer.draw.triangle(
                new Vector2f(0, 16),
                new Vector2f(16, 0),
                new Vector2f(16, 32),
                this.theme.control()
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(majorSpacing, 0));

        renderer.draw.rect(
                new Vector2f(),
                new Vector2f(6, 32),
                this.theme.control()
        );

        renderer.draw.rect(
                new Vector2f(12, 0),
                new Vector2f(6, 32),
                this.theme.control()
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(majorSpacing, 0));

        renderer.draw.triangle(
                new Vector2f(16, 16),
                new Vector2f(0, 0),
                new Vector2f(0, 32),
                this.theme.control()
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(minorSpacing, 0));

        renderer.draw.triangle(
                new Vector2f(16, 16),
                new Vector2f(0, 0),
                new Vector2f(0, 32),
                this.theme.control()
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(16, 0));

        renderer.draw.triangle(
                new Vector2f(16, 16),
                new Vector2f(0, 0),
                new Vector2f(0, 32),
                this.theme.control()
        );

        renderer.pose().popMatrix();
        renderer.pose().popMatrix();
        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        this.images.get("SVG_MusicPopup_Notes").renderWithTexture(() -> renderer.draw.rect(
                new Vector2f(15, 12.5f),
                new Vector2f(50, 50),
                Color.WHITE
        ));

        renderer.draw.text(
                futuraFont.getStyle("Demi"),
                albumAndSong.getSecond().getName(),
                new Vector2f(75, 0),
                Color.WHITE
        );

        renderer.pose().pushMatrix();

        renderer.pose().scale(0.75f);

        renderer.draw.text(
                futuraFont.getStyle("Demi"),
                albumAndSong.getFirst().getArtist(),
                new Vector2f(100, 55),
                Color.WHITE
        );

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();
    }

    private void renderWindow(float width, float height, String title, String subtitle, BiConsumer<Float, Float> contents) {
        Vector2f pos = new Vector2f((1280 - width) / 2, (720 - height) / 2);
        Vector2f size = new Vector2f(width, height);

        renderer.pose().pushMatrix();

        renderer.pose().translate(pos);

        float radius = 25;

        renderer.draw.roundedRect(
                new Vector2f(5, 5),
                size,
                radius,
                this.theme.popupShadow()
        );

        renderer.draw.roundedRect(
                new Vector2f(),
                size,
                radius,
                this.theme.popup()
        );

        this.images.get("SVG_Pencil").renderWithTexture(() -> renderer.draw.rect(
                new Vector2f(4, 2),
                new Vector2f(48),
                Color.WHITE
        ));

        renderer.pose().pushMatrix();

        renderer.pose().translate(56, 4);

        Font font = this.futuraFont.getStyle("Demi");

        renderer.draw.text(
                font,
                title,
                new Vector2f(),
                Color.WHITE
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(
                font.getTextWidth(title) + 4,
                16
        );

        renderer.pose().scale(0.5f);

        renderer.draw.text(
                font,
                subtitle,
                new Vector2f(),
                Color.WHITE
        );

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().pushMatrix();

        renderer.draw.roundedRect(
                new Vector2f(),
                new Vector2f(48, 10),
                10/2f,
                this.theme.control()
        );

        renderer.pose().popMatrix();

        renderer.pose().pushMatrix();

        int topMargin = 52;
        int margin = 8;

        renderer.pose().translate(margin, topMargin);

        Vector2f frameSize = new Vector2f(width - (margin * 2), height - (topMargin + margin));

        renderer.beginMask(() -> {
            renderer.draw.roundedRect(
                    new Vector2f(),
                    frameSize,
                    radius,
                    Color.WHITE
            );

            renderer.draw.rect(
                    new Vector2f(),
                    new Vector2f(frameSize.x, radius),
                    Color.WHITE
            );
        });

        contents.accept(frameSize.x, frameSize.y);

        renderer.endMask();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();
    }

    private void renderBackground(int rectSize) {
        renderer.pose().pushMatrix();

        renderer.pose().rotate((float)Math.toRadians(15));
        renderer.pose().translate(0, -365);

        float calculatedFrame = checkerboardFrame;

        if (calculatedFrame >= rectSize * 2) {
            checkerboardFrame = 0;
            calculatedFrame = 0;
        }

        renderer.pose().translate(new Vector2f(-calculatedFrame));

        for (int y = 0; y < 22; y++) {
            for (int x = 0; x < 40; x++) {
                int id = x ^ y;
                boolean darker = id % 2 == 0;
                Color color =
                        darker ?
                                this.theme.checkerboardDark() :
                                this.theme.checkerboardLight();
                renderer.draw.rect(
                        new Vector2f(x * rectSize, y * rectSize),
                        new Vector2f(rectSize),
                        color
                );
            }
        }

        renderer.pose().popMatrix();
    }
}
