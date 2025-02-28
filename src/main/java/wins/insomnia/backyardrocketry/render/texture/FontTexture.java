package wins.insomnia.backyardrocketry.render.texture;

public class FontTexture extends Texture {

	// char[0].width, char[0].x, char[0].y,
	// char[1].width, char[1].x, char[1].y,
	// . . . ,
	// char[n].width, char[n].x, char[n].y
	public static final int[] DEFAULT_FONT_CHARACTER_DIMENSIONS = new int[] {
			7, 0,      0,
			7, 7,      0,
			7, 7 * 2,  0,
			7, 7 * 3,  0,
			7, 7 * 4,  0,
			7, 7 * 5,  0,
			7, 7 * 6,  0,
			7, 7 * 7,  0,
			7, 7 * 8,  0,
			7, 7 * 9,  0,
			7, 7 * 10, 0,
			7, 7 * 11, 0,
			7, 7 * 12, 0,
			7, 7 * 13, 0,
			7, 7 * 14, 0,
			7, 7 * 15, 0,
			7, 7 * 16, 0,
			7, 7 * 17, 0,

			7, 0,      12,
			7, 7,      12,
			7, 7 * 2,  12,
			7, 7 * 3,  12,
			7, 7 * 4,  12,
			7, 7 * 5,  12,
			7, 7 * 6,  12,
			7, 7 * 7,  12,
			7, 7 * 8,  12,
			7, 7 * 9,  12,
			7, 7 * 10, 12,
			7, 7 * 11, 12,
			7, 7 * 12, 12,
			7, 7 * 13, 12,
			7, 7 * 14, 12,
			7, 7 * 15, 12,
			7, 7 * 16, 12,
			7, 7 * 17, 12,

			7, 0,      24,
			7, 7,      24,
			7, 7 * 2,  24,
			7, 7 * 3,  24,
			7, 7 * 4,  24,
			7, 7 * 5,  24,
			7, 7 * 6,  24,
			7, 7 * 7,  24,
			7, 7 * 8,  24,
			7, 7 * 9,  24,
			7, 7 * 10, 24,
			7, 7 * 11, 24,
			7, 7 * 12, 24,
			7, 7 * 13, 24,
			7, 7 * 14, 24,
			7, 7 * 15, 24,
			7, 7 * 16, 24,
			7, 7 * 17, 24,

			7, 0,      36,
			7, 7,      36,
			7, 7 * 2,  36,
			7, 7 * 3,  36,
			7, 7 * 4,  36,
			7, 7 * 5,  36,
			7, 7 * 6,  36,
			7, 7 * 7,  36,
			7, 7 * 8,  36,
			7, 7 * 9,  36,
			7, 7 * 10, 36,
			7, 7 * 11, 36,
			7, 7 * 12, 36,
			7, 7 * 13, 36,
			7, 7 * 14, 36,
			7, 7 * 15, 36,
			7, 7 * 16, 36,
			7, 7 * 17, 36,

			7, 0,      48,
			7, 7,      48,
			7, 7 * 2,  48,
			7, 7 * 3,  48,
			7, 7 * 4,  48,
			7, 7 * 5,  48,
			7, 7 * 6,  48,
			7, 7 * 7,  48,
			7, 7 * 8,  48,
			7, 7 * 9,  48,
			7, 7 * 10, 48,
			7, 7 * 11, 48,
			7, 7 * 12, 48,
			7, 7 * 13, 48,
			7, 7 * 14, 48,
			7, 7 * 15, 48,
			7, 7 * 16, 48,
			7, 7 * 17, 48,

			7, 0,      60,
			7, 7,      60,
			7, 7 * 2,  60,
			7, 7 * 3,  60
	};

	public static final int[] DEBUG_FONT_CHARACTER_DIMENSIONS = new int[] {
			5, 0,      2, // A
			5, 7,      2, // B
			5, 7 * 2,  2, // C
			5, 7 * 3,  2, // D
			5, 7 * 4,  2, // E
			5, 7 * 5,  2, // F
			5, 7 * 6,  2, // G
			5, 7 * 7,  2, // H
			4, 7 * 8,  2, // I
			5, 7 * 9,  2, // J
			5, 7 * 10, 2, // K
			4, 7 * 11, 2, // L
			6, 7 * 12, 2, // M
			5, 7 * 13, 2, // N
			5, 7 * 14, 2, // O
			5, 7 * 15, 2, // P
			5, 7 * 16, 2, // Q
			5, 7 * 17, 2, // R

			5, 0,      14, // S
			4, 7,      14, // T
			5, 7 * 2,  14, // U
			5, 7 * 3,  14, // V
			6, 7 * 4,  14, // W
			5, 7 * 5,  14, // X
			6, 7 * 6,  14, // Y
			5, 7 * 7,  14, // Z
			5, 7 * 8,  14, // a
			5, 7 * 9,  14, // b
			4, 7 * 10, 14, // c
			5, 7 * 11, 14, // d
			5, 7 * 12, 14, // e
			5, 7 * 13, 14, // f
			5, 7 * 14, 14, // g
			5, 7 * 15, 14, // h
			4, 7 * 16, 14, // i
			4, 7 * 17, 14, // j

			4, 0,      26, // k
			3, 7,      26, // l
			6, 7 * 2,  26, // m
			5, 7 * 3,  26, // n
			5, 7 * 4,  26, // o
			5, 7 * 5,  26, // p
			6, 7 * 6,  26, // q
			4, 7 * 7,  26, // r
			4, 7 * 8,  26, // s
			4, 7 * 9,  26, // t
			5, 7 * 10, 26, // u
			4, 7 * 11, 26, // v
			6, 7 * 12, 26, // w
			4, 7 * 13, 26, // x
			5, 7 * 14, 26, // y
			5, 7 * 15, 26, // z
			4, 7 * 16, 26, // 1
			4, 7 * 17, 26, // 2

			5, 0,      38, // 3
			4, 7,      38, // 4
			4, 7 * 2,  38, // 5
			4, 7 * 3,  38, // 6
			4, 7 * 4,  38, // 7
			4, 7 * 5,  38, // 8
			4, 7 * 6,  38, // 9
			4, 7 * 7,  38, // 0
			4, 7 * 8,  38, // "
			4, 7 * 9,  38, // \
			4, 7 * 10, 38, // (
			4, 7 * 11, 38, // )
			3, 7 * 12, 38, // |
			5, 7 * 13, 38, // }
			5, 7 * 14, 38, // {
			4, 7 * 15, 38, // ;
			4, 7 * 16, 38, // <
			4, 7 * 17, 38, // >

			5, 0,      50, // -
			6, 7,      50, // +
			5, 7 * 2,  50, // %
			5, 7 * 3,  50, // ?
			2, 7 * 4,  50, // ,
			2, 7 * 5,  50, // .
			4, 7 * 6,  50, // /
			2, 7 * 7,  50, // !
			3, 7 * 8,  50, // :
			6, 7 * 9,  50, // $
			5, 7 * 10, 50, // _
			5, 7 * 11, 50, // =
			6, 7 * 12, 50, // &
			6, 7 * 13, 50, // ~
			5, 7 * 14, 50, // *
			6, 7 * 15, 50, // #
			4, 7 * 16, 50, // ]
			4, 7 * 17, 50, // [

			3, 0,      62, // `
			7, 7,      62, // @
			5, 7 * 2,  62, // ^
			5, 7 * 3,  62  // ' ' <- space character
	};


	private final String KEY_STRING; // string holding order of characeters
	private final int[] CHARACTER_DIMENSIONS;
	private final int CHARACTER_HEIGHT;

	public FontTexture(String textureName, int characterHeight, int[] characterDimensions) {
		super(textureName);
		CHARACTER_HEIGHT = characterHeight;
		CHARACTER_DIMENSIONS = characterDimensions;
		KEY_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"\\()|}{;<>-+%?,./!:$_=&~*#][`@^ ";
	}

	public String getKeyString() {
		return KEY_STRING;
	}

	public int getCharacterWidth(char character) {

		for (int i = 0; i < getKeyString().length(); i++) {

			if (KEY_STRING.charAt(i) == character) {
				return getCharacterWidth(i);
			}

		}

		return 0;

	}

	public int getCharacterWidth(int characterIndex) {

		return CHARACTER_DIMENSIONS[characterIndex * 3];

	}

	public int getCharacterHeight() {

		return CHARACTER_HEIGHT;

	}

	public int getCharacterX(int characterIndex) {
		return CHARACTER_DIMENSIONS[characterIndex * 3 + 1];
	}

	public int getCharacterY(int characterIndex) {
		return CHARACTER_DIMENSIONS[characterIndex * 3 + 2];
	}

}
