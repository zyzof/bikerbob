package game;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LevelDbReader extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "bikerbob.db";
	private static final int DATABASE_VERSION = 1;

	public LevelDbReader(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * TODO: read statements from files.
	 * TODO: find out how to include a populated db file in assets/
	 * and query it directly
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		final String CREATE_TABLE_STATEMENT = 
				"CREATE TABLE level ("
				+ "id INTEGER PRIMARY KEY,"
				+ "level_number INTEGER NOT NULL,"
				+ "points text NOT NULL"
				+ ")";
		
		db.execSQL(CREATE_TABLE_STATEMENT);
		
		final String INSERT_LEVELS_STATEMENT =
				"INSERT INTO level (level_number, points) VALUES (1,"
				+ "\'1.0f 1.0f 0.0f,"
				+ "-0.0f -0.0f 0.0f,"
				+ "-1.0f -1.0f 0.0f,"
				+ "-2.0f -1.0f 0.0f,"
				+ "-2.5f -0.5f 0.0f,"
				+ "-50f -50f 0.0f')";
		db.execSQL(INSERT_LEVELS_STATEMENT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS level");
		onCreate(db);
	}
	
	public float[] readLevel(int levelNum) {
		final String GET_LEVEL_QUERY = String.format(
				"SELECT points FROM level WHERE level_number = %s", levelNum);
				
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(GET_LEVEL_QUERY, null);
		if (cursor != null) {
			cursor.moveToFirst();
			String pointsStr = cursor.getString(0);
			return getGlPointsArrayFromString(pointsStr);
		}
		return null;
	}

	/**
	 * Read ground points from the string obtained from the db.
	 * Points consist of 3 floats separated by spaces.
	 * Points are separated by commas.
	 * @param pointsStr
	 * @return
	 */
	private float[] getGlPointsArrayFromString(String pointsStr) {
		List<Float> pointCoords = new ArrayList<Float>();
		
		String[] points = pointsStr.split(",");
		for (String point : points) {
			String[] parts = point.split(" ");
			Float x = Float.valueOf(parts[0]);
			Float y = Float.valueOf(parts[1]);
			Float z = Float.valueOf(parts[2]);
			
			pointCoords.add(x);
			pointCoords.add(y);
			pointCoords.add(z);
			
			if (pointCoords.size() > 3) {
				int addAgainIndex = pointCoords.size() - 3;
				pointCoords.add(pointCoords.get(addAgainIndex));
				pointCoords.add(pointCoords.get(addAgainIndex + 1));
				pointCoords.add(pointCoords.get(addAgainIndex + 2));
			}
		}
		
		//Because java:
		float[] pointCoordsAsPrimitives = new float[pointCoords.size()];
		for (int i = 0; i < pointCoords.size(); i++) {
			pointCoordsAsPrimitives[i] = pointCoords.get(i).floatValue();
		}
		
		return pointCoordsAsPrimitives;
	}
}
