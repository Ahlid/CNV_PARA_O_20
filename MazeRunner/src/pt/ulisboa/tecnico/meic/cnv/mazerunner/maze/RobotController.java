package pt.ulisboa.tecnico.meic.cnv.mazerunner.maze;

public class RobotController {

	public static long CURRENT_RUN_TIME = 0;
	public static long CURRENT_OBSERVE_TIME = 0;
	
	public static byte PHOTO_ANALYZER = 0;
	
	/*
	 * Look at the posistion's photo and decide the next step.
	 */
	public static void observe(int observeTime, byte[] photo) {
		byte photoAnalyzer = 0;
		long currentObserveTime = 0;
		for(int k = 0; k < observeTime; k++) {
			for(int i = 0; i < 1250; i++) {
				// Analyzing the photo of the position.
				for(int photoIndex = 0; photoIndex < photo.length; photoIndex++) {
					photoAnalyzer = photo[photoIndex];
				}
				currentObserveTime = System.currentTimeMillis();
			}
		}
		PHOTO_ANALYZER = photoAnalyzer;
		CURRENT_OBSERVE_TIME = currentObserveTime;
	}
	
	/*
	 * Running to the next maze position.
	 */
	public static void run(int velocity) {
		long currentRunTime = 0;
		int minVelocityLoops = 10000;
		// Time to run into the next maze position.
		for(int k = 0; k < minVelocityLoops / velocity; k++) {
			for(int i = 0; i < 4500; i++) {
				currentRunTime = System.currentTimeMillis();
			}
		}
		CURRENT_RUN_TIME = currentRunTime;
	}

}
