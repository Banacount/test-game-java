package io.test.gamelol;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class Main implements ApplicationListener {
    // Declaration assets
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Array<Droplet> dropSprites;

    // High scope declarations
    Vector2 touchPos;

    @Override
    public void create() {
        // Prepare your application here.

        // Texture
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");

        // Music
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        touchPos = new Vector2();
        dropSprites = new Array<>();

        int spawnDropCount = 5;

        for (int i = 0; i < spawnDropCount; ++i)
        {
            createDroplet();
        }
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;
        viewport.update(width, height, true);

        // Resize your application here. The parameters represent the new window size.
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    public void input() {
        float speed = 3.5f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D))
        {
            bucketSprite.translateX(speed * delta);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A))
        {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }
    }

    public void logic() {
        float worldHeight = viewport.getWorldHeight();
        float worldWidth = viewport.getWorldWidth();
        float delta = Gdx.graphics.getDeltaTime();

        for (Droplet drop: dropSprites) {
            Sprite dropSprite = drop.sprite;
            dropSprite.translateY(-drop.fallSpeed * delta);

            if (dropSprite.getY() < -dropSprite.getHeight()) {
                float randomX = MathUtils.random(0f, worldWidth - dropSprite.getHeight());

                dropSprite.setY(worldHeight + dropSprite.getHeight());
                dropSprite.setX(randomX);
                drop.fallSpeed = MathUtils.random(1f, 2.1f);
            }
        }
    }

    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldHeight = viewport.getWorldHeight();
        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth-bucketWidth));
        bucketSprite.draw(spriteBatch);

        // Draw sprites
        for (Droplet drop: dropSprites) {
            drop.sprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    public void createDroplet() {
        float dropW = 0.5f, dropH = 0.5f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float randomX = MathUtils.random(0f, worldWidth - dropW);

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropW, dropH);
        dropSprite.setPosition(randomX, worldHeight-1f);

        Droplet drop = new Droplet(dropSprite, 1f);
        dropSprites.add(drop);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }
}

class Droplet {
    Sprite sprite;
    float fallSpeed = 1;

    public Droplet (Sprite dropSprite, float dropletFallSpeed) {
        sprite = dropSprite;
        fallSpeed = dropletFallSpeed;
    }
}
