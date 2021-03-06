package fr.vikingGameJam.tacheKiller2000;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Messages extends Animation
{
	private static Messages instance = null;
	public static final int WIDTH = 256;
	public static final int HEIGHT = 64;
	private static final int nbFrames = 15;

	public static Messages getInstance()
	{
		if (instance == null)
		{
			
			Sprite[] frames = new Sprite[nbFrames];
			for (int i = 0; i < nbFrames; i++)
				frames[i] = new Sprite(
						new Texture(Gdx.files.internal("img/messages.png")), i * WIDTH, 0, WIDTH,
						HEIGHT);
			instance = new Messages(1.0F, frames);
			instance.setPlayMode(PlayMode.NORMAL);
		}
		return instance;
	}
	
	public TextureRegion getKeyFrame(int number)
	{
		return super.getKeyFrame(number);
	}
	
	private Messages(float frameDuration, TextureRegion[] keyFrames)
	{
		super(frameDuration, keyFrames);
	}
}
