package net.maxurhino.doodley_editor.app;

import net.maxurhino.doodley_editor.Window;
import net.maxurhino.doodley_editor.util.*;
import net.maxurhino.doodley_editor.util.commons.CommonColors;
import net.maxurhino.doodley_editor.util.enums.SlideState;
import net.maxurhino.doodley_editor.util.json.SongsMetadata;
import net.maxurhino.doodley_editor.util.movement.Easings;
import net.maxurhino.doodley_editor.util.multiples.pairs.Pair;
import net.maxurhino.doodley_editor.util.render.Renderer;
import net.maxurhino.doodley_editor.util.render.font.FontFamily;
import net.maxurhino.doodley_editor.util.render.texture.*;
import net.maxurhino.doodley_editor.util.sound.*;
import org.joml.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final int whenToSwitchStateForTheFirstTime  = 50,
                      whenToSwitchStateForTheSecondTime = 125;

    private SlideState slideState = SlideState.SLIDING_IN;
    private float musicPopupY;

    @Override
    public void run() {
        window.create();

        renderer = new Renderer(window);
        renderer.setClearColor(Color.WHITE);

        try {
            this.images.put(
                    "SVG_MusicPopup_Notes",
                    new Texture(
                            SVGLoader.loadSVG(
                                    Paths.image.SVG("MusicPopup/Notes"),
                                    64,
                                    64,
                                    Color.WHITE
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        audio.create();

        songsMetadata = Util.loadJsonFromPath(
                Paths.music("SongsMetadata.json"),
                SongsMetadata.class
        );

        this.albumAndSong = Util.getAlbumAndSong(
                songsMetadata,
                "DS1",
                "COC"
        );

        SongsMetadata.Album.ResourceInfo resourceInfo = albumAndSong.getFirst().getResourceInfo();

        music = new Sound(Paths.music(resourceInfo.getAuthor(), resourceInfo.getAlbum(), albumAndSong.getSecond().getFileName()));
        music.setLooping(true);
        music.setVolume(0.5f);
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

        renderMusicPopup();

        renderer.pose().popMatrix();
    }

    private void animateMusicPopup() {
        int hiddenY = -100;

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
            case HIDDEN      -> {
                musicPopupY = hiddenY;
            }
        };
    }

    private void renderMusicPopup() {
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
                CommonColors.POPUP_SHADOW
        );

        renderer.draw.roundedRect(
                new Vector2f(),
                new Vector2f(750, 75),
                20,
                CommonColors.POPUP
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
                CommonColors.CONTROL
        );

        renderer.pose().popMatrix();

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(16, 0));

        renderer.draw.triangle(
                new Vector2f(0, 16),
                new Vector2f(16, 0),
                new Vector2f(16, 32),
                CommonColors.CONTROL
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(minorSpacing, 0));

        renderer.draw.triangle(
                new Vector2f(0, 16),
                new Vector2f(16, 0),
                new Vector2f(16, 32),
                CommonColors.CONTROL
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(majorSpacing, 0));

        renderer.draw.rect(
                new Vector2f(),
                new Vector2f(6, 32),
                CommonColors.CONTROL
        );

        renderer.draw.rect(
                new Vector2f(12, 0),
                new Vector2f(6, 32),
                CommonColors.CONTROL
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(majorSpacing, 0));

        renderer.draw.triangle(
                new Vector2f(16, 16),
                new Vector2f(0, 0),
                new Vector2f(0, 32),
                CommonColors.CONTROL
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(minorSpacing, 0));

        renderer.draw.triangle(
                new Vector2f(16, 16),
                new Vector2f(0, 0),
                new Vector2f(0, 32),
                CommonColors.CONTROL
        );

        renderer.pose().pushMatrix();

        renderer.pose().translate(new Vector2f(16, 0));

        renderer.draw.triangle(
                new Vector2f(16, 16),
                new Vector2f(0, 0),
                new Vector2f(0, 32),
                CommonColors.CONTROL
        );

        renderer.pose().popMatrix();
        renderer.pose().popMatrix();
        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        renderer.pose().popMatrix();

        this.images.get("SVG_MusicPopup_Notes").renderWithTexture(() -> {
            renderer.draw.rect(
                    new Vector2f(15, 12.5f),
                    new Vector2f(50, 50),
                    Color.WHITE
            );
        });

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
                                CommonColors.CHECKERBOARD_DARK :
                                CommonColors.CHECKERBOARD_LIGHT;
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
