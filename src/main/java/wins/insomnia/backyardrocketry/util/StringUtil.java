package wins.insomnia.backyardrocketry.util;

public class StringUtil {


	public static int countCharacter(String inputString, char character) {

		int amount = 0;

		for (int i = 0; i < inputString.length(); i++) {
			if (inputString.charAt(i) == character) amount++;
		}

		return amount;
	}

}
