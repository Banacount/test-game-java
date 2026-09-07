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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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
    ShapeRenderer shapeRender;

    // High scope declarations
    Vector2 touchPos;
    Rectangle bucketRectangle;
    float universalVolume;

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
        shapeRender = new ShapeRenderer();
        bucketRectangle = new Rectangle();

        int spawnDropCount = 5;

        for (int i = 0; i < spawnDropCount; ++i)
        {
            createDroplet();
        }

        // Game volume
        universalVolume = 0.2f;
        music.setLooping(true);
        music.setVolume(universalVolume);
        music.play();
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
        float bucketWidth = bucketSprite.getWidth();
        float delta = Gdx.graphics.getDeltaTime();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth-bucketWidth));
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(),
                            bucketSprite.getWidth(), bucketSprite.getHeight());

        for (Droplet drop: dropSprites) {
            Sprite dropSprite = drop.sprite;
            dropSprite.translateY(-drop.fallSpeed * delta);

            // Reset spawn
            class Helper {
                void resetDropPosition ()
                {
                    float randomX = MathUtils.random(0f, worldWidth - dropSprite.getHeight());

                    dropSprite.setY(worldHeight + dropSprite.getHeight());
                    dropSprite.setX(randomX);
                    drop.fallSpeed = MathUtils.random(1f, 2.1f);
                }
            }

            Helper funcs = new Helper();

            if (dropSprite.getY() < -dropSprite.getHeight())
            {
                funcs.resetDropPosition();
            }
            else if (bucketRectangle.overlaps(drop.rect)) {
                funcs.resetDropPosition();
                dropSound.play(universalVolume);
            }

            drop.updateRect();
        }
    }

    public void draw() {
        float worldHeight = viewport.getWorldHeight();
        float worldWidth = viewport.getWorldWidth();

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        // Background
        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        spriteBatch.end();

        // Draw the rects of objects
        //drawHitBoxes();

        // Draw bucket droplets
        spriteBatch.begin();
        bucketSprite.draw(spriteBatch);
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
        dropSprite.setPosition(randomX, worldHeight-dropH);

        Droplet drop = new Droplet(dropSprite);
        dropSprites.add(drop);
    }

    public void drawHitBoxes () {
        shapeRender.begin(ShapeRenderer.ShapeType.Filled);
        shapeRender.setProjectionMatrix(viewport.getCamera().combined);
        shapeRender.setColor(Color.RED);
        shapeRender.rect(bucketRectangle.x, bucketRectangle.y, bucketRectangle.width, bucketRectangle.height);
        shapeRender.end();
        for (Droplet drop: dropSprites) {
            drop.drawRect(shapeRender, viewport);
        }
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
    Rectangle rect;
    float fallSpeed = MathUtils.random(1f, 2.1f);

    public Droplet (Sprite dropSprite) {
        sprite = dropSprite;
        rect = new Rectangle(
                    dropSprite.getX(),
                    dropSprite.getY(),
                    dropSprite.getWidth(),
                    dropSprite.getHeight()
                );
    }

    void updateRect () {
        rect.set(
            sprite.getX(),
            sprite.getY(),
            sprite.getWidth(),
            sprite.getHeight()
        );
    }

    void drawRect (ShapeRenderer shapeRender, Viewport viewport) {
        shapeRender.begin(ShapeRenderer.ShapeType.Filled);
        shapeRender.setProjectionMatrix(viewport.getCamera().combined);
        shapeRender.setColor(Color.RED);
        shapeRender.rect(rect.x, rect.y, rect.width, rect.height);
        shapeRender.end();
    }
}
