package fr.vikingGameJam.tacheKiller2000;

import java.util.LinkedList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game extends ApplicationAdapter
{
	public static final int WIDTH = 800;
	public static final int HEIGHT = 760;
	private final int LEVEL_MAX = 12;
	private int insulte;

	private static float difficulty;

	SpriteBatch batch;
	private float stateTime;
	private Moustache moustache;
	private LinkedList<Tache> taches;
	private LinkedList<Missile> missiles;
	private LinkedList<TacheDeathAnimation> deathAnimations;

	private long lastMissile;
	private GameOverAnimation gameOver = null;
	private LevelUpAnimation levelUp = null;

	private Score score;
	Sound soundGameOver;
	Sound soundMoustache;
	Sound soundLaught;
	Sound soundShoot;
	Sound music;
	long musicId;

	private static Sprite backgroundSprite;

	@Override
	public void create()
	{
		batch = new SpriteBatch();
		stateTime = 0;
		taches = new LinkedList<Tache>();
		missiles = new LinkedList<Missile>();
		difficulty = 1;
		lastMissile = System.currentTimeMillis();
		deathAnimations = new LinkedList<TacheDeathAnimation>();
		moustache = Moustache.getMoustache();

		score = new Score();

		backgroundSprite = new Sprite(new Texture(Gdx.files.internal("img/background.png")), 0,
				0, 800, 800);
		soundGameOver = Gdx.audio.newSound(Gdx.files.internal(
				"sounds/game_over.wav"));
		soundMoustache = Gdx.audio.newSound(Gdx.files.internal(
				"sounds/moustache.wav"));
		soundLaught = Gdx.audio.newSound(Gdx.files.internal(
				"sounds/laught.wav"));
		soundShoot = Gdx.audio.newSound(Gdx.files.internal(
				"sounds/shoot.wav"));
		music = Gdx.audio.newSound(Gdx.files.internal(
				"sounds/theme_sound.wav"));
		soundMoustache.play();
		musicId = music.loop();
	}

	@Override
	public void render()
	{
		stateTime++;
		music.setPitch(musicId, (difficulty + 13.0f) / 14.0f);
		if (gameOver != null)
		{
			drawGameOver();
			
			if(Gdx.input.isKeyPressed(Input.Keys.ENTER))
			{
				restartGame();
			}
			return;
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		play();

		batch.begin();
		float backgroundSpeed = (10 * stateTime * difficulty) % 800;
		batch.draw(backgroundSprite, 0, -backgroundSpeed);
		batch.draw(backgroundSprite, 0, 800 - backgroundSpeed);
		batch.draw(backgroundSprite, 0, 1600 - backgroundSpeed);
		for (Missile missile : missiles)
			batch.draw(missile.getKeyFrame(), missile.getCoordX(),
					missile.getCoordY());
		batch.draw(moustache.getKeyFrame(stateTime), moustache.getCoordX(), 0);
		for (Tache tache : taches)
			batch.draw(tache.getKeyFrame(stateTime), tache.getCoordX(),
					tache.getCoordY());
		for (TacheDeathAnimation deathTache : deathAnimations)
			batch.draw(deathTache.getKeyFrame(), deathTache.getCoorX(),
					deathTache.getCoorY());
		if (stateTime % 100 / difficulty == 0)
			taches.add(Tache.getTache(moustache));

		score.draw(batch);

		if (levelUp != null)
			try
			{
				batch.draw(levelUp.getKeyFrame(),
						(WIDTH - GameOverAnimation.WIDTH) / 2,
						(HEIGHT - GameOverAnimation.HEIGHT) / 2);
			} catch (LevelUpAnimationEnded e)
			{
				levelUp = null;
			}

		batch.end();

		score.sub(20 * removeOut(taches));
		score.sub(15 * removeOut(missiles));
		checkCollisions();

		if (score.getValue() >= difficulty * 100 && difficulty < LEVEL_MAX)
		{
			difficulty += 1;
			levelUp = LevelUpAnimation.getLevelUp();
			soundLaught.play();
		}
	}

	private int removeOut(LinkedList<? extends Outable> list)
	{
		LinkedList<Outable> toRemove = new LinkedList<Outable>();
		for (Outable outable : list)
			if (outable.isOut())
				toRemove.add(outable);
		list.removeAll(toRemove);
		return toRemove.size();
	}

	private void checkCollisions()
	{
		LinkedList<Missile> toRemoveMissile = new LinkedList<Missile>();
		LinkedList<Tache> toRemoveTache = new LinkedList<Tache>();
		LinkedList<TacheDeathAnimation> toRemoveDeadTache = new LinkedList<TacheDeathAnimation>();
		for (Tache tache : taches)
		{
			if (tache.getCoordY() < moustache.getHeight() - 10
					&& tache.getCoordY() > 20
					&& tache.getCoordX() < moustache.getRightCorner() - 8
					&& tache.getRightCorner() > moustache.getCoordX() + 8)
				gameOver();

			for (Missile missile : missiles)
			{
				if (((missile.getCoordX() < tache.getCoordX() && tache
						.getCoordX() < (missile.getCoordX() + Missile.SIZE)) || (missile
						.getCoordX() < tache.getCoordX() + Tache.SIZE && tache
						.getCoordX() + Tache.SIZE < (missile.getCoordX() + Missile.SIZE)))
						&& missile.getCoordY() < tache.getCoordY()
						&& tache.getCoordY() < missile.getCoordY()
								+ Missile.SIZE)
				{
					toRemoveMissile.add(missile);
					toRemoveTache.add(tache);
					score.add(10);
				}
			}
		}

		for (TacheDeathAnimation deadTache : deathAnimations)
		{
			if(deadTache.getStateTime() > 16)
				toRemoveDeadTache.add(deadTache);
		}
		for (Tache deadTache : toRemoveTache)
		{
			deathAnimations.add(TacheDeathAnimation.getTacheDeathAnimation(deadTache.getCoordX(),deadTache.getCoordY(),deadTache.getIaNum()));
		}
		deathAnimations.removeAll(toRemoveDeadTache);
		taches.removeAll(toRemoveTache);
		missiles.removeAll(toRemoveMissile);
	}

	private void gameOver()
	{
		if (gameOver != null)
			return;
		gameOver = GameOverAnimation.getInstance();
		soundGameOver.play();
		insulte = (int) (Math.random() * 15);
	}

	private void restartGame()
	{
		gameOver = null;
		stateTime = 0;
		taches = new LinkedList<Tache>();
		missiles = new LinkedList<Missile>();
		difficulty = 1;
		lastMissile = System.currentTimeMillis();
		score = new Score();
		soundMoustache.play();
	}

	private void drawGameOver()
	{
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(gameOver.getKeyFrame(stateTime),
				(WIDTH - GameOverAnimation.WIDTH) / 2,
				(HEIGHT - GameOverAnimation.HEIGHT) / 2);
		batch.draw(Messages.getInstance().getKeyFrame(insulte),
				(WIDTH - Messages.WIDTH) / 2,
				(HEIGHT - GameOverAnimation.HEIGHT) / 3);
		batch.end();
	}

	private void play()
	{
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
			moustache.moveLeft();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			moustache.moveRight();
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)
				&& lastMissile < System.currentTimeMillis())
		{
			missiles.add(Missile.getMissile(moustache));
			lastMissile = System.currentTimeMillis() + 200;
			soundShoot.play((float) 0.1);
		}
		for (Tache tache : taches)
			tache.move();
		for (Missile missile : missiles)
			missile.move();
	}

	public static float getDifficulty()
	{
		return difficulty;
	}
}
